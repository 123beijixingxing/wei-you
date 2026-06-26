package com.weiyou.boot.im;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.weiyou")
@MapperScan("com.weiyou.**.infra.persistence.mapper")
public class WeiyouImGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeiyouImGatewayApplication.class, args);
    }
}
