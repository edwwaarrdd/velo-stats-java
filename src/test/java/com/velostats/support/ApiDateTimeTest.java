package com.velostats.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ApiDateTimeTest {

    @Test
    void formatsADateTimeWithATrailingZulu() {
        assertThat(ApiDateTime.datetime(LocalDateTime.of(2026, 9, 6, 8, 57, 2)))
                .isEqualTo("2026-09-06T08:57:02Z");
    }

    @Test
    void formatsADate() {
        assertThat(ApiDateTime.date(LocalDateTime.of(2026, 9, 6, 8, 57, 2))).isEqualTo("2026-09-06");
    }

    @Test
    void leavesNullAlone() {
        assertThat(ApiDateTime.datetime(null)).isNull();
        assertThat(ApiDateTime.date((LocalDateTime) null)).isNull();
    }
}
