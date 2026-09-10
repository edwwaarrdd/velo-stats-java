package com.velostats.domain.rides.repository;

/**
 * The one row of numbers the summary endpoint is built from.
 *
 * <p>Every figure but the count is nullable: with no rides there is nothing to total, and a ride with
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
