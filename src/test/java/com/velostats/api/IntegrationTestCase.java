package com.velostats.api;

import com.velostats.domain.rides.entity.Ride;
import com.velostats.domain.rides.repository.RideRepository;
import com.velostats.domain.routing.entity.StationRoute;
import com.velostats.domain.routing.entity.TravelMode;
import com.velostats.domain.routing.repository.StationRouteRepository;
import com.velostats.domain.stations.entity.Station;
import com.velostats.domain.stations.repository.StationRepository;
import com.velostats.domain.weather.entity.WeatherObservation;
import com.velostats.domain.weather.entity.WeatherRecord;
import com.velostats.domain.weather.repository.WeatherRecordRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

/**
 * Boots the application against a throwaway SQLite file, emptied before every test so nothing leaks
 * between them. Nothing here touches the network or Redis.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class IntegrationTestCase {

    static final long RIDE_ID = 73147208L;

    static final String ORIGIN_CODE = "021";

    static final String DESTINATION_CODE = "041";

    @LocalServerPort
    int port;

    @Autowired
    StationRepository stations;

    @Autowired
    RideRepository rides;

    @Autowired
    StationRouteRepository routes;

    @Autowired
    WeatherRecordRepository weatherRecords;

    RestClient api;

    @DynamicPropertySource
    static void throwawayDatabase(DynamicPropertyRegistry registry) throws IOException {
        Path database = Files.createTempDirectory("velo-stats-test").resolve("test.sqlite");

        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + database + "?busy_timeout=5000&foreign_keys=on");
    }

    @BeforeEach
    void resetDatabase() {
        api = RestClient.create("http://localhost:" + port);

        weatherRecords.deleteAllInBatch();
        routes.deleteAllInBatch();
        rides.deleteAllInBatch();
        stations.deleteAllInBatch();
    }

    /**
     * The exact response body, byte for byte, which is what these tests are for.
     */
    String getJson(String path) {
        return api.get().uri(path).retrieve().body(String.class);
    }

    Station givenStation() {
        return givenStation(ORIGIN_CODE, "021- Driekoningen", 51.2189, 4.4131);
    }

    Station givenDestinationStation() {
        return givenStation(DESTINATION_CODE, "041- Van Eyck", 51.2205, 4.3997);
    }

    Station givenStation(String stationId, String name, double lat, double lon) {
        return stations.save(new Station(
                stationId,
                name,
                stationId,
                lat,
                lon,
                "Driekoningenstraat",
                "2600",
                List.of("KEY"),
                24
        ));
    }

    Ride givenRide() {
        return givenRide(RIDE_ID, LocalDateTime.of(2026, 9, 6, 8, 57, 2), ORIGIN_CODE, DESTINATION_CODE, 8);
    }

    Ride givenRide(long rideId, LocalDateTime checkoutTime) {
        return givenRide(rideId, checkoutTime, ORIGIN_CODE, DESTINATION_CODE, 8);
    }

    Ride givenRide(long rideId, LocalDateTime checkoutTime, String originCode, String destinationCode, int duration) {
        return rides.save(new Ride(
                rideId,
                123L,
                "Completed",
                duration,
                "5097",
                originCode,
                "021- Driekoningen",
                "15",
                checkoutTime,
                destinationCode,
                "041- Van Eyck",
                "23",
                checkoutTime.plusSeconds(508)
        ));
    }

    StationRoute givenRoute(Station origin, Station destination) {
        return givenRoute(origin, destination, TravelMode.BIKE, 1500.0, 400.0);
    }

    StationRoute givenRoute(Station origin, Station destination, TravelMode mode, double distance, double duration) {
        return routes.save(new StationRoute(origin, destination, mode, distance, duration));
    }

    WeatherRecord givenWeather(Ride ride) {
        return weatherRecords.save(new WeatherRecord(ride, new WeatherObservation(
                18.4,
                17.1,
                0.2,
                0.2,
                0.0,
                75.0,
                14.8,
                31.0,
                210.0,
                72.0,
                3,
                LocalDateTime.of(2026, 9, 6, 9, 0)
        )));
    }
}
