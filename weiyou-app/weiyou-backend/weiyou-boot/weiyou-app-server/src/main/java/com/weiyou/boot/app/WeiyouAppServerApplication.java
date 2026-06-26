package com.weiyou.boot.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.weiyou")
@MapperScan("com.weiyou.**.infra.persistence.mapper")
public class WeiyouAppServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeiyouAppServerApplication.class, args);
    }
}
