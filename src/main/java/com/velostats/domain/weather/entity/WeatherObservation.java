package com.velostats.domain.weather.entity;

import java.time.LocalDateTime;

/**
 * One hour of weather at one place, with the upstream's variable names already translated into the
 * names this application uses.
 */
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
