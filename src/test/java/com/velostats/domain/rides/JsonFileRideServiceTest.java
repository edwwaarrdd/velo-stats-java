package com.velostats.domain.rides;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.velostats.domain.rides.service.JsonFileRideService;
import com.velostats.domain.rides.service.RideRecord;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonFileRideServiceTest {

    @TempDir
    Path directory;

    @Test
    void readsEveryRideFromTheExport() throws IOException {
        Path export = write("""
                {
                    "data": {
                        "CustomerRides": [
                            {
                                "id": 73147208,
                                "accountId": 123,
                                "status": "Completed",
                                "duration": 8,
                                "bikeNumber": "5097",
                                "originStationCode": "021",
                                "originStation": "021- Driekoningen",
                                "originSlotId": "15",
                                "checkoutTime": "2026-09-06 08:57:02",
                                "destinationStationCode": "041",
                                "destinationStation": "041- Van Eyck",
                                "destinationSlotId": "23",
                                "checkinTime": "2026-09-06 09:05:30"
                            }
                        ]
                    }
                }
                """);

        Map<Long, RideRecord> rides = JsonFileRideService.at(export).fetchRides();

        assertThat(rides).hasSize(1);

        RideRecord ride = rides.get(73147208L);
        assertThat(ride.accountId()).isEqualTo(123L);
        assertThat(ride.status()).isEqualTo("Completed");
        assertThat(ride.duration()).isEqualTo(8);
        assertThat(ride.bikeNumber()).isEqualTo("5097");
        assertThat(ride.originStationCode()).isEqualTo("021");
        assertThat(ride.checkoutTime()).isEqualTo(LocalDateTime.of(2026, 9, 6, 8, 57, 2));
        assertThat(ride.checkinTime()).isEqualTo(LocalDateTime.of(2026, 9, 6, 9, 5, 30));
    }

    /**
     * The export has been seen to repeat an id. The last occurrence wins rather than the load failing.
     */
    @Test
    void collapsesADuplicateIdToTheLastOccurrence() throws IOException {
        Path export = write("""
                {"data": {"CustomerRides": [
                    %s,
                    %s
                ]}}
                """.formatted(ride(1, "5097"), ride(1, "2281")));

        Map<Long, RideRecord> rides = JsonFileRideService.at(export).fetchRides();

        assertThat(rides).hasSize(1);
        assertThat(rides.get(1L).bikeNumber()).isEqualTo("2281");
    }

    @Test
    void refusesAnExportThatIsNotThere() {
        assertThatThrownBy(() -> JsonFileRideService.at(directory.resolve("missing.json")).fetchRides())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Rides export not found");
    }

    @Test
    void refusesAnUnreadableTimestamp() throws IOException {
        Path export = write("""
                {"data": {"CustomerRides": [
                    {
                        "id": 1, "accountId": 1, "status": "Completed", "duration": 8,
                        "bikeNumber": "5097", "originStationCode": "021", "originStation": "Origin",
                        "originSlotId": "15", "checkoutTime": "yesterday",
                        "destinationStationCode": "041", "destinationStation": "Destination",
                        "destinationSlotId": "23", "checkinTime": "2026-09-06 09:05:30"
                    }
                ]}}
                """);

        assertThatThrownBy(() -> JsonFileRideService.at(export).fetchRides())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unreadable timestamp");
    }

    private static String ride(long id, String bikeNumber) {
        return """
                {
                    "id": %d, "accountId": 1, "status": "Completed", "duration": 8,
                    "bikeNumber": "%s", "originStationCode": "021", "originStation": "Origin",
                    "originSlotId": "15", "checkoutTime": "2026-09-06 08:57:02",
                    "destinationStationCode": "041", "destinationStation": "Destination",
                    "destinationSlotId": "23", "checkinTime": "2026-09-06 09:05:30"
                }
                """.formatted(id, bikeNumber);
    }

    private Path write(String json) throws IOException {
        Path export = directory.resolve("rides.json");
        Files.writeString(export, json);

        return export;
    }
}
