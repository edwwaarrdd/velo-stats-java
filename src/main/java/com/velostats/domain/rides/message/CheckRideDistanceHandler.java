package com.velostats.domain.rides.message;

import com.velostats.domain.rides.entity.Ride;
import com.velostats.domain.rides.repository.RideRepository;
import com.velostats.domain.routing.entity.TravelMode;
import com.velostats.domain.routing.service.CachedStationRouteService;
import com.velostats.domain.stations.entity.Station;
import com.velostats.domain.stations.repository.StationRepository;
import com.velostats.queue.JobHandler;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CheckRideDistanceHandler implements JobHandler<CheckRideDistance> {

    private static final Logger log = LoggerFactory.getLogger(CheckRideDistanceHandler.class);

    private final RideRepository rides;
    private final StationRepository stations;
    private final CachedStationRouteService routeService;

    public CheckRideDistanceHandler(
            RideRepository rides,
            StationRepository stations,
            CachedStationRouteService routeService
    ) {
        this.rides = rides;
        this.stations = stations;
        this.routeService = routeService;
    }

    @Override
    public Class<CheckRideDistance> handles() {
        return CheckRideDistance.class;
    }

    @Override
    @Transactional
    public void handle(CheckRideDistance job) {
        Ride ride = rides.findById(job.rideId())
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot check distance: ride " + job.rideId() + " does not exist."
                ));

        if (ride.distanceCheckedAt() != null) {
            return;
        }

        Optional<Station> origin = stations.findById(ride.originStationCode());
        Optional<Station> destination = stations.findById(ride.destinationStationCode());

        // The export contains rides from stations that have since been retired. Leaving the ride
        // unmarked means a later reload of the station feed gives it another chance.
        if (origin.isEmpty() || destination.isEmpty()) {
            log.error(
                    "Cannot check distance for ride {}: unknown station code(s) {} / {}",
                    job.rideId(),
                    ride.originStationCode(),
                    ride.destinationStationCode()
            );

            return;
        }

        routeService.getRoute(origin.get(), destination.get(), TravelMode.BIKE);

        ride.markDistanceChecked();
        rides.save(ride);
    }
}
