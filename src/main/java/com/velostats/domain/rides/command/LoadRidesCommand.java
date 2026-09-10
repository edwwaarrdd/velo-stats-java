package com.velostats.domain.rides.command;

import com.velostats.console.CommandArguments;
import com.velostats.console.ConsoleCommand;
import com.velostats.console.ConsoleOutput;
import com.velostats.domain.rides.entity.Ride;
import com.velostats.domain.rides.repository.RideRepository;
import com.velostats.domain.rides.service.JsonFileRideService;
import com.velostats.domain.rides.service.RideDataSource;
import com.velostats.domain.rides.service.RideRecord;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LoadRidesCommand implements ConsoleCommand {

    private final RideDataSource rides;
    private final RideRepository repository;
    private final ConsoleOutput output;

    public LoadRidesCommand(RideDataSource rides, RideRepository repository, ConsoleOutput output) {
        this.rides = rides;
        this.repository = repository;
        this.output = output;
    }

    @Override
    public String name() {
        return "rides:load";
    }

    @Override
    public String description() {
        return "Load the ride history from the JSON export.";
    }

    @Override
    @Transactional
    public int run(CommandArguments arguments) {
        RideDataSource source = arguments.option("path")
                .<RideDataSource>map(path -> JsonFileRideService.at(Path.of(path)))
                .orElse(rides);

        Map<Long, RideRecord> fetched = source.fetchRides();
        int created = 0;

        for (RideRecord record : fetched.values()) {
            if (upsert(record)) {
                created++;
            }
        }

        output.success(String.format(
                "Loaded %d rides (%d created, %d updated).",
                fetched.size(),
                created,
                fetched.size() - created
        ));

        return 0;
    }

    /**
     * The two check timestamps are deliberately not part of the record, so re-importing the export
     * never queues work that has already been done.
     *
     * @return whether a ride was created rather than updated
     */
    private boolean upsert(RideRecord record) {
        Optional<Ride> existing = repository.findById(record.rideId());

        existing.ifPresentOrElse(
                ride -> ride.fill(
                        record.accountId(),
                        record.status(),
                        record.duration(),
                        record.bikeNumber(),
                        record.originStationCode(),
                        record.originStation(),
                        record.originSlotId(),
                        record.checkoutTime(),
                        record.destinationStationCode(),
                        record.destinationStation(),
                        record.destinationSlotId(),
                        record.checkinTime()
                ),
                () -> repository.save(new Ride(
                        record.rideId(),
                        record.accountId(),
                        record.status(),
                        record.duration(),
                        record.bikeNumber(),
                        record.originStationCode(),
                        record.originStation(),
                        record.originSlotId(),
                        record.checkoutTime(),
                        record.destinationStationCode(),
                        record.destinationStation(),
                        record.destinationSlotId(),
                        record.checkinTime()
                ))
        );

        return existing.isEmpty();
    }
}
