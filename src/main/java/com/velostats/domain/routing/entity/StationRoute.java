package com.velostats.domain.routing.entity;

import com.velostats.domain.stations.entity.Station;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

/**
 * A cached route between two stations for one travel mode.
 *
 * <p>The upstream routing service is free and rate-limited, and the answer for a pair of fixed
 * docking stations never changes, so every lookup is stored here and never asked for twice. The
 * unique constraint is the cache key.
 */
@Entity
@Table(
        name = "station_routes",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_station_route_per_mode",
                columnNames = {"origin_station_id", "destination_station_id", "mode"}
        )
)
public class StationRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_station_id", referencedColumnName = "station_id", nullable = false)
    private Station originStation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_station_id", referencedColumnName = "station_id", nullable = false)
    private Station destinationStation;

    /**
     * The two foreign keys again, read-only, so the list endpoint can index cached routes by their
     * station pair without initialising a lazy proxy per row.
     */
    @Column(name = "origin_station_id", insertable = false, updatable = false)
    private String originStationId;

    @Column(name = "destination_station_id", insertable = false, updatable = false)
    private String destinationStationId;

    @Column(name = "mode", length = 8, nullable = false)
    private TravelMode mode;

    @Column(name = "distance_meters", nullable = false)
    private double distanceMeters;

    @Column(name = "duration_seconds", nullable = false)
    private double durationSeconds;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected StationRoute() {
    }

    public StationRoute(
            Station originStation,
            Station destinationStation,
            TravelMode mode,
            double distanceMeters,
            double durationSeconds
    ) {
        this.originStation = originStation;
        this.destinationStation = destinationStation;
        this.originStationId = originStation.stationId();
        this.destinationStationId = destinationStation.stationId();
        this.mode = mode;
        this.distanceMeters = distanceMeters;
        this.durationSeconds = durationSeconds;
    }

    public String originStationId() {
        return originStationId;
    }

    public String destinationStationId() {
        return destinationStationId;
    }

    public double distanceMeters() {
        return distanceMeters;
    }

    public double durationSeconds() {
        return durationSeconds;
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
