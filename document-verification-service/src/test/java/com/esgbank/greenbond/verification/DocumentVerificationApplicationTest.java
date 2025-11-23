package com.esgbank.greenbond.verification;

import com.esgbank.greenbond.verification.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class DocumentVerificationApplicationTest {

    @Test
    void contextLoads() {
        // Verify that the application context loads successfully
    }
}