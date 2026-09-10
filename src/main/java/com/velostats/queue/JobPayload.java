package com.velostats.queue;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The envelope a job travels in: the job's class name alongside its own JSON.
 *
 * @param type the fully qualified class name of the job
 * @param body the job serialised as JSON
 */
public record JobPayload(@JsonProperty("type") String type, @JsonProperty("body") String body) {
}
