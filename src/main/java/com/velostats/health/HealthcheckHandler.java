package com.velostats.health;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Answers whether the process is up. It deliberately touches neither the database nor Redis:
 * container orchestration uses this to decide whether to route traffic here, and a slow database
 * should not take the process out.
 */
@RestController
public class HealthcheckHandler {

    @GetMapping("/_healthcheck")
    public Map<String, String> handle() {
        return Map.of("message", "ok");
    }
}
