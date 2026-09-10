package com.velostats.domain.stations.web;

import com.velostats.domain.stations.dto.StationResponse;
import com.velostats.domain.stations.repository.StationRepository;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Every known docking station, with just enough to place it on a map.
 */
@RestController
public class ListStationsHandler {

    private final StationRepository stations;

    public ListStationsHandler(StationRepository stations) {
        this.stations = stations;
    }

    @GetMapping("/stations")
    public Map<String, List<StationResponse>> handle() {
        return Map.of("results", stations.findAllForApi().stream().map(StationResponse::from).toList());
    }
}
