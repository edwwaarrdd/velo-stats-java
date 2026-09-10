package com.velostats.console;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The options given after the command name.
 *
 * <p>Both {@code --path=value} and {@code --path value} are accepted, and a bare {@code --force} is a
 * flag. This is deliberately the whole parser: no command here takes anything more elaborate.
 */
public final class CommandArguments {

    private final Map<String, String> options;

    private CommandArguments(Map<String, String> options) {
        this.options = options;
    }

    public static CommandArguments parse(List<String> arguments) {
        Map<String, String> options = new LinkedHashMap<>();

        for (int index = 0; index < arguments.size(); index++) {
            String argument = arguments.get(index);

            if (!argument.startsWith("--")) {
                continue;
            }

            String name = argument.substring(2);
            int equals = name.indexOf('=');

            if (equals >= 0) {
                options.put(name.substring(0, equals), name.substring(equals + 1));

                continue;
            }

            String next = index + 1 < arguments.size() ? arguments.get(index + 1) : null;

            if (next != null && !next.startsWith("--")) {
                options.put(name, next);
                index++;
            } else {
                options.put(name, "");
            }
        }

        return new CommandArguments(options);
    }

    public Optional<String> option(String name) {
        return Optional.ofNullable(options.get(name)).filter(value -> !value.isEmpty());
    }

    public boolean flag(String name) {
        return options.containsKey(name);
    }
}
