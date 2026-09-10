package com.velostats.queue;

/**
 * The queue names every velo-stats backend uses, so a worker in any of them consumes the same
 * logical queue.
 */
public final class Queues {

    public static final String DEFAULT = "default";

    public static final String RIDE_DISTANCE_CHECKS = "ride_distance_checks";

    public static final String RIDE_WEATHER_CHECKS = "ride_weather_checks";

    private Queues() {
    }
}
