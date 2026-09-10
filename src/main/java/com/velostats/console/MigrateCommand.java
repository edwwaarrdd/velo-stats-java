package com.velostats.console;

import org.springframework.stereotype.Component;

/**
 * Creates the database schema.
 *
 * <p>Migrations run on start in every mode, so by the time this command's body executes the work is
 * already done. It exists so that a container can migrate and exit without also serving or working.
 */
@Component
public class MigrateCommand implements ConsoleCommand {

    private final ConsoleOutput output;

    public MigrateCommand(ConsoleOutput output) {
        this.output = output;
    }

    @Override
    public String name() {
        return "migrate";
    }

    @Override
    public String description() {
        return "Bring the database schema up to date.";
    }

    @Override
    public int run(CommandArguments arguments) {
        output.success("Database schema is up to date.");

        return 0;
    }
}
