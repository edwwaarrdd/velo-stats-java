package com.velostats.domain.routing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.velostats.config.AppProperties;
import com.velostats.domain.routing.entity.Route;
import com.velostats.domain.routing.entity.TravelMode;
import com.velostats.domain.routing.service.OsrmRouteService;
import com.velostats.support.Coordinate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OsrmRouteServiceTest {

    private static final String BASE_URL = "https://osrm.example";

    private static final Coordinate ORIGIN = new Coordinate(51.2189, 4.4131);

    private static final Coordinate DESTINATION = new Coordinate(51.2205, 4.3997);

    private MockRestServiceServer upstream;
    private OsrmRouteService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        upstream = MockRestServiceServer.bindTo(builder).build();
        service = new OsrmRouteService(
                builder.build(),
                new AppProperties(
                        List.of(),
                        "data/rides.json",
                        "test:",
                        new AppProperties.Upstream("https://feed.example", BASE_URL, "https://meteo.example")
                )
        );
    }

    /**
     * Two things are easy to get wrong and both change the answer: OSRM wants longitude before
     * latitude, and the profile is chosen by the instance path rather than by the URL segment.
     */
    @Test
    void asksTheBikeInstanceForLonLatCoordinates() {
        upstream.expect(requestTo(
                        BASE_URL + "/routed-bike/route/v1/bike/4.4131,51.2189;4.3997,51.2205?overview=false"
                ))
                .andRespond(withSuccess(
                        """
                        {"code": "Ok", "routes": [{"distance": 1500.0, "duration": 400.0}]}
                        """,
                        MediaType.APPLICATION_JSON
                ));

        Route route = service.getRoute(ORIGIN, DESTINATION, TravelMode.BIKE);

        assertThat(route.distanceMeters()).isEqualTo(1500.0);
        assertThat(route.durationSeconds()).isEqualTo(400.0);
        upstream.verify();
    }

    @Test
    void reportsAnUpstreamFailure() {
        upstream.expect(requestTo(org.hamcrest.Matchers.containsString("routed-bike")))
                .andRespond(withSuccess(
                        """
                        {"code": "NoRoute", "message": "Impossible route"}
                        """,
                        MediaType.APPLICATION_JSON
                ));

        assertThatThrownBy(() -> service.getRoute(ORIGIN, DESTINATION, TravelMode.BIKE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Impossible route");
    }

    @Test
    void reportsAnEmptyRouteList() {
        upstream.expect(requestTo(org.hamcrest.Matchers.containsString("routed-bike")))
                .andRespond(withSuccess("""
                        {"code": "Ok", "routes": []}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> service.getRoute(ORIGIN, DESTINATION, TravelMode.BIKE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no route");
    }
}
