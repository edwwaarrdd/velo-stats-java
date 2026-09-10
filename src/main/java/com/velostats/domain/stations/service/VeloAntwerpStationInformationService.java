package com.velostats.domain.stations.service;

import com.velostats.config.AppProperties;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

/**
 * Reads the operator's public GBFS station information feed.
 */
@Service
public class VeloAntwerpStationInformationService implements StationInformationService {

    private final RestClient http;
    private final String stationInformationUrl;

    public VeloAntwerpStationInformationService(RestClient upstreamRestClient, AppProperties properties) {
        this.http = upstreamRestClient;
        this.stationInformationUrl = properties.upstream().stationInformationUrl();
    }

    @Override
    public Map<String, StationInformation> fetchStations() {
        JsonNode payload = http.get()
                .uri(URI.create(stationInformationUrl))
                .retrieve()
                .body(JsonNode.class);

        Map<String, StationInformation> stations = new LinkedHashMap<>();

        for (JsonNode station : payload.path("data").path("stations")) {
            StationInformation information = fromGbfs(station);
            stations.put(information.stationId(), information);
        }

        return stations;
    }

    /**
     * The two defaulted fields are the ones the feed has been seen to omit. Everything else is
     * required, and a feed missing it is a feed worth failing on rather than silently storing an empty
     * station.
     */
    static StationInformation fromGbfs(JsonNode station) {
        return new StationInformation(
                required(station, "station_id").asString(),
                required(station, "name").asString(),
                required(station, "short_name").asString(),
                required(station, "lat").asDouble(),
                required(station, "lon").asDouble(),
                required(station, "address").asString(),
                required(station, "post_code").asString(),
                rentalMethods(station.path("rental_methods")),
                station.path("capacity").asInt(0)
        );
    }

    private static List<String> rentalMethods(JsonNode node) {
        if (!node.isArray()) {
            return List.of();
        }

        return node.valueStream().map(JsonNode::asString).toList();
    }

    private static JsonNode required(JsonNode station, String field) {
        JsonNode value = station.get(field);

        if (value == null || value.isNull()) {
            throw new IllegalStateException("Station feed entry is missing the " + field + " field.");
        }

        return value;
    }
}
