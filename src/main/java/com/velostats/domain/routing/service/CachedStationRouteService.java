package com.velostats.domain.routing.service;

import com.velostats.domain.routing.entity.Route;
import com.velostats.domain.routing.entity.StationRoute;
import com.velostats.domain.routing.entity.TravelMode;
import com.velostats.domain.routing.repository.StationRouteRepository;
import com.velostats.domain.stations.entity.Station;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Docking stations do not move, so the route between any two of them is answered once and stored
 * forever. This is what keeps the free upstream service usable at all.
 */
@Service
public class CachedStationRouteService {

    private final RouteService routeService;
    private final StationRouteRepository routes;

    public CachedStationRouteService(RouteService routeService, StationRouteRepository routes) {
        this.routeService = routeService;
        this.routes = routes;
    }

    @Transactional
    public Route getRoute(Station origin, Station destination, TravelMode mode) {
        return routes.findByOriginStationAndDestinationStationAndMode(origin, destination, mode)
                .map(cached -> new Route(cached.distanceMeters(), cached.durationSeconds()))
                .orElseGet(() -> fetchAndStore(origin, destination, mode));
    }

    private Route fetchAndStore(Station origin, Station destination, TravelMode mode) {
        Route route = routeService.getRoute(origin.coordinate(), destination.coordinate(), mode);

        routes.save(new StationRoute(
                origin,
                destination,
                mode,
                route.distanceMeters(),
                route.durationSeconds()
        ));

        return route;
    }
}
