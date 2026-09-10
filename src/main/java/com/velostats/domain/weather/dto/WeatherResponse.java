package com.velostats.domain.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.velostats.domain.weather.entity.WeatherRecord;
import com.velostats.support.ApiDateTime;

@JsonPropertyOrder({
        "temperature_c",
        "apparent_temperature_c",
        "precipitation_mm",
        "rain_mm",
        "snowfall_cm",
        "cloud_cover_percent",
        "wind_speed_kmh",
        "wind_gusts_kmh",
        "wind_direction_degrees",
        "relative_humidity_percent",
        "weather_code",
        "observed_at"
})
public record WeatherResponse(
        @JsonProperty("temperature_c") double temperatureC,
        @JsonProperty("apparent_temperature_c") double apparentTemperatureC,
        @JsonProperty("precipitation_mm") double precipitationMm,
        @JsonProperty("rain_mm") double rainMm,
        @JsonProperty("snowfall_cm") double snowfallCm,
        @JsonProperty("cloud_cover_percent") double cloudCoverPercent,
        @JsonProperty("wind_speed_kmh") double windSpeedKmh,
        @JsonProperty("wind_gusts_kmh") double windGustsKmh,
        @JsonProperty("wind_direction_degrees") double windDirectionDegrees,
        @JsonProperty("relative_humidity_percent") double relativeHumidityPercent,
        @JsonProperty("weather_code") int weatherCode,
        @JsonProperty("observed_at") String observedAt
) {

    public static WeatherResponse from(WeatherRecord record) {
        if (record == null) {
            return null;
        }

        return new WeatherResponse(
                record.temperatureC(),
                record.apparentTemperatureC(),
                record.precipitationMm(),
                record.rainMm(),
                record.snowfallCm(),
                record.cloudCoverPercent(),
                record.windSpeedKmh(),
                record.windGustsKmh(),
                record.windDirectionDegrees(),
                record.relativeHumidityPercent(),
                record.weatherCode(),
                ApiDateTime.datetime(record.observedAt())
        );
    }
}
