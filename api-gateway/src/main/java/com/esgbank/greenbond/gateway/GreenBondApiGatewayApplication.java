package com.esgbank.greenbond.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class GreenBondApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreenBondApiGatewayApplication.class, args);
    }
}