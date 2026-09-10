package com.velostats.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Rounding for values that appear in API responses.
 */
public final class Round {

    public static final int PRECISION = 2;

    private Round() {
    }

    /**
     * Rounds the double that is actually stored, rather than the decimal a human would have typed.
     *
     * <p>The distinction matters: the nearest double to 15.995 is really 15.99499999999999957, so this
     * reports 15.99. Going through {@code BigDecimal.valueOf} would round the shortest decimal
     * representation instead and lift the same value to 16.0, which is why the exact
     * {@code BigDecimal(double)} constructor is used here.
     */
    public static Double money(Double value) {
        if (value == null) {
            return null;
        }

        return new BigDecimal(value).setScale(PRECISION, RoundingMode.HALF_UP).doubleValue();
    }

    public static Double money(long value) {
        return money((double) value);
    }
}
