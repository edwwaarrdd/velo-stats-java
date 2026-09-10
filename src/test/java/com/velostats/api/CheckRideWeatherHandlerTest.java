package com.velostats.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.velostats.domain.rides.entity.Ride;
import com.velostats.domain.weather.entity.WeatherObservation;
import com.velostats.domain.weather.message.CheckRideWeather;
import com.velostats.domain.weather.message.CheckRideWeatherHandler;
import com.velostats.domain.weather.service.WeatherService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class CheckRideWeatherHandlerTest extends IntegrationTestCase {

    @Autowired
    CheckRideWeatherHandler handler;

    @MockitoBean
    WeatherService weatherService;

    @Test
    void storesTheWeatherAndMarksTheRideChecked() {
        givenStation();
        Ride ride = givenRide();

        given(weatherService.getWeather(any(), any())).willReturn(observation(18.4));

        handler.handle(new CheckRideWeather(ride.rideId(), false));

        // The weather that matters is the weather the rider arrived in.
        verify(weatherService).getWeather(any(), org.mockito.ArgumentMatchers.eq(ride.checkinTime()));
        assertThat(weatherRecords.findAll()).hasSize(1);
        assertThat(rides.findById(ride.rideId()).orElseThrow().weatherCheckedAt()).isNotNull();
    }

    @Test
    void doesNothingForARideThatHasAlreadyBeenChecked() {
        givenStation();
        Ride ride = givenRide();
        ride.markWeatherChecked();
        rides.save(ride);

        handler.handle(new CheckRideWeather(ride.rideId(), false));

        verify(weatherService, never()).getWeather(any(), any());
    }

    /**
     * The force flag exists for the case where the stored observation is wrong rather than stale.
     */
    @Test
    void refetchesACheckedRideWhenForced() {
        givenStation();
        Ride ride = givenRide();
        givenWeather(ride);
        ride.markWeatherChecked();
        rides.save(ride);

        given(weatherService.getWeather(any(), any())).willReturn(observation(21.0));

        handler.handle(new CheckRideWeather(ride.rideId(), true));

        verify(weatherService, times(1)).getWeather(any(), any());
        // One row per ride, so a re-check overwrites rather than accumulating.
        assertThat(weatherRecords.findAll()).hasSize(1);
        assertThat(weatherRecords.findAll().getFirst().temperatureC()).isEqualTo(21.0);
    }

    @Test
    void leavesARideFromAnUnknownStationUnmarked() {
        Ride ride = givenRide(1L, LocalDateTime.of(2026, 9, 6, 8, 57, 2), "999", "998", 8);

        handler.handle(new CheckRideWeather(ride.rideId(), false));

        verify(weatherService, never()).getWeather(any(), any());
        assertThat(rides.findById(ride.rideId()).orElseThrow().weatherCheckedAt()).isNull();
    }

    @Test
    void refusesAJobForARideThatDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new CheckRideWeather(404L, false)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not exist");
    }

    private static WeatherObservation observation(double temperature) {
        return new WeatherObservation(
                temperature,
                17.1,
                0.2,
                0.2,
                0.0,
                75.0,
                14.8,
                31.0,
                210.0,
                72.0,
                3,
                LocalDateTime.of(2026, 9, 6, 9, 0)
        );
    }
}
