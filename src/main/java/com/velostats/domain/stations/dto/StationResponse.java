package com.velostats.domain.stations.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.velostats.domain.stations.entity.Station;

/**
 * A station as the API publishes it: only what a map needs. The address, capacity and rental methods
 * are stored but not exposed.
 */
@JsonPropertyOrder({"station_id", "name", "lat", "lon"})
public record StationResponse(
        @JsonProperty("station_id") String stationId,
        @JsonProperty("name") String name,
        @JsonProperty("lat") double lat,
        @JsonProperty("lon") double lon
) {

    public static StationResponse from(Station station) {
        return new StationResponse(station.stationId(), station.name(), station.lat(), station.lon());
    }
}
