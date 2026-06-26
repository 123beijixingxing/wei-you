package com.weiyou.boot.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.weiyou.boot.job", "com.weiyou.common"})
public class WeiyouJobApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeiyouJobApplication.class, args);
    }
}
