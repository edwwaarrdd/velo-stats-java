package com.velostats.console;

/**
 * One console command.
 *
 * <p>Every implementation is a bean, so adding a command means adding one class and changing nothing
 * else: the dispatcher finds it by the name it reports.
 */
public interface ConsoleCommand {

    /**
     * The name typed on the command line, for example {@code rides:load}.
     */
    String name();

    /**
     * The one-line description shown in the usage listing.
     */
    String description();

    /**
     * @return the process exit code
     */
    int run(CommandArguments arguments);
}
