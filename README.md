# Velo Stats

Java service for tracking Velo Antwerp bike-share stations, ride history, routing and weather.

Ride history is loaded from a JSON export, station information from the public Velo Antwerp GBFS feed. Each ride is
then enriched in the background: the cycling distance between its two stations comes from the public OSRM routing
API, and the weather at its origin station and checkin time comes from the free Open-Meteo archive. The API serves
the combined data as JSON.

This is a port of the Symfony application of the same name, and serves byte-identical JSON on every endpoint.

## Setup

```
docker compose up -d --build
```

This starts five services:
- `app` – the API, served on port `8000`
- `redis` – queue backend
- `worker` – queue worker for the `default` queue
- `worker-ride-distance` – worker consuming the `ride_distance_checks` queue one job at a time, so calls to the free
  routing API are never made concurrently
- `worker-ride-weather` – worker consuming the `ride_weather_checks` queue one job at a time, so calls to the free
  Open-Meteo API are never made concurrently

Every container migrates the database on start, so no manual setup is needed.

Verify the app is up and running:

```
curl http://localhost:8000/_healthcheck
```

Should return a 200 OK response.

Stop everything with:

```
docker compose down
```

The SQLite database and the ride export are bind-mounted from `database/` and `data/`, so data survives a rebuild
and a new export can be dropped in without one. The jar itself lives in the image, so code changes need a rebuild:

```
docker compose up -d --build
```

### Loading data

A fresh database is empty. Populate it in this order:

```
docker compose exec app velo stations:load
docker compose exec app velo rides:load
docker compose exec app velo rides:check-distances
docker compose exec app velo rides:check-weather
```

The last two commands queue one job per ride and return immediately. The dedicated workers drain them one call at a
time, which takes a few minutes for a full ride history. Both are safe to re-run: a ride is only queued while its
`distance_checked_at` or `weather_checked_at` is still null, so a failed check is picked up next time and a finished
one is never repeated. `rides:check-weather --force` re-fetches every ride regardless.

## Configuration

Environment variables, set in `docker-compose.yml` and documented in `.env.example`:

| Variable | Default | Description |
|---|---|---|
| `HTTP_PORT` | `8000` | Port the API listens on |
| `DB_DATABASE` | `database/database.sqlite` | Path to the SQLite database |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Comma-separated origins allowed to call the API |
| `REDIS_HOST` | `127.0.0.1` | Redis host backing the queues |
| `REDIS_PORT` | `6379` | Redis port |
| `REDIS_PASSWORD` | empty | Redis password, when one is set |
| `REDIS_DB` | `0` | Redis database index |
| `QUEUE_PREFIX` | `velo-stats:queues:` | Prefix for the queue keys in Redis |
| `RIDES_JSON_PATH` | `data/rides.json` | Path to the rides JSON export |

The three upstream endpoints can be overridden with `VELO_ANTWERP_STATION_INFORMATION_URL`, `OSRM_BASE_URL` and
`OPEN_METEO_ARCHIVE_URL`.

## API Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/_healthcheck` | Returns `{"message": "ok"}` with a 200 status if the app is up |
| `GET` | `/rides` | Returns every ride with its basic info, distance (from the cached station route), speed (distance ÷ the exact time between check-out and check-in), the expected ride time from the cached route and how far the actual ride time was under or over it, and cached weather, most recent first |
| `GET` | `/rides/summary` | Returns aggregate stats across all rides: total rides, total/average/longest/shortest duration, and total/average distance |
| `GET` | `/rides/cost` | Returns the cost per ride, using the € 58/year subscription price prorated over the date range from the first to the last ride, plus the equivalent cost and money saved versus paying with day passes (€ 5) or week passes (€ 12) instead |
| `GET` | `/stations` | Returns every known station with its coordinates |

None of them take a parameter.

## Console Commands

Run against the running `app` container:

```
docker compose exec app velo <command>
```

| Command | Description |
|---|---|
| `serve` | Serve the JSON API. This is what the `app` container runs, and what an empty command line defaults to |
| `work --queue=NAME` | Run a queue worker, draining one queue a single job at a time |
| `migrate` | Bring the database schema up to date. Every command does this on start, so it is rarely needed on its own |
| `stations:load` | Fetches Velo Antwerp station information from the public GBFS feed and upserts it into the database |
| `rides:load [--path=PATH]` | Loads ride history from a JSON export (defaults to `data/rides.json`) and upserts it into the database |
| `tasks:dispatch-test [--message=TEXT]` | Dispatches a test job that logs a message from the worker, useful for verifying the queue setup |
| `rides:check-distances` | Queues a job per unchecked ride to calculate and cache the distance between its origin and destination stations, one at a time via the `ride_distance_checks` queue |
| `rides:check-weather [--force]` | Queues a job per ride to fetch and cache the biking-relevant weather (temperature, precipitation, wind, cloud cover, humidity, weather code) at its origin station and checkin time from the free Open-Meteo API, one at a time via the `ride_weather_checks` queue. Only unchecked rides are queued by default; pass `--force` to re-fetch weather for every ride |

### Verifying the queue setup

Dispatch a test "hello world" job through the `app` container:

```
docker compose exec app velo tasks:dispatch-test --message "hello world"
```

Then check the `worker` container's logs to confirm the message was picked up and processed:

```
docker compose logs worker
```

You should see a log line containing `hello world` from the worker.

## Production

`docker-compose.prod.yml` runs the shape this would actually be deployed in: no bind mounts, the seeded SQLite
database baked into the image, and the JVM told how many processors it has rather than left to guess from the host.

```
docker compose -f docker-compose.prod.yml up -d --build
```

Because the database is inside the image, re-seeding means rebuilding. The container is capped at four CPUs,
matching the other velo-stats backends so the benchmark in `velo-stats-speedtest` compares them on an equal share of
the machine.

## Layout

Code is grouped by domain rather than by kind, so everything one part of the app needs sits together.

```
src/main/java/com/velostats/
  VeloStatsApplication      the single entry point: the API, the workers and every command
  config/                   settings read from the environment, and the beans built from them
  console/                  the command interface, the dispatcher and the commands that are not domain work
  domain/rides/             ride history: the export reader, the list and its derived figures
  domain/stations/          the station feed
  domain/routing/           cycling distances, cached per station pair
  domain/weather/           historical weather, cached per ride
  health/                   the health check
  http/                     cross-origin access
  queue/                    the Redis queue and the worker that drains it
  support/                  rounding, date formatting and the coordinate pair
src/main/resources/db/migration    the schema, as SQL
```

Within a domain:

| Directory | Holds |
|---|---|
| `entity` | the JPA entities and the value types stored inside them |
| `repository` | every query against those entities, and nothing else |
| `service` | the upstream clients, the caches wrapping them, and the read models the endpoints need |
| `web` | one class per endpoint, carrying its own route |
| `dto` | the records an endpoint serialises, one field per published value |
| `message` | the background checks: the job and the handler that runs it |
| `command` | the console commands |

There are no shared controllers. Each endpoint is a single-method class with its mapping on it, so adding an
endpoint means adding one class and changing nothing else.

Background work is pushed onto Redis lists and popped by a worker, one job at a time per worker. Jobs are run once
and are not retried; a failure is logged, and the ride's null `distance_checked_at` or `weather_checked_at` means
the next run of the matching command queues it again.

## Tests

```
mvn verify
```

Two suites. The unit tests cover the pieces where a small mistake changes the API output: rounding, date formatting,
the export parser and the three upstream clients. The integration tests boot the application against a throwaway
SQLite database and cover all five endpoints and both background checks, asserting the exact JSON bodies the Symfony
application returns. Neither touches the network or Redis.

Without a local JDK, the same suite runs in the build image:

```
docker run --rm -v "$PWD":/app -w /app maven:3-eclipse-temurin-25 mvn -B verify
```
