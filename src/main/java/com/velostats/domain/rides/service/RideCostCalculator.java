package com.velostats.domain.rides.service;

import com.velostats.domain.rides.dto.RideCostResponse;
import com.velostats.domain.rides.repository.RideRepository;
import com.velostats.support.ApiDateTime;
import com.velostats.support.Round;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RideCostCalculator {

    public static final double ANNUAL_SUBSCRIPTION_PRICE_EUR = 58.0;

    public static final int DAYS_PER_YEAR = 365;

    public static final double DAY_PASS_PRICE_EUR = 5.0;

    public static final double WEEK_PASS_PRICE_EUR = 12.0;

    private final RideRepository rides;

    public RideCostCalculator(RideRepository rides) {
        this.rides = rides;
    }

    @Transactional(readOnly = true)
    public RideCostResponse calculate() {
        List<LocalDateTime> checkoutTimes = rides.allCheckoutTimes();
        int totalRides = checkoutTimes.size();

        if (totalRides == 0) {
            return emptySummary();
        }

        LocalDateTime firstRide = checkoutTimes.stream().min(LocalDateTime::compareTo).orElseThrow();
        LocalDateTime lastRide = checkoutTimes.stream().max(LocalDateTime::compareTo).orElseThrow();

        // Inclusive of both end days, so a single-day history is one day long rather than zero.
        int dateRangeDays = (int) ChronoUnit.DAYS.between(firstRide.toLocalDate(), lastRide.toLocalDate()) + 1;

        Double proratedSubscriptionPrice = Round.money(
                ANNUAL_SUBSCRIPTION_PRICE_EUR * dateRangeDays / DAYS_PER_YEAR
        );

        // Deliberately divides the rounded price rather than the exact one, so the per-ride figure is
        // consistent with the total shown beside it.
        Double costPerRide = Round.money(proratedSubscriptionPrice / totalRides);

        Set<LocalDate> rideDays = new HashSet<>();
        Set<String> rideWeeks = new HashSet<>();

        for (LocalDateTime checkoutTime : checkoutTimes) {
            rideDays.add(checkoutTime.toLocalDate());
            // ISO week numbering, so a week spanning New Year counts once.
            rideWeeks.add(
                    checkoutTime.get(IsoFields.WEEK_BASED_YEAR) + "-" + checkoutTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
            );
        }

        Double dayPassEquivalent = Round.money(rideDays.size() * DAY_PASS_PRICE_EUR);
        Double weekPassEquivalent = Round.money(rideWeeks.size() * WEEK_PASS_PRICE_EUR);

        return new RideCostResponse(
                totalRides,
                ApiDateTime.date(firstRide),
                ApiDateTime.date(lastRide),
                dateRangeDays,
                ANNUAL_SUBSCRIPTION_PRICE_EUR,
                proratedSubscriptionPrice,
                costPerRide,
                dayPassEquivalent,
                weekPassEquivalent,
                Round.money(dayPassEquivalent - proratedSubscriptionPrice),
                Round.money(weekPassEquivalent - proratedSubscriptionPrice)
        );
    }

    private static RideCostResponse emptySummary() {
        return new RideCostResponse(
                0,
                null,
                null,
                null,
                ANNUAL_SUBSCRIPTION_PRICE_EUR,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
