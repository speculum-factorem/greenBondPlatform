package com.esgbank.greenbond.core;

import com.esgbank.greenbond.core.util.TestMdcUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @BeforeEach
    void setUpBase() {
        TestMdcUtils.setupTestMdc();
    }

    @AfterEach
    void tearDownBase() {
        TestMdcUtils.clearTestMdc();
    }
}