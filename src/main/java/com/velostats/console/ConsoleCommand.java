package com.velostats.console;

/**
 * Every implementation is a bean, so adding a command means adding one class and changing nothing
 * else: the dispatcher finds it by the name it reports.
 */
public interface ConsoleCommand {

    /**
     * The name typed on the command line, for example {@code rides:load}.
     */
    String name();

    String description();

    /**
     * @return the process exit code
     */
    int run(CommandArguments arguments);
}
