package com.esgbank.greenbond.monitoring.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "app.influxdb")
public class InfluxDBProperties {
    private String url;
    private String token;
    private String org;
    private String bucket;
    private int batchSize = 1000;
    private int flushInterval = 1000;
}