package com.velostats.domain.routing.service;

import com.velostats.config.AppProperties;
import com.velostats.domain.routing.entity.Route;
import com.velostats.domain.routing.entity.TravelMode;
import com.velostats.support.Coordinate;
import java.net.URI;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

@Service
public class OsrmRouteService implements RouteService {

    private final RestClient http;
    private final String baseUrl;

    public OsrmRouteService(RestClient upstreamRestClient, AppProperties properties) {
        this.http = upstreamRestClient;
        this.baseUrl = properties.upstream().osrmBaseUrl();
    }

    @Override
    public Route getRoute(Coordinate origin, Coordinate destination, TravelMode mode) {
        // OSRM expects coordinates as "lon,lat", not "lat,lon".
        String coordinates = String.format(
                Locale.ROOT,
                "%s,%s;%s,%s",
                origin.lon(),
                origin.lat(),
                destination.lon(),
                destination.lat()
        );

        String url = String.format(
                Locale.ROOT,
                // The geometry is never stored, so asking for it would only make the response bigger.
                "%s/%s/route/v1/%s/%s?overview=false",
                baseUrl,
                mode.osrmInstancePath(),
                mode.value(),
                coordinates
        );

        // A ready-made URI rather than a template: the coordinate pair contains separators that
        // template expansion would encode.
        JsonNode payload = http.get().uri(URI.create(url)).retrieve().body(JsonNode.class);

        if (!"Ok".equals(payload.path("code").asString(null))) {
            String reason = payload.path("message").asString(payload.path("code").asString("unknown error"));

            throw new IllegalStateException("OSRM request failed: " + reason);
        }

        JsonNode route = payload.path("routes").path(0);

        if (route.isMissingNode() || route.isNull()) {
            throw new IllegalStateException("OSRM returned no route for the requested coordinates.");
        }

        return new Route(route.path("distance").asDouble(), route.path("duration").asDouble());
    }
}
