package com.velostats.console;

import com.velostats.queue.RedisQueue;
import org.springframework.stereotype.Component;

/**
 * Dispatches a job that only logs, for checking that Redis and a worker are talking to each other.
 */
@Component
public class DispatchTestJobCommand implements ConsoleCommand {

    private final RedisQueue queue;
    private final ConsoleOutput output;

    public DispatchTestJobCommand(RedisQueue queue, ConsoleOutput output) {
        this.queue = queue;
        this.output = output;
    }

    @Override
    public String name() {
        return "tasks:dispatch-test";
    }

    @Override
    public String description() {
        return "Dispatch a job that logs a message from the worker. Pass --message=TEXT.";
    }

    @Override
    public int run(CommandArguments arguments) {
        String message = arguments.option("message").orElse("hello world");

        queue.dispatch(new LogMessage(message));
        output.success("Dispatched a test job carrying: " + message);

        return 0;
    }
}
