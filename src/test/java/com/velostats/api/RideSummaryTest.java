package com.velostats.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.velostats.domain.stations.entity.Station;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class RideSummaryTest extends IntegrationTestCase {

    private static final LocalDateTime CHECKOUT = LocalDateTime.of(2026, 9, 6, 8, 57, 2);

    /**
     * An empty history and a history of zero-length rides are different things, so every figure but
     * the count reports null rather than zero.
     */
    @Test
    void reportsNullsRatherThanZerosWhenThereAreNoRides() {
        assertThat(getJson("/rides/summary")).isEqualTo(
                "{\"total_rides\":0,\"total_duration\":null,\"average_duration\":null,"
                        + "\"longest_ride_duration\":null,\"shortest_ride_duration\":null,"
                        + "\"total_distance_meters\":null,\"average_distance_meters\":null}"
        );
    }

    @Test
    void aggregatesDurationsAcrossEveryRide() {
        givenRide(1L, CHECKOUT, ORIGIN_CODE, DESTINATION_CODE, 10);
        givenRide(2L, CHECKOUT, ORIGIN_CODE, DESTINATION_CODE, 20);
        givenRide(3L, CHECKOUT, ORIGIN_CODE, DESTINATION_CODE, 15);

        assertThat(getJson("/rides/summary")).contains(
                "\"total_rides\":3",
                "\"total_duration\":45",
                "\"average_duration\":15.0",
                "\"longest_ride_duration\":20",
                "\"shortest_ride_duration\":10"
        );
    }

    /**
     * A ride with no cached route contributes nothing to the distance figures rather than dragging the
     * average towards zero.
     */
    @Test
    void averagesDistanceOverOnlyTheRidesThatHaveARoute() {
        Station origin = givenStation();
        givenRoute(origin, givenDestinationStation(), com.velostats.domain.routing.entity.TravelMode.BIKE, 2000.0, 400.0);

        givenRide(1L, CHECKOUT);
        givenRide(2L, CHECKOUT);
        givenRide(3L, CHECKOUT, "999", DESTINATION_CODE, 8);

        assertThat(getJson("/rides/summary")).contains(
                "\"total_rides\":3",
                "\"total_distance_meters\":4000.0",
                "\"average_distance_meters\":2000.0"
        );
    }
}
