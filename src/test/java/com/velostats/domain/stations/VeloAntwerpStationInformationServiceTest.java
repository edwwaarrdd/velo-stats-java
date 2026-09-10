package com.velostats.domain.stations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.velostats.config.AppProperties;
import com.velostats.domain.stations.service.StationInformation;
import com.velostats.domain.stations.service.VeloAntwerpStationInformationService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class VeloAntwerpStationInformationServiceTest {

    private static final String FEED_URL = "https://feed.example/station_information.json";

    private MockRestServiceServer upstream;
    private VeloAntwerpStationInformationService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        upstream = MockRestServiceServer.bindTo(builder).build();
        service = new VeloAntwerpStationInformationService(
                builder.build(),
                new AppProperties(
                        List.of(),
                        "data/rides.json",
                        "test:",
                        new AppProperties.Upstream(FEED_URL, "https://osrm.example", "https://meteo.example")
                )
        );
    }

    @Test
    void readsEveryStationKeyedByItsId() {
        respondWith("""
                {"data": {"stations": [
                    {
                        "station_id": "021", "name": "021- Driekoningen", "short_name": "021",
                        "lat": 51.2189, "lon": 4.4131, "address": "Driekoningenstraat",
                        "post_code": "2600", "rental_methods": ["KEY"], "capacity": 24
                    }
                ]}}
                """);

        Map<String, StationInformation> stations = service.fetchStations();

        assertThat(stations).containsOnlyKeys("021");

        StationInformation station = stations.get("021");
        assertThat(station.name()).isEqualTo("021- Driekoningen");
        assertThat(station.lat()).isEqualTo(51.2189);
        assertThat(station.lon()).isEqualTo(4.4131);
        assertThat(station.rentalMethods()).containsExactly("KEY");
        assertThat(station.capacity()).isEqualTo(24);
    }

    /**
     * The feed has been seen to omit both of these. Everything else is required.
     */
    @Test
    void defaultsTheRentalMethodsAndCapacityWhenTheFeedOmitsThem() {
        respondWith("""
                {"data": {"stations": [
                    {
                        "station_id": "021", "name": "Driekoningen", "short_name": "021",
                        "lat": 51.2189, "lon": 4.4131, "address": "Driekoningenstraat",
                        "post_code": "2600"
                    }
                ]}}
                """);

        StationInformation station = service.fetchStations().get("021");

        assertThat(station.rentalMethods()).isEmpty();
        assertThat(station.capacity()).isZero();
    }

    @Test
    void refusesAStationMissingARequiredField() {
        respondWith("""
                {"data": {"stations": [{"station_id": "021", "name": "Driekoningen"}]}}
                """);

        assertThatThrownBy(() -> service.fetchStations())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("short_name");
    }

    private void respondWith(String json) {
        upstream.expect(requestTo(FEED_URL))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
    }
}
