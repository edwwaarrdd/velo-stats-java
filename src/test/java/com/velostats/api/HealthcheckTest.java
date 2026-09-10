package com.velostats.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HealthcheckTest extends IntegrationTestCase {

    @Test
    void reportsThatTheProcessIsUp() {
        assertThat(getJson("/_healthcheck")).isEqualTo("{\"message\":\"ok\"}");
    }
}
