package com.velostats.domain.weather.entity;

import java.time.LocalDateTime;

public record WeatherObservation(
        double temperatureC,
        double apparentTemperatureC,
        double precipitationMm,
        double rainMm,
        double snowfallCm,
        double cloudCoverPercent,
        double windSpeedKmh,
        double windGustsKmh,
        double windDirectionDegrees,
        double relativeHumidityPercent,
        int weatherCode,
        LocalDateTime observedAt
) {
}
