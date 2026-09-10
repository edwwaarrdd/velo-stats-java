package com.velostats.queue;

import com.velostats.config.AppProperties;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * A job is a small JSON envelope pushed onto a list and taken off the other end, so a queue is
 * first-in first-out and a worker that blocks on it wakes the moment work arrives.
 */
@Component
public class RedisQueue {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private final StringRedisTemplate redis;
    private final String prefix;

    public RedisQueue(StringRedisTemplate redis, AppProperties properties) {
        this.redis = redis;
        this.prefix = properties.queuePrefix();
    }

    public void dispatch(Job job) {
        JobPayload payload = new JobPayload(job.getClass().getName(), MAPPER.writeValueAsString(job));

        redis.opsForList().rightPush(key(job.queue()), MAPPER.writeValueAsString(payload));
    }

    /**
     * Blocking rather than polling is what lets a worker sit idle at no cost and still pick work up
     * immediately.
     */
    public Job take(String queue, Duration timeout) {
        String raw = redis.opsForList().leftPop(key(queue), timeout);

        if (raw == null) {
            return null;
        }

        JobPayload payload = MAPPER.readValue(raw, JobPayload.class);

        try {
            Class<?> type = Class.forName(payload.type());

            return (Job) MAPPER.readValue(payload.body(), type);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Queued job has an unknown type: " + payload.type(), e);
        }
    }

    public long size(String queue) {
        Long size = redis.opsForList().size(key(queue));

        return size == null ? 0 : size;
    }

    private String key(String queue) {
        return prefix + queue;
    }
}
