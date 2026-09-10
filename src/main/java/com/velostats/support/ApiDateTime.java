package com.velostats.support;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Date and time formatting for API responses. Stored timestamps are naive and mean UTC, so they are
 * rendered with a trailing Z and no conversion.
 */
public final class ApiDateTime {

    public static final DateTimeFormatter DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ApiDateTime() {
    }

    public static String datetime(LocalDateTime value) {
        return value == null ? null : DATETIME_FORMAT.format(value);
    }

    public static String date(LocalDate value) {
        return value == null ? null : DATE_FORMAT.format(value);
    }

    public static String date(LocalDateTime value) {
        return value == null ? null : DATE_FORMAT.format(value);
    }
}
