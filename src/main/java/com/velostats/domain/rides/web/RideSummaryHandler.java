package com.velostats.domain.rides.web;

import com.velostats.domain.rides.dto.RideSummaryResponse;
import com.velostats.domain.rides.service.RideSummaryCalculator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RideSummaryHandler {

    private final RideSummaryCalculator calculator;

    public RideSummaryHandler(RideSummaryCalculator calculator) {
        this.calculator = calculator;
    }

    @GetMapping("/rides/summary")
    public RideSummaryResponse handle() {
        return calculator.calculate();
    }
}
