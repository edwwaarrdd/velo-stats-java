package com.velostats.support;

/**
 * A latitude and longitude pair, in that order. The upstream routing API wants them the other way
 * round, which is exactly why they travel together in a named type rather than as two loose doubles.
 */
public record Coordinate(double lat, double lon) {
}
