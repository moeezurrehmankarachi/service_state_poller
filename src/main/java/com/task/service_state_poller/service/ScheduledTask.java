package com.task.service_state_poller.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class ScheduledTask {
    Logger logger = LoggerFactory.getLogger(ExternalServiceServiceImplementation.class);

    @Autowired
    private ExternalServiceService externalServiceService;

    public ScheduledTask(ExternalServiceService externalServiceService) {
        this.externalServiceService = externalServiceService;
    }

    @Scheduled(cron = "${cron.expression}")
    public void scheduleFixedDelayTask() {
        logger.info("Starting Scheduled Task");
        externalServiceService.monitorExternalServices();
    }
}
