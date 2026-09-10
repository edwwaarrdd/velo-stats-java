package com.velostats.domain.rides.service;

import java.util.Map;

public interface RideDataSource {

    /**
     * Every ride in the export, keyed by ride id. A duplicate id in the source collapses to the last
     * occurrence.
     */
    Map<Long, RideRecord> fetchRides();
}
