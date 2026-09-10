package com.velostats.domain.rides.web;

import com.velostats.domain.rides.dto.RideResponse;
import com.velostats.domain.rides.service.RideListProvider;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ListRidesHandler {

    private final RideListProvider rides;

    public ListRidesHandler(RideListProvider rides) {
        this.rides = rides;
    }

    @GetMapping("/rides")
    public Map<String, List<RideResponse>> handle() {
        return Map.of("results", rides.list().stream().map(RideResponse::from).toList());
    }
}
