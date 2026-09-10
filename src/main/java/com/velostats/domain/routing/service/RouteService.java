package com.velostats.domain.routing.service;

import com.velostats.domain.routing.entity.Route;
import com.velostats.domain.routing.entity.TravelMode;
import com.velostats.support.Coordinate;

public interface RouteService {

    Route getRoute(Coordinate origin, Coordinate destination, TravelMode mode);
}
