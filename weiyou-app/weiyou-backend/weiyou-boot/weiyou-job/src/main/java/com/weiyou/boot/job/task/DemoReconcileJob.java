package com.weiyou.boot.job.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DemoReconcileJob {

    private static final Logger log = LoggerFactory.getLogger(DemoReconcileJob.class);

    @Scheduled(fixedDelay = 60000)
    public void tick() {
        log.info("weiyou job heartbeat");
    }
}
