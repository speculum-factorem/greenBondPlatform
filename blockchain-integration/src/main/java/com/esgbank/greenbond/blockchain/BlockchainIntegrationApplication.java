package com.esgbank.greenbond.blockchain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class BlockchainIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlockchainIntegrationApplication.class, args);
        log.info("Blockchain Integration Service started successfully");
    }
}