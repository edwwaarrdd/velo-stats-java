package com.velostats.domain.rides.service;

import com.velostats.domain.rides.entity.Ride;
import com.velostats.domain.routing.entity.StationRoute;

/**
 * The route hangs off the pair of station codes rather than off the ride, so it cannot be a
 * relation on the entity. Pairing them here keeps the list query to two round trips instead of one
 * lookup per ride.
 */
public record RideWithRoute(Ride ride, StationRoute route) {
}
