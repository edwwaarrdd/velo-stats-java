package com.velostats.domain.rides.service;

import java.time.LocalDateTime;

/**
 * One ride as the export describes it. The export uses camelCase keys and naive timestamps; the
 * reader is where both are translated, once.
 */
public record RideRecord(
        long rideId,
        long accountId,
        String status,
        int duration,
        String bikeNumber,
        String originStationCode,
        String originStation,
        String originSlotId,
        LocalDateTime checkoutTime,
        String destinationStationCode,
        String destinationStation,
        String destinationSlotId,
        LocalDateTime checkinTime
) {
}
