package com.velostats.domain.weather.entity;

import com.velostats.domain.rides.entity.Ride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

/**
 * The weather at a ride's origin station when it was returned. One row per ride, enforced by the
 * unique constraint, so the archive is never queried twice for the same ride.
 */
@Entity
@Table(
        name = "weather_records",
        uniqueConstraints = @UniqueConstraint(name = "weather_records_ride_id_unique", columnNames = "ride_id")
)
public class WeatherRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "ride_id", referencedColumnName = "ride_id", nullable = false, unique = true)
    private Ride ride;

    @Column(name = "temperature_c", nullable = false)
    private double temperatureC;

    @Column(name = "apparent_temperature_c", nullable = false)
    private double apparentTemperatureC;

    @Column(name = "precipitation_mm", nullable = false)
    private double precipitationMm;

    @Column(name = "rain_mm", nullable = false)
    private double rainMm;

    @Column(name = "snowfall_cm", nullable = false)
    private double snowfallCm;

    @Column(name = "cloud_cover_percent", nullable = false)
    private double cloudCoverPercent;

    @Column(name = "wind_speed_kmh", nullable = false)
    private double windSpeedKmh;

    @Column(name = "wind_gusts_kmh", nullable = false)
    private double windGustsKmh;

    @Column(name = "wind_direction_degrees", nullable = false)
    private double windDirectionDegrees;

    @Column(name = "relative_humidity_percent", nullable = false)
    private double relativeHumidityPercent;

    /**
     * The WMO weather code describing the conditions.
     */
    @Column(name = "weather_code", nullable = false)
    private int weatherCode;

    @Column(name = "observed_at", nullable = false)
    private LocalDateTime observedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected WeatherRecord() {
    }

    public WeatherRecord(Ride ride, WeatherObservation observation) {
        this.ride = ride;
        fill(observation);
    }

    public void fill(WeatherObservation observation) {
        temperatureC = observation.temperatureC();
        apparentTemperatureC = observation.apparentTemperatureC();
        precipitationMm = observation.precipitationMm();
        rainMm = observation.rainMm();
        snowfallCm = observation.snowfallCm();
        cloudCoverPercent = observation.cloudCoverPercent();
        windSpeedKmh = observation.windSpeedKmh();
        windGustsKmh = observation.windGustsKmh();
        windDirectionDegrees = observation.windDirectionDegrees();
        relativeHumidityPercent = observation.relativeHumidityPercent();
        weatherCode = observation.weatherCode();
        observedAt = observation.observedAt();
    }

    public double temperatureC() {
        return temperatureC;
    }

    public double apparentTemperatureC() {
        return apparentTemperatureC;
    }

    public double precipitationMm() {
        return precipitationMm;
    }

    public double rainMm() {
        return rainMm;
    }

    public double snowfallCm() {
        return snowfallCm;
    }

    public double cloudCoverPercent() {
        return cloudCoverPercent;
    }

    public double windSpeedKmh() {
        return windSpeedKmh;
    }

    public double windGustsKmh() {
        return windGustsKmh;
    }

    public double windDirectionDegrees() {
        return windDirectionDegrees;
    }

    public double relativeHumidityPercent() {
        return relativeHumidityPercent;
    }

    public int weatherCode() {
        return weatherCode;
    }

    public LocalDateTime observedAt() {
        return observedAt;
    }

    public WeatherObservation toObservation() {
        return new WeatherObservation(
                temperatureC,
                apparentTemperatureC,
                precipitationMm,
                rainMm,
                snowfallCm,
                cloudCoverPercent,
                windSpeedKmh,
                windGustsKmh,
                windDirectionDegrees,
                relativeHumidityPercent,
                weatherCode,
                observedAt
        );
    }

    @PrePersist
    void onPrePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onPreUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
