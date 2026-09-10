package com.velostats.domain.rides.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Plain JDBC rather than a mapped projection, because SQLite reports a computed REAL column as a
 * 32-bit float in its result set metadata. Letting the mapper follow that would round a distance
 * total of 279220.6 metres to 279220.59375 on the way out. Asking for a double explicitly reads the
 * value SQLite actually computed.
 */
public class RideRepositoryImpl implements RideSummaryQueries {

    private static final String SUMMARY_SQL = """
            SELECT
                COUNT(*) AS total_rides,
                SUM(r.duration) AS total_duration,
                AVG(r.duration) AS average_duration,
                MAX(r.duration) AS longest_ride_duration,
                MIN(r.duration) AS shortest_ride_duration,
                SUM(sr.distance_meters) AS total_distance_meters,
                AVG(sr.distance_meters) AS average_distance_meters
            FROM rides r
            LEFT JOIN station_routes sr
                ON sr.origin_station_id = r.origin_station_code
                AND sr.destination_station_id = r.destination_station_code
                AND sr.mode = :mode
            """;

    private final JdbcClient jdbc;

    public RideRepositoryImpl(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Distance sums and averages skip rides with no cached route, which the left join gives for free:
     * those rows contribute NULL, and SQL aggregates ignore NULL. The join is on the two station codes
     * rather than on a relation, because a route belongs to a station pair and not to a ride.
     */
    @Override
    public RideSummaryStats summaryStats(String mode) {
        return jdbc.sql(SUMMARY_SQL)
                .param("mode", mode)
                .query((ResultSet row, int rowNumber) -> new RideSummaryStats(
                        row.getLong("total_rides"),
                        nullableLong(row, "total_duration"),
                        nullableDouble(row, "average_duration"),
                        nullableInt(row, "longest_ride_duration"),
                        nullableInt(row, "shortest_ride_duration"),
                        nullableDouble(row, "total_distance_meters"),
                        nullableDouble(row, "average_distance_meters")
                ))
                .single();
    }

    private static Long nullableLong(ResultSet row, String column) throws SQLException {
        long value = row.getLong(column);

        return row.wasNull() ? null : value;
    }

    private static Integer nullableInt(ResultSet row, String column) throws SQLException {
        int value = row.getInt(column);

        return row.wasNull() ? null : value;
    }

    private static Double nullableDouble(ResultSet row, String column) throws SQLException {
        double value = row.getDouble(column);

        return row.wasNull() ? null : value;
    }
}
