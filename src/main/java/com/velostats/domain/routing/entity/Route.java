package com.velostats.domain.routing.entity;

/**
 * The distance and expected travel time between two points, as a routing service reports them.
 */
public record Route(double distanceMeters, double durationSeconds) {
}
