package com.velostats.domain.rides.web;

import com.velostats.domain.rides.dto.RideCostResponse;
import com.velostats.domain.rides.service.RideCostCalculator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RideCostHandler {

    private final RideCostCalculator calculator;

    public RideCostHandler(RideCostCalculator calculator) {
        this.calculator = calculator;
    }

    @GetMapping("/rides/cost")
    public RideCostResponse handle() {
        return calculator.calculate();
    }
}
