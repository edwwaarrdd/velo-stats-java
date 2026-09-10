package com.velostats.domain.weather.message;

import com.velostats.domain.rides.entity.Ride;
import com.velostats.domain.rides.repository.RideRepository;
import com.velostats.domain.stations.entity.Station;
import com.velostats.domain.stations.repository.StationRepository;
import com.velostats.domain.weather.service.CachedRideWeatherService;
import com.velostats.queue.JobHandler;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CheckRideWeatherHandler implements JobHandler<CheckRideWeather> {

    private static final Logger log = LoggerFactory.getLogger(CheckRideWeatherHandler.class);

    private final RideRepository rides;
    private final StationRepository stations;
    private final CachedRideWeatherService weatherService;

    public CheckRideWeatherHandler(
            RideRepository rides,
            StationRepository stations,
            CachedRideWeatherService weatherService
    ) {
        this.rides = rides;
        this.stations = stations;
        this.weatherService = weatherService;
    }

    @Override
    public Class<CheckRideWeather> handles() {
        return CheckRideWeather.class;
    }

    @Override
    @Transactional
    public void handle(CheckRideWeather job) {
        Ride ride = rides.findById(job.rideId())
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot check weather: ride " + job.rideId() + " does not exist."
                ));

        if (ride.weatherCheckedAt() != null && !job.force()) {
            return;
        }

        // Only the origin station matters: the weather recorded is the weather the rider set off in.
        Optional<Station> origin = stations.findById(ride.originStationCode());

        if (origin.isEmpty()) {
            log.error(
                    "Cannot check weather for ride {}: unknown origin station code {}",
                    job.rideId(),
                    ride.originStationCode()
            );

            return;
        }

        weatherService.getWeather(ride, origin.get().coordinate(), job.force());

        ride.markWeatherChecked();
        rides.save(ride);
    }
}
