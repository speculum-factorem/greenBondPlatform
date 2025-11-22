package com.esgbank.greenbond.monitoring.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Slf4j
@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.esgbank.greenbond.monitoring.repository")
public class MongoConfig {

    // MongoDB configuration is handled by application.yml
    // This class enables MongoDB auditing and repository scanning
}