package com.velostats.queue;

/**
 * Handles one kind of job. One implementation per job type, found by the worker through the
 * container.
 */
public interface JobHandler<T extends Job> {

    Class<T> handles();

    void handle(T job);
}
