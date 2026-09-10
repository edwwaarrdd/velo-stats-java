package com.velostats.domain.stations.service;

import java.util.List;

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
