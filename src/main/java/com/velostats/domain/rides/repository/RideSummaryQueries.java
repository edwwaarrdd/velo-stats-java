package com.velostats.domain.rides.repository;

/**
 * The part of the ride repository that is hand-written SQL rather than a derived query.
 */
public interface RideSummaryQueries {

    RideSummaryStats summaryStats(String mode);
}
