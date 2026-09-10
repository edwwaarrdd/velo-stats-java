package com.velostats.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * The client every upstream call goes through.
 *
 * <p>The timeouts are the point: all three services are free and public, and a worker that hangs on
 * one of them stops draining its queue entirely.
 */
@Configuration
public class HttpClientConfig {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Bean
    public RestClient upstreamRestClient() {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(TIMEOUT).build()
        );
        requestFactory.setReadTimeout(TIMEOUT);

        return RestClient.builder().requestFactory(requestFactory).build();
    }
}
