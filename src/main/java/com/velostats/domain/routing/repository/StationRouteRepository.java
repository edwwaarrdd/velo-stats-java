package com.velostats.domain.routing.repository;

import com.velostats.domain.routing.entity.StationRoute;
import com.velostats.domain.routing.entity.TravelMode;
import com.velostats.domain.stations.entity.Station;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationRouteRepository extends JpaRepository<StationRoute, Long> {

    Optional<StationRoute> findByOriginStationAndDestinationStationAndMode(
            Station originStation,
            Station destinationStation,
            TravelMode mode
    );

    List<StationRoute> findByMode(TravelMode mode);

    /**
     * The key both sides of a lookup agree on. Routes are directional, so the order of the two codes
     * matters.
     */
    static String key(String originStationId, String destinationStationId) {
        return originStationId + "|" + destinationStationId;
    }
}
