package com.velostats.domain.stations.service;

import java.util.Map;

public interface StationInformationService {

    /**
     * Every station the operator currently publishes, keyed by station id.
     */
    Map<String, StationInformation> fetchStations();
}
