package com.esgbank.greenbond.monitoring.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class InfluxDBConfig {

    @Value("${app.influxdb.url:http://localhost:8086}")
    private String influxUrl;

    @Value("${app.influxdb.token:my-token}")
    private String influxToken;

    @Value("${app.influxdb.org:esgbank}")
    private String influxOrg;

    @Value("${app.influxdb.bucket:impact-metrics}")
    private String influxBucket;

    @Bean
    public InfluxDBClient influxDBClient() {
        log.info("Initializing InfluxDB connection to: {}", influxUrl);

        try {
            InfluxDBClient client = InfluxDBClientFactory.create(influxUrl, influxToken.toCharArray(), influxOrg, influxBucket);
            log.info("Successfully connected to InfluxDB");
            return client;
        } catch (Exception e) {
            log.error("Failed to connect to InfluxDB: {}", e.getMessage());
            throw new RuntimeException("InfluxDB connection failed", e);
        }
    }

    @Bean
    public InfluxDBProperties influxDBProperties() {
        return InfluxDBProperties.builder()
                .url(influxUrl)
                .token(influxToken)
                .org(influxOrg)
                .bucket(influxBucket)
                .build();
    }
}