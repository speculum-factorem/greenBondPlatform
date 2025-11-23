package com.esgbank.greenbond.core.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @BeforeEach
    void setUp() {
        TestMdcUtils.setupTestMdc();
    }

    @Test
    void shouldValidateEmail() {
        assertTrue(ValidationUtils.isValidEmail("test@example.com"));
        assertTrue(ValidationUtils.isValidEmail("user.name+tag@domain.co.uk"));
        assertFalse(ValidationUtils.isValidEmail("invalid-email"));
        assertFalse(ValidationUtils.isValidEmail(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0x742d35Cc6634C0532925a3b8Dc9F5a4B5a7F8E9C",
            "0xABC123def456DEF789abc012ABC123def456DEF7"
    })
    void shouldValidateValidEthereumAddresses(String address) {
        assertTrue(ValidationUtils.isValidEthereumAddress(address));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0xinvalid",
            "742d35Cc6634C0532925a3b8Dc9F5a4B5a7F8E9C", // missing 0x
            "0x742d35Cc6634C0532925a3b8Dc9F5a4B5a7F8E9" // too short
    })
    void shouldRejectInvalidEthereumAddresses(String address) {
        assertFalse(ValidationUtils.isValidEthereumAddress(address));
    }

    @Test
    void shouldRejectNullEthereumAddress() {
        assertFalse(ValidationUtils.isValidEthereumAddress(null));
    }

    @Test
    void shouldValidatePhoneNumbers() {
        assertTrue(ValidationUtils.isValidPhone("+1234567890"));
        assertTrue(ValidationUtils.isValidPhone("+441234567890"));
        assertTrue(ValidationUtils.isValidPhone("1234567890")); // valid without +
        assertTrue(ValidationUtils.isValidPhone("12345")); // valid (starts with 1-9, has at least 1 more digit)
        assertFalse(ValidationUtils.isValidPhone("012345")); // invalid (starts with 0)
        assertFalse(ValidationUtils.isValidPhone("+abc123")); // invalid characters
        assertFalse(ValidationUtils.isValidPhone(null));
    }

    @Test
    void shouldValidateUrls() {
        assertTrue(ValidationUtils.isValidUrl("https://example.com"));
        assertTrue(ValidationUtils.isValidUrl("http://localhost:8080"));
        assertFalse(ValidationUtils.isValidUrl("not-a-url"));
        assertFalse(ValidationUtils.isValidUrl(null));
    }

    @Test
    void shouldValidateNumbers() {
        assertTrue(ValidationUtils.isPositiveNumber(123));
        assertTrue(ValidationUtils.isPositiveNumber(0.5));
        assertFalse(ValidationUtils.isPositiveNumber(0));
        assertFalse(ValidationUtils.isPositiveNumber(-1));
        assertFalse(ValidationUtils.isPositiveNumber(null));

        assertTrue(ValidationUtils.isNonNegativeNumber(0));
        assertTrue(ValidationUtils.isNonNegativeNumber(123));
        assertFalse(ValidationUtils.isNonNegativeNumber(-1));
        assertFalse(ValidationUtils.isNonNegativeNumber(null));
    }

    @Test
    void shouldValidateText() {
        assertTrue(ValidationUtils.hasText("text"));
        assertFalse(ValidationUtils.hasText(" "));
        assertFalse(ValidationUtils.hasText(null));

        assertTrue(ValidationUtils.hasMinLength("text", 3));
        assertFalse(ValidationUtils.hasMinLength("ab", 3));
        assertFalse(ValidationUtils.hasMinLength(null, 3));

        assertTrue(ValidationUtils.hasMaxLength("text", 10));
        assertFalse(ValidationUtils.hasMaxLength("very long text", 5));
        assertFalse(ValidationUtils.hasMaxLength(null, 5));
    }
}