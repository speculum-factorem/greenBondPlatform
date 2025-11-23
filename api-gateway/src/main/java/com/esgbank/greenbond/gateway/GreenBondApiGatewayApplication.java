package com.esgbank.greenbond.gateway;

import com.esgbank.greenbond.gateway.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class
})
@EnableConfigurationProperties
public class GreenBondApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreenBondApiGatewayApplication.class, args);
    }

    @Bean
    public ErrorWebExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}