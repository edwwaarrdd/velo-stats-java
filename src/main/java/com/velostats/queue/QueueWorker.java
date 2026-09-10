package com.velostats.queue;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Single-threaded on purpose: the two check queues call free public APIs, and handling one job to
 * completion before taking the next is what keeps those calls from ever overlapping.
 *
 * <p>There are no retries. A failure is logged, and the ride's {@code distance_checked_at} or
 * {@code weather_checked_at} stays null, so the next run of the matching console command queues it
 * again. That null column is the whole idempotency design.
 */
@Component
public class QueueWorker {

    private static final Logger log = LoggerFactory.getLogger(QueueWorker.class);

    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(5);

    private final RedisQueue queue;
    private final Map<Class<?>, JobHandler<Job>> handlers = new HashMap<>();

    @SuppressWarnings("unchecked")
    public QueueWorker(RedisQueue queue, List<JobHandler<?>> handlers) {
        this.queue = queue;

        for (JobHandler<?> handler : handlers) {
            this.handlers.put(handler.handles(), (JobHandler<Job>) handler);
        }
    }

    public void run(String queueName) {
        log.info("Worker started on queue {}", queueName);

        while (!Thread.currentThread().isInterrupted()) {
            Job job;

            try {
                job = queue.take(queueName, POLL_TIMEOUT);
            } catch (RuntimeException e) {
                log.error("Could not read from queue {}", queueName, e);

                continue;
            }

            if (job != null) {
                handle(job);
            }
        }
    }

    void handle(Job job) {
        JobHandler<Job> handler = handlers.get(job.getClass());

        if (handler == null) {
            log.error("No handler is registered for {}", job.getClass().getName());

            return;
        }

        try {
            handler.handle(job);
        } catch (RuntimeException e) {
            log.error("Job {} failed", job.getClass().getSimpleName(), e);
        }
    }
}
