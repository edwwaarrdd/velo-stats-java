package com.velostats.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.velostats.domain.rides.entity.Ride;
import com.velostats.domain.rides.message.CheckRideDistance;
import com.velostats.domain.rides.message.CheckRideDistanceHandler;
import com.velostats.domain.routing.entity.Route;
import com.velostats.domain.routing.entity.TravelMode;
import com.velostats.domain.routing.service.RouteService;
import com.velostats.support.Coordinate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class CheckRideDistanceHandlerTest extends IntegrationTestCase {

    @Autowired
    CheckRideDistanceHandler handler;

    @MockitoBean
    RouteService routeService;

    @Test
    void cachesTheRouteAndMarksTheRideChecked() {
        givenStation();
        givenDestinationStation();
        Ride ride = givenRide();

        given(routeService.getRoute(any(), any(), any())).willReturn(new Route(1500.0, 400.0));

        handler.handle(new CheckRideDistance(ride.rideId()));

        verify(routeService).getRoute(
                new Coordinate(51.2189, 4.4131),
                new Coordinate(51.2205, 4.3997),
                TravelMode.BIKE
        );
        assertThat(routes.findAll()).hasSize(1);
        assertThat(rides.findById(ride.rideId()).orElseThrow().distanceCheckedAt()).isNotNull();
    }

    @Test
    void doesNothingForARideThatHasAlreadyBeenChecked() {
        givenStation();
        givenDestinationStation();
        Ride ride = givenRide();
        ride.markDistanceChecked();
        rides.save(ride);

        handler.handle(new CheckRideDistance(ride.rideId()));

        verify(routeService, never()).getRoute(any(), any(), any());
    }

    /**
     * The export contains rides from stations that have since been retired. Leaving the ride unmarked
     * means a later reload of the station feed gives it another chance.
     */
    @Test
    void leavesARideFromAnUnknownStationUnmarked() {
        Ride ride = givenRide(1L, LocalDateTime.of(2026, 9, 6, 8, 57, 2), "999", "998", 8);

        handler.handle(new CheckRideDistance(ride.rideId()));

        verify(routeService, never()).getRoute(any(), any(), any());
        assertThat(rides.findById(ride.rideId()).orElseThrow().distanceCheckedAt()).isNull();
    }

    @Test
    void refusesAJobForARideThatDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new CheckRideDistance(404L)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not exist");
    }
}
