package com.velostats.domain.weather;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.velostats.config.AppProperties;
import com.velostats.domain.weather.entity.WeatherObservation;
import com.velostats.domain.weather.service.OpenMeteoWeatherService;
import com.velostats.support.Coordinate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OpenMeteoWeatherServiceTest {

    private static final String ARCHIVE_URL = "https://meteo.example/archive";

    private static final Coordinate ANTWERP = new Coordinate(51.2189, 4.4131);

    private MockRestServiceServer upstream;
    private OpenMeteoWeatherService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        upstream = MockRestServiceServer.bindTo(builder).build();
        service = new OpenMeteoWeatherService(
                builder.build(),
                new AppProperties(
                        List.of(),
                        "data/rides.json",
                        "test:",
                        new AppProperties.Upstream("https://feed.example", "https://osrm.example", ARCHIVE_URL)
                )
        );
    }

    /**
     * The archive answers in columns: one index selects one hour across every variable. Picking the
     * wrong index silently attributes another hour's weather to the ride.
     */
    @Test
    void picksTheHourTheRideEndedOutOfTheDay() {
        upstream.expect(requestTo(containsString("start_date=2026-09-06")))
                .andRespond(withSuccess(hourlyResponse(), MediaType.APPLICATION_JSON));

        WeatherObservation observation = service.getWeather(ANTWERP, LocalDateTime.of(2026, 9, 6, 9, 5, 30));

        assertThat(observation.temperatureC()).isEqualTo(18.4);
        assertThat(observation.apparentTemperatureC()).isEqualTo(17.1);
        assertThat(observation.precipitationMm()).isEqualTo(0.2);
        assertThat(observation.rainMm()).isEqualTo(0.2);
        assertThat(observation.snowfallCm()).isZero();
        assertThat(observation.cloudCoverPercent()).isEqualTo(75.0);
        assertThat(observation.windSpeedKmh()).isEqualTo(14.8);
        assertThat(observation.windGustsKmh()).isEqualTo(31.0);
        assertThat(observation.windDirectionDegrees()).isEqualTo(210.0);
        assertThat(observation.relativeHumidityPercent()).isEqualTo(72.0);
        assertThat(observation.weatherCode()).isEqualTo(3);
        assertThat(observation.observedAt()).isEqualTo(LocalDateTime.of(2026, 9, 6, 9, 0));
    }

    @Test
    void reportsAnHourTheArchiveDoesNotCover() {
        upstream.expect(requestTo(containsString("start_date=2026-09-06")))
                .andRespond(withSuccess(hourlyResponse(), MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> service.getWeather(ANTWERP, LocalDateTime.of(2026, 9, 6, 23, 0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("2026-09-06T23:00");
    }

    @Test
    void reportsAnUpstreamFailure() {
        upstream.expect(requestTo(containsString("start_date")))
                .andRespond(withSuccess("""
                        {"error": true, "reason": "Value out of allowed range"}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> service.getWeather(ANTWERP, LocalDateTime.of(2026, 9, 6, 9, 5)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Value out of allowed range");
    }

    private static String hourlyResponse() {
        return """
                {
                    "hourly": {
                        "time": ["2026-09-06T08:00", "2026-09-06T09:00"],
                        "temperature_2m": [17.2, 18.4],
                        "apparent_temperature": [16.0, 17.1],
                        "precipitation": [0.0, 0.2],
                        "rain": [0.0, 0.2],
                        "snowfall": [0.0, 0.0],
                        "cloud_cover": [50, 75],
                        "wind_speed_10m": [12.1, 14.8],
                        "wind_gusts_10m": [25.2, 31.0],
                        "wind_direction_10m": [200, 210],
                        "relative_humidity_2m": [80, 72],
                        "weather_code": [2, 3]
                    }
                }
                """;
    }
}
