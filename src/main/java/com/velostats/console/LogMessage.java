package com.velostats.console;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.velostats.queue.Job;
import com.velostats.queue.Queues;

/**
 * A job that does nothing but log, so the queue setup can be verified end to end.
 */
public record LogMessage(@JsonProperty("message") String message) implements Job {

    @Override
    public String queue() {
        return Queues.DEFAULT;
    }
}
