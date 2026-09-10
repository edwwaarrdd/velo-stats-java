package com.velostats.domain.stations.command;

import com.velostats.console.CommandArguments;
import com.velostats.console.ConsoleCommand;
import com.velostats.console.ConsoleOutput;
import com.velostats.domain.stations.entity.Station;
import com.velostats.domain.stations.repository.StationRepository;
import com.velostats.domain.stations.service.StationInformation;
import com.velostats.domain.stations.service.StationInformationService;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LoadStationsCommand implements ConsoleCommand {

    private final StationInformationService stations;
    private final StationRepository repository;
    private final ConsoleOutput output;

    public LoadStationsCommand(
            StationInformationService stations,
            StationRepository repository,
            ConsoleOutput output
    ) {
        this.stations = stations;
        this.repository = repository;
        this.output = output;
    }

    @Override
    public String name() {
        return "stations:load";
    }

    @Override
    public String description() {
        return "Load the docking stations from the operator feed.";
    }

    @Override
    @Transactional
    public int run(CommandArguments arguments) {
        Map<String, StationInformation> fetched = stations.fetchStations();
        int created = 0;

        for (StationInformation information : fetched.values()) {
            if (upsert(information)) {
                created++;
            }
        }

        output.success(String.format(
                "Loaded %d stations (%d created, %d updated).",
                fetched.size(),
                created,
                fetched.size() - created
        ));

        return 0;
    }

    /**
     * @return whether a station was created rather than updated
     */
    private boolean upsert(StationInformation information) {
        Optional<Station> existing = repository.findById(information.stationId());

        existing.ifPresentOrElse(
                station -> station.fill(
                        information.name(),
                        information.shortName(),
                        information.lat(),
                        information.lon(),
                        information.address(),
                        information.postCode(),
                        information.rentalMethods(),
                        information.capacity()
                ),
                () -> repository.save(new Station(
                        information.stationId(),
                        information.name(),
                        information.shortName(),
                        information.lat(),
                        information.lon(),
                        information.address(),
                        information.postCode(),
                        information.rentalMethods(),
                        information.capacity()
                ))
        );

        return existing.isEmpty();
    }
}
