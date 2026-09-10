package com.velostats.domain.rides.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.velostats.queue.Job;
import com.velostats.queue.Queues;

public record CheckRideDistance(@JsonProperty("ride_id") long rideId) implements Job {

    @Override
    public String queue() {
        return Queues.RIDE_DISTANCE_CHECKS;
    }
}
