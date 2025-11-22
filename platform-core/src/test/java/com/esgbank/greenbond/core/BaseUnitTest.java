package com.esgbank.greenbond.core;

import com.esgbank.greenbond.core.util.TestMdcUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class BaseUnitTest {

    @BeforeEach
    void setUpBase() {
        TestMdcUtils.setupTestMdc();
    }

    @AfterEach
    void tearDownBase() {
        TestMdcUtils.clearTestMdc();
    }
}