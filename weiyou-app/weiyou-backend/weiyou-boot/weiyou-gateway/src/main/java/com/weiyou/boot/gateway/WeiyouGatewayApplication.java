package com.weiyou.boot.gateway;

import com.weiyou.boot.gateway.config.GatewayPathProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GatewayPathProperties.class)
public class WeiyouGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeiyouGatewayApplication.class, args);
    }
}
