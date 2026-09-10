package com.velostats.domain.weather.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.velostats.queue.Job;
import com.velostats.queue.Queues;

/**
 * @param force re-fetch even when the ride has already been checked
 */
public record CheckRideWeather(
        @JsonProperty("ride_id") long rideId,
        @JsonProperty("force") boolean force
) implements Job {

    @Override
    public String queue() {
        return Queues.RIDE_WEATHER_CHECKS;
    }
}
