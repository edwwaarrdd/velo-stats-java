package com.velostats.domain.rides.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Totals and averages across the whole ride history.
 *
 * <p>Every figure but the count is nullable, and reports null rather than zero when there is nothing
 * to average: an empty history and a history of zero-length rides are different things.
 */
@JsonPropertyOrder({
        "total_rides",
        "total_duration",
        "average_duration",
        "longest_ride_duration",
        "shortest_ride_duration",
        "total_distance_meters",
        "average_distance_meters"
})
public record RideSummaryResponse(
        @JsonProperty("total_rides") long totalRides,
        @JsonProperty("total_duration") Long totalDuration,
        @JsonProperty("average_duration") Double averageDuration,
        @JsonProperty("longest_ride_duration") Integer longestRideDuration,
        @JsonProperty("shortest_ride_duration") Integer shortestRideDuration,
        @JsonProperty("total_distance_meters") Double totalDistanceMeters,
        @JsonProperty("average_distance_meters") Double averageDistanceMeters
) {
}
