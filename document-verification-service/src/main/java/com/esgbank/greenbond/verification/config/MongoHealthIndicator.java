package com.esgbank.greenbond.verification.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for MongoDB connectivity.
 */
@Slf4j
@Component
public class MongoHealthIndicator implements HealthIndicator {

    private final MongoTemplate mongoTemplate;

    public MongoHealthIndicator(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Health health() {
        try {
            // Perform a simple operation to check MongoDB connection
            mongoTemplate.getDb().getName();
            log.debug("MongoDB connection is healthy");
            return Health.up()
                    .withDetail("database", "MongoDB")
                    .withDetail("status", "Connected")
                    .withDetail("databaseName", mongoTemplate.getDb().getName())
                    .build();
        } catch (Exception e) {
            log.error("MongoDB health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("database", "MongoDB")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}

