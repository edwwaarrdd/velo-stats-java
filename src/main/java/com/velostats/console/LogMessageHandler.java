package com.velostats.console;

import com.velostats.queue.JobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogMessageHandler implements JobHandler<LogMessage> {

    private static final Logger log = LoggerFactory.getLogger(LogMessageHandler.class);

    @Override
    public Class<LogMessage> handles() {
        return LogMessage.class;
    }

    @Override
    public void handle(LogMessage job) {
        log.info("Test job says: {}", job.message());
    }
}
