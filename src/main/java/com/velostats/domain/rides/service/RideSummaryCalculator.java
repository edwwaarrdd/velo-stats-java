package com.velostats.domain.rides.service;

import com.velostats.domain.rides.dto.RideSummaryResponse;
import com.velostats.domain.rides.repository.RideRepository;
import com.velostats.domain.rides.repository.RideSummaryStats;
import com.velostats.domain.routing.entity.TravelMode;
import com.velostats.support.Round;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The aggregate view of every ride.
 */
@Service
public class RideSummaryCalculator {

    private final RideRepository rides;

    public RideSummaryCalculator(RideRepository rides) {
        this.rides = rides;
    }

    @Transactional(readOnly = true)
    public RideSummaryResponse calculate() {
        RideSummaryStats stats = rides.summaryStats(TravelMode.BIKE.value());

        return new RideSummaryResponse(
                stats.totalRides(),
                stats.totalDuration(),
                Round.money(stats.averageDuration()),
                stats.longestRideDuration(),
                stats.shortestRideDuration(),
                stats.totalDistanceMeters(),
                Round.money(stats.averageDistanceMeters())
        );
    }
}
