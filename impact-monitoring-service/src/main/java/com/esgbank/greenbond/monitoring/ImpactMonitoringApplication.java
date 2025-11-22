package com.esgbank.greenbond.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties
@EnableAsync
@EnableScheduling
public class ImpactMonitoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImpactMonitoringApplication.class, args);
        log.info("Impact Monitoring Service started successfully");
    }
}