package com.velostats.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Everything this application reads from the environment, in one place.
 *
 * @param corsAllowedOrigins origins allowed to call the API
 * @param ridesJsonPath      path to the rides JSON export
 * @param queuePrefix        prefix for the queue keys in Redis
 * @param upstream           the three third-party services the background checks call
 */
@ConfigurationProperties(prefix = "velo")
public record AppProperties(
        List<String> corsAllowedOrigins,
        String ridesJsonPath,
        String queuePrefix,
        Upstream upstream
) {

    /**
     * @param stationInformationUrl the operator's GBFS station information feed
     * @param osrmBaseUrl           the OSRM host, without a profile path
     * @param openMeteoArchiveUrl   the Open-Meteo historical archive endpoint
     */
    public record Upstream(String stationInformationUrl, String osrmBaseUrl, String openMeteoArchiveUrl) {
    }
}
