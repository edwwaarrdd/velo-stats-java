package com.velostats.domain.rides.repository;

/**
 * Every figure but the count is nullable: with no rides there is nothing to total, and a ride with
 * no cached route contributes SQL NULL to the two distance figures rather than a zero.
 */
public record RideSummaryStats(
        long totalRides,
        Long totalDuration,
        Double averageDuration,
        Integer longestRideDuration,
        Integer shortestRideDuration,
        Double totalDistanceMeters,
        Double averageDistanceMeters
) {
}
