package com.velostats.console;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Only registered when the application is started in console mode, so the API process never has a
 * command running underneath it.
 */
@Component
public class ConsoleRunner {

    private final List<ConsoleCommand> commands;
    private final ConsoleOutput output;

    public ConsoleRunner(List<ConsoleCommand> commands, ConsoleOutput output) {
        this.commands = commands;
        this.output = output;
    }

    public int run(String[] arguments) {
        if (arguments.length == 0) {
            printUsage();

            return 1;
        }

        String name = arguments[0];
        Optional<ConsoleCommand> command = commands.stream()
                .filter(candidate -> candidate.name().equals(name))
                .findFirst();

        if (command.isEmpty()) {
            output.error("Unknown command: " + name);
            printUsage();

            return 1;
        }

        List<String> rest = Arrays.asList(arguments).subList(1, arguments.length);

        return command.get().run(CommandArguments.parse(rest));
    }

    private void printUsage() {
        output.line("Usage: velo <command> [options]");
        output.line("");
        output.line("Available commands:");

        commands.stream()
                .sorted(Comparator.comparing(ConsoleCommand::name))
                .forEach(command -> output.line(
                        String.format("  %-24s %s", command.name(), command.description())
                ));
    }

    public static void runAndExit(ApplicationContext context, String[] arguments) {
        int status = context.getBean(ConsoleRunner.class).run(arguments);

        System.exit(SpringApplication.exit(context, (ExitCodeGenerator) () -> status));
    }
}
