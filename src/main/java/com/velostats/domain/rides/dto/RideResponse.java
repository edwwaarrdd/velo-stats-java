package com.velostats.domain.rides.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.velostats.domain.rides.entity.Ride;
import com.velostats.domain.rides.service.RideWithRoute;
import com.velostats.domain.routing.entity.StationRoute;
import com.velostats.domain.weather.dto.WeatherResponse;
import com.velostats.support.ApiDateTime;
import com.velostats.support.Round;
import java.time.Duration;

@JsonPropertyOrder({
        "ride_id",
        "account_id",
        "status",
        "duration",
        "bike_number",
        "origin_station_code",
        "origin_station",
        "origin_slot_id",
        "checkout_time",
        "destination_station_code",
        "destination_station",
        "destination_slot_id",
        "checkin_time",
        "distance_meters",
        "speed_kmh",
        "expected_duration_seconds",
        "actual_duration_seconds",
        "duration_vs_expected_seconds",
        "weather"
})
public record RideResponse(
        @JsonProperty("ride_id") long rideId,
        @JsonProperty("account_id") long accountId,
        @JsonProperty("status") String status,
        @JsonProperty("duration") int duration,
        @JsonProperty("bike_number") String bikeNumber,
        @JsonProperty("origin_station_code") String originStationCode,
        @JsonProperty("origin_station") String originStation,
        @JsonProperty("origin_slot_id") String originSlotId,
        @JsonProperty("checkout_time") String checkoutTime,
        @JsonProperty("destination_station_code") String destinationStationCode,
        @JsonProperty("destination_station") String destinationStation,
        @JsonProperty("destination_slot_id") String destinationSlotId,
        @JsonProperty("checkin_time") String checkinTime,
        @JsonProperty("distance_meters") Double distanceMeters,
        @JsonProperty("speed_kmh") Double speedKmh,
        @JsonProperty("expected_duration_seconds") Double expectedDurationSeconds,
        @JsonProperty("actual_duration_seconds") Double actualDurationSeconds,
        @JsonProperty("duration_vs_expected_seconds") Double durationVsExpectedSeconds,
        @JsonProperty("weather") WeatherResponse weather
) {

    public static RideResponse from(RideWithRoute rideWithRoute) {
        Ride ride = rideWithRoute.ride();
        StationRoute route = rideWithRoute.route();

        Double distanceMeters = route == null ? null : route.distanceMeters();
        Double expectedDurationSeconds = route == null ? null : route.durationSeconds();
        Double actualDurationSeconds = actualDurationSeconds(ride);

        return new RideResponse(
                ride.rideId(),
                ride.accountId(),
                ride.status(),
                ride.duration(),
                ride.bikeNumber(),
                ride.originStationCode(),
                ride.originStation(),
                ride.originSlotId(),
                ApiDateTime.datetime(ride.checkoutTime()),
                ride.destinationStationCode(),
                ride.destinationStation(),
                ride.destinationSlotId(),
                ApiDateTime.datetime(ride.checkinTime()),
                distanceMeters,
                speedKmh(distanceMeters, actualDurationSeconds),
                Round.money(expectedDurationSeconds),
                actualDurationSeconds,
                durationVsExpectedSeconds(actualDurationSeconds, expectedDurationSeconds),
                WeatherResponse.from(ride.weather())
        );
    }

    /**
     * The stored duration is whole minutes, which is too coarse for anything derived from it.
     */
    private static Double actualDurationSeconds(Ride ride) {
        return Round.money(Duration.between(ride.checkoutTime(), ride.checkinTime()).toSeconds());
    }

    /**
     * Average speed over the ride, using the exact seconds rather than the rounded minutes, which
     * would overstate it.
     */
    private static Double speedKmh(Double distanceMeters, Double seconds) {
        if (distanceMeters == null || seconds == null || seconds <= 0.0) {
            return null;
        }

        return Round.money((distanceMeters / 1000) / (seconds / 3600));
    }

    /**
     * How much longer the ride took than the router predicted. Negative means the rider beat the
     * prediction.
     */
    private static Double durationVsExpectedSeconds(Double actual, Double expected) {
        if (actual == null || expected == null) {
            return null;
        }

        return Round.money(actual - expected);
    }
}
