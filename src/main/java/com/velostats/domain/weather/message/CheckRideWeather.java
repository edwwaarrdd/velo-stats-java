package com.velostats.domain.weather.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.velostats.queue.Job;
import com.velostats.queue.Queues;

/**
 * Asks for one ride's weather to be looked up and cached.
 *
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
