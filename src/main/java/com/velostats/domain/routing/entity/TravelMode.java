package com.velostats.domain.routing.entity;

public enum TravelMode {

    FOOT("foot", "routed-foot"),
    BIKE("bike", "routed-bike");

    private final String value;
    private final String osrmInstancePath;

    TravelMode(String value, String osrmInstancePath) {
        this.value = value;
        this.osrmInstancePath = osrmInstancePath;
    }

    /**
     * The value stored in the database and sent to OSRM, shared with every other velo-stats backend.
     */
    public String value() {
        return value;
    }

    /**
     * The demo server at router.project-osrm.org only hosts the car profile and silently ignores
     * the profile named in the URL, so every mode came back with car driving times. FOSSGIS runs a
     * separate instance per profile, and the profile is selected by this path rather than by the URL
     * segment.
     */
    public String osrmInstancePath() {
        return osrmInstancePath;
    }

    public static TravelMode fromValue(String value) {
        for (TravelMode mode : values()) {
            if (mode.value.equals(value)) {
                return mode;
            }
        }

        throw new IllegalArgumentException("Unknown travel mode: " + value);
    }
}
