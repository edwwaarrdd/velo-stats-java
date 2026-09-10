package com.velostats.domain.stations.service;

import java.util.Map;

public interface StationInformationService {

    Map<String, StationInformation> fetchStations();
}
