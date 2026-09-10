package com.velostats.domain.rides.service;

import com.velostats.domain.rides.entity.Ride;
import com.velostats.domain.rides.repository.RideRepository;
import com.velostats.domain.routing.entity.StationRoute;
import com.velostats.domain.routing.entity.TravelMode;
import com.velostats.domain.routing.repository.StationRouteRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A route belongs to a pair of station codes rather than to a ride, so it cannot be a relation on
 * the entity and cannot be joined as one. Loading the cached routes once and matching them in memory
 * keeps the endpoint to two queries however many rides there are.
 */
@Service
public class RideListProvider {

    private final RideRepository rides;
    private final StationRouteRepository routes;

    public RideListProvider(RideRepository rides, StationRouteRepository routes) {
        this.rides = rides;
        this.routes = routes;
    }

    @Transactional(readOnly = true)
    public List<RideWithRoute> list() {
        Map<String, StationRoute> byStationPair = new HashMap<>();

        for (StationRoute route : routes.findByMode(TravelMode.BIKE)) {
            byStationPair.put(
                    StationRouteRepository.key(route.originStationId(), route.destinationStationId()),
                    route
            );
        }

        return rides.findAllWithWeather().stream()
                .map(ride -> new RideWithRoute(ride, byStationPair.get(keyFor(ride))))
                .toList();
    }

    private static String keyFor(Ride ride) {
        return StationRouteRepository.key(ride.originStationCode(), ride.destinationStationCode());
    }
}
