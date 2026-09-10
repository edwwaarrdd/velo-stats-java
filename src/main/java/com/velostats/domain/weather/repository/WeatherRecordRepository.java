package com.velostats.domain.weather.repository;

import com.velostats.domain.rides.entity.Ride;
import com.velostats.domain.weather.entity.WeatherRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherRecordRepository extends JpaRepository<WeatherRecord, Long> {

    Optional<WeatherRecord> findByRide(Ride ride);
}
