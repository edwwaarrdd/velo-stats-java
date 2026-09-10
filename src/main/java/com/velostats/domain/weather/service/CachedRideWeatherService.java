package com.velostats.domain.weather.service;

import com.velostats.domain.rides.entity.Ride;
import com.velostats.domain.weather.entity.WeatherObservation;
import com.velostats.domain.weather.entity.WeatherRecord;
import com.velostats.domain.weather.repository.WeatherRecordRepository;
import com.velostats.support.Coordinate;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Historical weather never changes, so a ride is looked up once. The force flag exists for the case
 * where the stored observation is wrong rather than stale.
 */
@Service
public class CachedRideWeatherService {

    private final WeatherService weatherService;
    private final WeatherRecordRepository records;

    public CachedRideWeatherService(WeatherService weatherService, WeatherRecordRepository records) {
        this.weatherService = weatherService;
        this.records = records;
    }

    @Transactional
    public WeatherObservation getWeather(Ride ride, Coordinate location, boolean force) {
        Optional<WeatherRecord> cached = records.findByRide(ride);

        if (!force && cached.isPresent()) {
            return cached.get().toObservation();
        }

        // The weather that matters is the weather the rider arrived in, at the station they set off
        // from.
        WeatherObservation observation = weatherService.getWeather(location, ride.checkinTime());

        // One row per ride, so a re-check overwrites rather than accumulating.
        cached.ifPresentOrElse(
                record -> record.fill(observation),
                () -> records.save(new WeatherRecord(ride, observation))
        );

        return observation;
    }
}
