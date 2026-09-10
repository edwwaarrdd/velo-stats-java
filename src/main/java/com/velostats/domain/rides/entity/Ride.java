package com.velostats.domain.rides.entity;

import com.velostats.domain.weather.entity.WeatherRecord;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * The station columns hold codes rather than a relation, and there is deliberately no foreign key
 * to stations: the export contains rides from stations that have since been retired, and those rides
 * still have to load. The background checks treat an unknown code as a logged non-event.
 */
@Entity
@Table(name = "rides", indexes = @Index(name = "rides_checkout_time_index", columnList = "checkout_time"))
public class Ride {

    /**
     * The identifier the operator assigned, not one of ours.
     */
    @Id
    @Column(name = "ride_id", nullable = false)
    private long rideId;

    @Column(name = "account_id", nullable = false)
    private long accountId;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    /**
     * Whole minutes, as exported. Anything needing precision recomputes from the check-out and
     * check-in timestamps instead.
     */
    @Column(name = "duration", nullable = false)
    private int duration;

    @Column(name = "bike_number", length = 32, nullable = false)
    private String bikeNumber;

    @Column(name = "origin_station_code", length = 32, nullable = false)
    private String originStationCode;

    @Column(name = "origin_station", nullable = false)
    private String originStation;

    @Column(name = "origin_slot_id", length = 16, nullable = false)
    private String originSlotId;

    @Column(name = "checkout_time", nullable = false)
    private LocalDateTime checkoutTime;

    @Column(name = "destination_station_code", length = 32, nullable = false)
    private String destinationStationCode;

    @Column(name = "destination_station", nullable = false)
    private String destinationStation;

    @Column(name = "destination_slot_id", length = 16, nullable = false)
    private String destinationSlotId;

    @Column(name = "checkin_time", nullable = false)
    private LocalDateTime checkinTime;

    /**
     * Set once the distance check has run. Leaving it null is how a failed check asks to be retried by
     * the next command run.
     */
    @Column(name = "distance_checked_at")
    private LocalDateTime distanceCheckedAt;

    @Column(name = "weather_checked_at")
    private LocalDateTime weatherCheckedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * The weather at the origin station when this ride ended, once it has been checked. Mapped as the
     * inverse side so the list query can fetch it alongside the ride rather than one query per row.
     */
    @OneToOne(mappedBy = "ride", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private WeatherRecord weather;

    protected Ride() {
    }

    public Ride(
            long rideId,
            long accountId,
            String status,
            int duration,
            String bikeNumber,
            String originStationCode,
            String originStation,
            String originSlotId,
            LocalDateTime checkoutTime,
            String destinationStationCode,
            String destinationStation,
            String destinationSlotId,
            LocalDateTime checkinTime
    ) {
        this.rideId = rideId;
        fill(
                accountId,
                status,
                duration,
                bikeNumber,
                originStationCode,
                originStation,
                originSlotId,
                checkoutTime,
                destinationStationCode,
                destinationStation,
                destinationSlotId,
                checkinTime
        );
    }

    /**
     * Overwrite everything the export carries, leaving the two check timestamps alone so re-importing
     * does not queue work that has already been done.
     */
    public void fill(
            long accountId,
            String status,
            int duration,
            String bikeNumber,
            String originStationCode,
            String originStation,
            String originSlotId,
            LocalDateTime checkoutTime,
            String destinationStationCode,
            String destinationStation,
            String destinationSlotId,
            LocalDateTime checkinTime
    ) {
        this.accountId = accountId;
        this.status = status;
        this.duration = duration;
        this.bikeNumber = bikeNumber;
        this.originStationCode = originStationCode;
        this.originStation = originStation;
        this.originSlotId = originSlotId;
        this.checkoutTime = checkoutTime;
        this.destinationStationCode = destinationStationCode;
        this.destinationStation = destinationStation;
        this.destinationSlotId = destinationSlotId;
        this.checkinTime = checkinTime;
    }

    public long rideId() {
        return rideId;
    }

    public long accountId() {
        return accountId;
    }

    public String status() {
        return status;
    }

    public int duration() {
        return duration;
    }

    public String bikeNumber() {
        return bikeNumber;
    }

    public String originStationCode() {
        return originStationCode;
    }

    public String originStation() {
        return originStation;
    }

    public String originSlotId() {
        return originSlotId;
    }

    public LocalDateTime checkoutTime() {
        return checkoutTime;
    }

    public String destinationStationCode() {
        return destinationStationCode;
    }

    public String destinationStation() {
        return destinationStation;
    }

    public String destinationSlotId() {
        return destinationSlotId;
    }

    public LocalDateTime checkinTime() {
        return checkinTime;
    }

    public WeatherRecord weather() {
        return weather;
    }

    public LocalDateTime distanceCheckedAt() {
        return distanceCheckedAt;
    }

    public LocalDateTime weatherCheckedAt() {
        return weatherCheckedAt;
    }

    public void markDistanceChecked() {
        distanceCheckedAt = LocalDateTime.now();
    }

    public void markWeatherChecked() {
        weatherCheckedAt = LocalDateTime.now();
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
