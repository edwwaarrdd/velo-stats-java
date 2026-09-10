package com.velostats.domain.weather.service;

import com.velostats.config.AppProperties;
import com.velostats.domain.weather.entity.WeatherObservation;
import com.velostats.support.Coordinate;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

@Service
public class OpenMeteoWeatherService implements WeatherService {

    public static final List<String> HOURLY_VARIABLES = List.of(
            "temperature_2m",
            "apparent_temperature",
            "precipitation",
            "rain",
            "snowfall",
            "cloud_cover",
            "wind_speed_10m",
            "wind_gusts_10m",
            "wind_direction_10m",
            "relative_humidity_2m",
            "weather_code"
    );

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:00");

    private final RestClient http;
    private final String archiveUrl;

    public OpenMeteoWeatherService(RestClient upstreamRestClient, AppProperties properties) {
        this.http = upstreamRestClient;
        this.archiveUrl = properties.upstream().openMeteoArchiveUrl();
    }

    @Override
    public WeatherObservation getWeather(Coordinate location, LocalDateTime at) {
        String date = DATE.format(at);

        // The archive is queried a whole day at a time and the hour is picked out below, because it
        // has no endpoint for a single hour.
        URI uri = UriComponentsBuilder.fromUriString(archiveUrl)
                .queryParam("latitude", location.lat())
                .queryParam("longitude", location.lon())
                .queryParam("start_date", date)
                .queryParam("end_date", date)
                .queryParam("hourly", String.join(",", HOURLY_VARIABLES))
                .queryParam("timezone", "UTC")
                .build()
                .toUri();

        JsonNode payload = http.get().uri(uri).retrieve().body(JsonNode.class);
        JsonNode hourly = payload.get("hourly");

        if (hourly == null || hourly.isNull()) {
            throw new IllegalStateException(
                    "Open-Meteo request failed: " + payload.path("reason").asString(payload.toString())
            );
        }

        String targetHour = HOUR.format(at);
        int index = indexOfHour(hourly.path("time"), targetHour);

        if (index < 0) {
            throw new IllegalStateException("Open-Meteo response has no observation for " + targetHour + ".");
        }

        return fromOpenMeteoHourly(hourly, index);
    }

    private static int indexOfHour(JsonNode times, String targetHour) {
        for (int index = 0; index < times.size(); index++) {
            if (targetHour.equals(times.path(index).asString(null))) {
                return index;
            }
        }

        return -1;
    }

    /**
     * Open-Meteo answers in columns rather than rows: {@code time} is a list of hours, and every
     * variable is a parallel list. One index therefore selects one hour across all of them.
     */
    static WeatherObservation fromOpenMeteoHourly(JsonNode hourly, int index) {
        return new WeatherObservation(
                hourly.path("temperature_2m").path(index).asDouble(),
                hourly.path("apparent_temperature").path(index).asDouble(),
                hourly.path("precipitation").path(index).asDouble(),
                hourly.path("rain").path(index).asDouble(),
                hourly.path("snowfall").path(index).asDouble(),
                hourly.path("cloud_cover").path(index).asDouble(),
                hourly.path("wind_speed_10m").path(index).asDouble(),
                hourly.path("wind_gusts_10m").path(index).asDouble(),
                hourly.path("wind_direction_10m").path(index).asDouble(),
                hourly.path("relative_humidity_2m").path(index).asDouble(),
                hourly.path("weather_code").path(index).asInt(),
                LocalDateTime.parse(hourly.path("time").path(index).asString())
        );
    }
}
