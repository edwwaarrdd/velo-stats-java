package com.velostats.domain.rides.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public class JsonFileRideService implements RideDataSource {

    /**
     * The timestamps in the export carry no zone. They are Antwerp local time in name only: the rest
     * of this application works in UTC, and the other ports read them the same way.
     */
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private final Path path;

    public JsonFileRideService(Path path) {
        this.path = path;
    }

    /**
     * A reader for one specific export, for the {@code --path} option and for tests.
     */
    public static JsonFileRideService at(Path path) {
        return new JsonFileRideService(path);
    }

    @Override
    public Map<Long, RideRecord> fetchRides() {
        if (!Files.isRegularFile(path)) {
            throw new IllegalStateException("Rides export not found at " + path + ".");
        }

        JsonNode payload;

        try {
            payload = MAPPER.readTree(Files.readString(path));
        } catch (IOException e) {
            throw new UncheckedIOException("Rides export at " + path + " could not be read.", e);
        }

        Map<Long, RideRecord> rides = new LinkedHashMap<>();

        for (JsonNode ride : payload.path("data").path("CustomerRides")) {
            RideRecord record = fromExport(ride);
            rides.put(record.rideId(), record);
        }

        return rides;
    }

    static RideRecord fromExport(JsonNode ride) {
        return new RideRecord(
                ride.path("id").asLong(),
                ride.path("accountId").asLong(),
                ride.path("status").asString(),
                ride.path("duration").asInt(),
                ride.path("bikeNumber").asString(),
                ride.path("originStationCode").asString(),
                ride.path("originStation").asString(),
                ride.path("originSlotId").asString(),
                parseDateTime(ride.path("checkoutTime").asString()),
                ride.path("destinationStationCode").asString(),
                ride.path("destinationStation").asString(),
                ride.path("destinationSlotId").asString(),
                parseDateTime(ride.path("checkinTime").asString())
        );
    }

    private static LocalDateTime parseDateTime(String value) {
        try {
            return LocalDateTime.parse(value, DATETIME_FORMAT);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Ride export has an unreadable timestamp: " + value + ".", e);
        }
    }
}
