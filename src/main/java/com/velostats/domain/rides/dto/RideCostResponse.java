package com.velostats.domain.rides.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
        "total_rides",
        "first_ride_date",
        "last_ride_date",
        "date_range_days",
        "subscription_price_eur",
        "prorated_subscription_price_eur",
        "cost_per_ride_eur",
        "day_pass_equivalent_eur",
        "week_pass_equivalent_eur",
        "money_saved_vs_day_passes_eur",
        "money_saved_vs_week_passes_eur"
})
public record RideCostResponse(
        @JsonProperty("total_rides") int totalRides,
        @JsonProperty("first_ride_date") String firstRideDate,
        @JsonProperty("last_ride_date") String lastRideDate,
        @JsonProperty("date_range_days") Integer dateRangeDays,
        @JsonProperty("subscription_price_eur") double subscriptionPriceEur,
        @JsonProperty("prorated_subscription_price_eur") Double proratedSubscriptionPriceEur,
        @JsonProperty("cost_per_ride_eur") Double costPerRideEur,
        @JsonProperty("day_pass_equivalent_eur") Double dayPassEquivalentEur,
        @JsonProperty("week_pass_equivalent_eur") Double weekPassEquivalentEur,
        @JsonProperty("money_saved_vs_day_passes_eur") Double moneySavedVsDayPassesEur,
        @JsonProperty("money_saved_vs_week_passes_eur") Double moneySavedVsWeekPassesEur
) {
}
