package com.velostats.domain.stations.entity;

import java.util.List;
import tools.jackson.databind.json.JsonMapper;

/**
 * The rental methods column holds a JSON array of strings, because that is what the feed publishes
 * and what the other velo-stats backends store. Nothing queries inside it, so it stays text.
 */
final class RentalMethods {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private RentalMethods() {
    }

    static String encode(List<String> methods) {
        return MAPPER.writeValueAsString(methods == null ? List.of() : methods);
    }

    static List<String> decode(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        return MAPPER.readValue(json, MAPPER.getTypeFactory().constructCollectionType(List.class, String.class));
    }
}
