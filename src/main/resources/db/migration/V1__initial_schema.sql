-- The initial schema: stations, rides, cached routes and weather.
--
-- Hand-written rather than generated from the entities, because SQLite cannot add a foreign key
-- after the fact: every constraint has to be declared inside its CREATE TABLE. It is also the same
-- schema the other velo-stats backends create, so a database seeded by any of them is readable by
-- all of them.

CREATE TABLE stations (
    station_id VARCHAR(32) NOT NULL,
    name VARCHAR(255) NOT NULL,
    short_name VARCHAR(32) NOT NULL,
    lat DOUBLE PRECISION NOT NULL,
    lon DOUBLE PRECISION NOT NULL,
    address VARCHAR(255) NOT NULL,
    post_code VARCHAR(16) NOT NULL,
    rental_methods CLOB NOT NULL,
    capacity INTEGER DEFAULT 0 NOT NULL,
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    PRIMARY KEY (station_id)
);

-- The station columns hold codes rather than references on purpose: the export contains rides from
-- stations that have since been retired, and those rides still have to load.
CREATE TABLE rides (
    ride_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    duration INTEGER NOT NULL,
    bike_number VARCHAR(32) NOT NULL,
    origin_station_code VARCHAR(32) NOT NULL,
    origin_station VARCHAR(255) NOT NULL,
    origin_slot_id VARCHAR(16) NOT NULL,
    checkout_time DATETIME NOT NULL,
    destination_station_code VARCHAR(32) NOT NULL,
    destination_station VARCHAR(255) NOT NULL,
    destination_slot_id VARCHAR(16) NOT NULL,
    checkin_time DATETIME NOT NULL,
    distance_checked_at DATETIME DEFAULT NULL,
    weather_checked_at DATETIME DEFAULT NULL,
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    PRIMARY KEY (ride_id)
);

CREATE INDEX rides_checkout_time_index ON rides (checkout_time);

CREATE TABLE station_routes (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    origin_station_id VARCHAR(32) NOT NULL,
    destination_station_id VARCHAR(32) NOT NULL,
    mode VARCHAR(8) NOT NULL,
    distance_meters DOUBLE PRECISION NOT NULL,
    duration_seconds DOUBLE PRECISION NOT NULL,
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    FOREIGN KEY (origin_station_id) REFERENCES stations (station_id) ON UPDATE NO ACTION ON DELETE CASCADE NOT DEFERRABLE INITIALLY IMMEDIATE,
    FOREIGN KEY (destination_station_id) REFERENCES stations (station_id) ON UPDATE NO ACTION ON DELETE CASCADE NOT DEFERRABLE INITIALLY IMMEDIATE
);

-- The cache key: one answer per station pair per travel mode.
CREATE UNIQUE INDEX unique_station_route_per_mode
    ON station_routes (origin_station_id, destination_station_id, mode);

CREATE TABLE weather_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    ride_id BIGINT NOT NULL,
    temperature_c DOUBLE PRECISION NOT NULL,
    apparent_temperature_c DOUBLE PRECISION NOT NULL,
    precipitation_mm DOUBLE PRECISION NOT NULL,
    rain_mm DOUBLE PRECISION NOT NULL,
    snowfall_cm DOUBLE PRECISION NOT NULL,
    cloud_cover_percent DOUBLE PRECISION NOT NULL,
    wind_speed_kmh DOUBLE PRECISION NOT NULL,
    wind_gusts_kmh DOUBLE PRECISION NOT NULL,
    wind_direction_degrees DOUBLE PRECISION NOT NULL,
    relative_humidity_percent DOUBLE PRECISION NOT NULL,
    weather_code INTEGER NOT NULL,
    observed_at DATETIME NOT NULL,
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    FOREIGN KEY (ride_id) REFERENCES rides (ride_id) ON UPDATE NO ACTION ON DELETE CASCADE NOT DEFERRABLE INITIALLY IMMEDIATE
);

CREATE UNIQUE INDEX weather_records_ride_id_unique ON weather_records (ride_id);
