package com.velostats.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.velostats.domain.rides.entity.Ride;
import com.velostats.domain.routing.entity.TravelMode;
import com.velostats.domain.stations.entity.Station;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class ListRidesTest extends IntegrationTestCase {

    @Test
    void returnsAnEmptyListWhenThereAreNoRides() {
        assertThat(getJson("/rides")).isEqualTo("{\"results\":[]}");
    }

    @Test
    void returnsARideWithItsRouteAndWeather() {
        Station origin = givenStation();
        Station destination = givenDestinationStation();
        givenRoute(origin, destination);
        givenWeather(givenRide());

        assertThat(getJson("/rides")).contains(
                "\"ride_id\":73147208",
                "\"checkout_time\":\"2026-09-06T08:57:02Z\"",
                "\"distance_meters\":1500.0",
                "\"expected_duration_seconds\":400.0",
                "\"actual_duration_seconds\":508.0",
                "\"duration_vs_expected_seconds\":108.0",
                // Distance over the exact seconds, not over the rounded minutes, which would overstate it.
                "\"speed_kmh\":10.63",
                "\"weather_code\":3"
        );
    }

    /**
     * A whole-numbered distance has to stay a float in the JSON, or a client sees a field's type
     * change with its value.
     */
    @Test
    void keepsTheFractionOnWholeNumberedFigures() {
        Station origin = givenStation();
        givenRoute(origin, givenDestinationStation());
        givenRide();

        assertThat(getJson("/rides")).contains("\"distance_meters\":1500.0", "\"actual_duration_seconds\":508.0");
    }

    /**
     * The route lookup is directional and bike-only, so neither the reverse pair nor another travel
     * mode may be picked up by mistake.
     */
    @Test
    void ignoresARouteForTheReversePair() {
        Station origin = givenStation();
        Station destination = givenDestinationStation();
        givenRoute(destination, origin);
        givenRide();

        assertThat(getJson("/rides")).contains("\"distance_meters\":null", "\"speed_kmh\":null");
    }

    @Test
    void ignoresARouteForAnotherTravelMode() {
        Station origin = givenStation();
        givenRoute(origin, givenDestinationStation(), TravelMode.FOOT, 1500.0, 400.0);
        givenRide();

        assertThat(getJson("/rides")).contains("\"distance_meters\":null");
    }

    @Test
    void reportsNullWeatherForARideThatHasNone() {
        givenRide();

        assertThat(getJson("/rides")).contains("\"weather\":null");
    }

    @Test
    void ordersNewestFirstAndBreaksTiesByRideId() {
        givenRide(1L, LocalDateTime.of(2025, 3, 12, 15, 30, 17));
        givenRide(2L, LocalDateTime.of(2026, 9, 6, 8, 57, 2));
        givenRide(3L, LocalDateTime.of(2026, 9, 6, 8, 57, 2));

        // Newest first, and the two rides sharing a check-out time ordered by descending id.
        assertThat(rideIdsInOrder()).containsExactly(3L, 2L, 1L);
    }

    private List<Long> rideIdsInOrder() {
        Matcher matcher = Pattern.compile("\"ride_id\":(\\d+)").matcher(getJson("/rides"));
        List<Long> ids = new java.util.ArrayList<>();

        while (matcher.find()) {
            ids.add(Long.parseLong(matcher.group(1)));
        }

        return ids;
    }

    /**
     * A ride from a station that has since been retired still has to appear, which is why the station
     * columns are codes rather than references.
     */
    @Test
    void stillReturnsARideWhoseStationsAreUnknown() {
        Ride ride = givenRide(1L, LocalDateTime.of(2026, 9, 6, 8, 57, 2), "999", "998", 8);

        assertThat(ride.originStationCode()).isEqualTo("999");
        assertThat(getJson("/rides")).contains("\"origin_station_code\":\"999\"", "\"distance_meters\":null");
    }
}
