package com.velostats.queue;

/**
 * Implementations carry only what identifies the work, never the loaded entity, so a message stays
 * small and always acts on the row as it is when the worker picks it up rather than when it was
 * queued.
 */
public interface Job {

    String queue();
}
