package com.esgbank.greenbond.monitoring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class ImpactMonitoringApplicationTest {

    @Test
    void contextLoads() {
        // Verify that the application context loads successfully
    }
}