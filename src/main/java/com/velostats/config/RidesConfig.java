package com.velostats.config;

import com.velostats.domain.rides.service.JsonFileRideService;
import com.velostats.domain.rides.service.RideDataSource;
import java.nio.file.Path;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The configured ride export.
 *
 * <p>Declared here rather than as a component, because the reader takes a path rather than the
 * settings object: the same class serves the configured export and the one named by
 * {@code rides:load --path}.
 */
@Configuration
public class RidesConfig {

    @Bean
    public RideDataSource rideDataSource(AppProperties properties) {
        return JsonFileRideService.at(Path.of(properties.ridesJsonPath()));
    }
}
