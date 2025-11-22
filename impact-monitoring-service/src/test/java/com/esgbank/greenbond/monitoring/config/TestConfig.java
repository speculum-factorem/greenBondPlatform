package com.esgbank.greenbond.monitoring.config;

import com.esgbank.greenbond.monitoring.integration.BlockchainService;
import com.esgbank.greenbond.monitoring.integration.IoTIntegrationService;
import com.influxdb.client.InfluxDBClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    public InfluxDBClient influxDBClient() {
        return mock(InfluxDBClient.class);
    }

    @Bean
    public BlockchainService blockchainService() {
        return mock(BlockchainService.class);
    }

    @Bean
    public IoTIntegrationService ioTIntegrationService() {
        return mock(IoTIntegrationService.class);
    }

    @Bean
    public WebClient webClient() {
        return mock(WebClient.class);
    }
}