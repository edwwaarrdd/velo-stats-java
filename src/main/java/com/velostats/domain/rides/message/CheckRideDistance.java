package com.velostats.domain.rides.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.velostats.queue.Job;
import com.velostats.queue.Queues;

/**
 * Asks for one ride's cycling distance to be looked up and cached.
 */
public record CheckRideDistance(@JsonProperty("ride_id") long rideId) implements Job {

    @Override
    public String queue() {
        return Queues.RIDE_DISTANCE_CHECKS;
    }
}
