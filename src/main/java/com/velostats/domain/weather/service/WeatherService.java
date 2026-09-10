package com.velostats.domain.weather.service;

import com.velostats.domain.weather.entity.WeatherObservation;
import com.velostats.support.Coordinate;
import java.time.LocalDateTime;

public interface WeatherService {

    WeatherObservation getWeather(Coordinate location, LocalDateTime at);
}
