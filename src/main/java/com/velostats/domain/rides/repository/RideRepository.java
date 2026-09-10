package com.velostats.domain.rides.repository;

import com.velostats.domain.rides.entity.Ride;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Every query against rides lives here.
 *
 * <p>The list endpoint hydrates entities and hands them to the response mapper, which is what the
 * other ports do and what makes them comparable. The summary is an aggregate returning one row of
 * numbers, so there is nothing to hydrate and it stays a plain query.
 */
public interface RideRepository extends JpaRepository<Ride, Long>, RideSummaryQueries {

    /**
     * Ride ids still awaiting a distance check.
     */
    @Query("SELECT r.rideId FROM Ride r WHERE r.distanceCheckedAt IS NULL ORDER BY r.rideId")
    List<Long> idsAwaitingDistanceCheck();

    /**
     * Ride ids still awaiting a weather check.
     */
    @Query("SELECT r.rideId FROM Ride r WHERE r.weatherCheckedAt IS NULL ORDER BY r.rideId")
    List<Long> idsAwaitingWeatherCheck();

    /**
     * Every ride id, for a forced weather re-check.
     */
    @Query("SELECT r.rideId FROM Ride r ORDER BY r.rideId")
    List<Long> allIds();

    /**
     * Every ride, newest first, with its weather already loaded.
     *
     * <p>The weather comes back in the same query rather than one lookup per ride, which is the only
     * thing that could turn this into an N+1.
     */
    @Query("SELECT r FROM Ride r LEFT JOIN FETCH r.weather ORDER BY r.checkoutTime DESC, r.rideId DESC")
    List<Ride> findAllWithWeather();

    /**
     * Every ride's check-out time, which is all the cost calculation needs.
     */
    @Query("SELECT r.checkoutTime FROM Ride r")
    List<LocalDateTime> allCheckoutTimes();
}
