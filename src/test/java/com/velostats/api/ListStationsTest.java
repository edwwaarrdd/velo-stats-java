package com.velostats.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ListStationsTest extends IntegrationTestCase {

    @Test
    void returnsAnEmptyListWhenThereAreNoStations() {
        assertThat(getJson("/stations")).isEqualTo("{\"results\":[]}");
    }

    /**
     * Only what a map needs. The address, post code, capacity and rental methods are stored but never
     * published.
     */
    @Test
    void returnsOnlyThePositionOfEachStation() {
        givenStation();

        assertThat(getJson("/stations")).isEqualTo(
                "{\"results\":[{\"station_id\":\"021\",\"name\":\"021- Driekoningen\","
                        + "\"lat\":51.2189,\"lon\":4.4131}]}"
        );
    }
}
