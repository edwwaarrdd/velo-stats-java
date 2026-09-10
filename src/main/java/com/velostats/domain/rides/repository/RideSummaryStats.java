package com.velostats.domain.rides.repository;

/**
 * The one row of numbers the summary endpoint is built from.
 *
 * <p>An interface projection rather than a record, because the query is native and the driver maps
 * columns onto these getters by their alias.
 *
 * <p>Every figure but the count is nullable: with no rides there is nothing to total, and a ride with
 * no cached route contributes SQL NULL to the two distance figures rather than a zero.
 */
public interface RideSummaryStats {

    long getTotalRides();

    Long getTotalDuration();

    Double getAverageDuration();

    Integer getLongestRideDuration();

    Integer getShortestRideDuration();

    Double getTotalDistanceMeters();

    Double getAverageDistanceMeters();
}
