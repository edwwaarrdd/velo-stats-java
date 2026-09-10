package com.velostats;

import com.velostats.config.AppProperties;
import com.velostats.console.ConsoleRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * The single entry point: the API, the workers and every console command.
 *
 * <p>The first argument decides which. {@code serve}, or no argument at all, starts the HTTP server;
 * anything else runs that console command with no web server underneath it and exits with its status.
 * Migrations run on start either way, so a fresh container needs no manual setup.
 */
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class VeloStatsApplication {

    public static final String SERVE = "serve";

    public static void main(String[] arguments) {
        if (isServe(arguments)) {
            new SpringApplicationBuilder(VeloStatsApplication.class)
                    .web(WebApplicationType.SERVLET)
                    .run(arguments);

            return;
        }

        ConfigurableApplicationContext context = new SpringApplicationBuilder(VeloStatsApplication.class)
                .web(WebApplicationType.NONE)
                .run(arguments);

        ConsoleRunner.runAndExit(context, arguments);
    }

    private static boolean isServe(String[] arguments) {
        return arguments.length == 0 || SERVE.equals(arguments[0]);
    }
}
