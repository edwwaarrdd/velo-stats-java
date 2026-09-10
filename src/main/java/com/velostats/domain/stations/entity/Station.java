package com.velostats.domain.stations.entity;

import com.velostats.support.Coordinate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A Velo Antwerp docking station, as published by the operator's GBFS feed. The station code the feed
 * uses is the primary key, because rides reference stations by that code and nothing else.
 */
@Entity
@Table(name = "stations")
public class Station {

    @Id
    @Column(name = "station_id", length = 32, nullable = false)
    private String stationId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "short_name", length = 32, nullable = false)
    private String shortName;

    @Column(name = "lat", nullable = false)
    private double lat;

    @Column(name = "lon", nullable = false)
    private double lon;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "post_code", length = 16, nullable = false)
    private String postCode;

    /**
     * Stored as a JSON array of strings, matching the column the other backends write.
     */
    @Column(name = "rental_methods", nullable = false)
    private String rentalMethods;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Station() {
    }

    public Station(
            String stationId,
            String name,
            String shortName,
            double lat,
            double lon,
            String address,
            String postCode,
            List<String> rentalMethods,
            int capacity
    ) {
        this.stationId = stationId;
        fill(name, shortName, lat, lon, address, postCode, rentalMethods, capacity);
    }

    /**
     * Overwrite everything except the identifier, which is how a reload of the upstream feed updates a
     * station that already exists.
     */
    public void fill(
            String name,
            String shortName,
            double lat,
            double lon,
            String address,
            String postCode,
            List<String> rentalMethods,
            int capacity
    ) {
        this.name = name;
        this.shortName = shortName;
        this.lat = lat;
        this.lon = lon;
        this.address = address;
        this.postCode = postCode;
        this.rentalMethods = RentalMethods.encode(rentalMethods);
        this.capacity = capacity;
    }

    public String stationId() {
        return stationId;
    }

    public String name() {
        return name;
    }

    public String shortName() {
        return shortName;
    }

    public double lat() {
        return lat;
    }

    public double lon() {
        return lon;
    }

    public String address() {
        return address;
    }

    public String postCode() {
        return postCode;
    }

    public List<String> rentalMethods() {
        return RentalMethods.decode(rentalMethods);
    }

    public int capacity() {
        return capacity;
    }

    public Coordinate coordinate() {
        return new Coordinate(lat, lon);
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
