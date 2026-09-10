package com.velostats.console;

import com.velostats.queue.QueueWorker;
import com.velostats.queue.Queues;
import org.springframework.stereotype.Component;

/**
 * Runs a queue worker until the process is stopped.
 */
@Component
public class WorkCommand implements ConsoleCommand {

    private final QueueWorker worker;

    public WorkCommand(QueueWorker worker) {
        this.worker = worker;
    }

    @Override
    public String name() {
        return "work";
    }

    @Override
    public String description() {
        return "Drain a queue, one job at a time. Pass --queue=NAME (default: " + Queues.DEFAULT + ").";
    }

    @Override
    public int run(CommandArguments arguments) {
        worker.run(arguments.option("queue").orElse(Queues.DEFAULT));

        return 0;
    }
}
