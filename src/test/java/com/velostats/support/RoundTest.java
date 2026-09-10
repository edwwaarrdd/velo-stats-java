package com.velostats.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RoundTest {

    @Test
    @DisplayName("leaves null alone")
    void leavesNullAlone() {
        assertThat(Round.money(null)).isNull();
    }

    /**
     * The case the whole helper exists for. Rounding the shortest decimal representation treats 15.995
     * as an exact midpoint and lifts it to 16.0, even though the nearest double is really
     * 15.99499999999999957. Rounding the exact binary value reports 15.99.
     */
    @Test
    @DisplayName("rounds the stored double rather than the typed decimal")
    void roundsTheStoredDouble() {
        assertThat(Math.round(15.995 * 100) / 100.0).isEqualTo(16.0);
        assertThat(Round.money(15.995)).isEqualTo(15.99);
    }

    @ParameterizedTest(name = "{0} rounds to {1}")
    @CsvSource({
            "42.0, 42.0",
            "1.5, 1.5",
            "1.234, 1.23",
            "1.236, 1.24",
            "-1.236, -1.24",
            "0.0, 0.0"
    })
    void roundsToTwoDecimals(double value, double expected) {
        assertThat(Round.money(value)).isEqualTo(expected);
    }
}
