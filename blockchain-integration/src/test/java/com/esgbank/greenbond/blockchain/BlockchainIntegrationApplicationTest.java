package com.esgbank.greenbond.blockchain;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestConfig.class)
class BlockchainIntegrationApplicationTest {

    @Test
    void contextLoads() {
        // Verify that the application context loads successfully
    }
}