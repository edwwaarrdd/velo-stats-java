package com.velostats.domain.stations.service;

import java.util.List;

/**
 * One station as the upstream feed describes it, with the fields this application stores and nothing
 * else.
 */
public record StationInformation(
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
}
