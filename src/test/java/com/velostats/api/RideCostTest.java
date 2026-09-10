package com.velostats.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class RideCostTest extends IntegrationTestCase {

    @Test
    void reportsTheSubscriptionPriceEvenWithNoRides() {
        assertThat(getJson("/rides/cost")).isEqualTo(
                "{\"total_rides\":0,\"first_ride_date\":null,\"last_ride_date\":null,"
                        + "\"date_range_days\":null,\"subscription_price_eur\":58.0,"
                        + "\"prorated_subscription_price_eur\":null,\"cost_per_ride_eur\":null,"
                        + "\"day_pass_equivalent_eur\":null,\"week_pass_equivalent_eur\":null,"
                        + "\"money_saved_vs_day_passes_eur\":null,\"money_saved_vs_week_passes_eur\":null}"
        );
    }

    @Test
    void proratesTheSubscriptionOverTheRideHistory() {
        givenRide(1L, LocalDateTime.of(2026, 1, 1, 8, 0));
        givenRide(2L, LocalDateTime.of(2026, 1, 5, 8, 0));
        givenRide(3L, LocalDateTime.of(2026, 1, 10, 8, 0));

        assertThat(getJson("/rides/cost")).contains(
                "\"total_rides\":3",
                "\"first_ride_date\":\"2026-01-01\"",
                "\"last_ride_date\":\"2026-01-10\"",
                // Inclusive of both end days.
                "\"date_range_days\":10",
                "\"prorated_subscription_price_eur\":1.59",
                // Divides the rounded price, so the per-ride figure agrees with the total beside it.
                "\"cost_per_ride_eur\":0.53"
        );
    }

    /**
     * Two rides on one day are one day pass, not two.
     */
    @Test
    void countsDayPassesByDistinctDay() {
        givenRide(1L, LocalDateTime.of(2026, 1, 1, 8, 0));
        givenRide(2L, LocalDateTime.of(2026, 1, 1, 18, 0));
        givenRide(3L, LocalDateTime.of(2026, 1, 2, 8, 0));

        assertThat(getJson("/rides/cost")).contains("\"day_pass_equivalent_eur\":10.0");
    }

    /**
     * ISO week numbering, so a week straddling New Year counts once rather than twice.
     */
    @Test
    void countsWeekPassesByIsoWeek() {
        // Both of these fall in ISO week 2026-W01.
        givenRide(1L, LocalDateTime.of(2025, 12, 30, 8, 0));
        givenRide(2L, LocalDateTime.of(2026, 1, 2, 8, 0));

        assertThat(getJson("/rides/cost")).contains("\"week_pass_equivalent_eur\":12.0");
    }

    @Test
    void treatsASingleDayHistoryAsOneDayLong() {
        givenRide(1L, LocalDateTime.of(2026, 1, 1, 8, 0));

        assertThat(getJson("/rides/cost")).contains("\"date_range_days\":1");
    }
}
