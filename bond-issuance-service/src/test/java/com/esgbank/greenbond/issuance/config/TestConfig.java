package com.esgbank.greenbond.issuance.config;

import com.esgbank.greenbond.issuance.integration.BlockchainService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    public BlockchainService blockchainService() {
        return mock(BlockchainService.class);
    }
}