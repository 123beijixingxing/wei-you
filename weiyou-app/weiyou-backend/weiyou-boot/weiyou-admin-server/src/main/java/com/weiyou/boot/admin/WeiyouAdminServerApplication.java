package com.weiyou.boot.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.weiyou.boot.admin", "com.weiyou.common"})
public class WeiyouAdminServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeiyouAdminServerApplication.class, args);
    }
}
