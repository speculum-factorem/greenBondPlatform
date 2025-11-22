package com.esgbank.greenbond.core.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.regex.Pattern;

@Slf4j
@UtilityClass
public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[1-9]\\d{1,14}$");

    private static final Pattern URL_PATTERN =
            Pattern.compile("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$");

    private static final Pattern ETHEREUM_ADDRESS_PATTERN =
            Pattern.compile("^0x[a-fA-F0-9]{40}$");

    public static boolean isValidEmail(String email) {
        if (email == null) {
            log.debug("Email validation failed: null value. RequestId: {}", MDC.get("requestId"));
            return false;
        }
        boolean valid = EMAIL_PATTERN.matcher(email).matches();
        if (!valid) {
            log.debug("Email validation failed for: {}. RequestId: {}", email, MDC.get("requestId"));
        }
        return valid;
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidUrl(String url) {
        if (url == null) {
            return false;
        }
        return URL_PATTERN.matcher(url).matches();
    }

    public static boolean isValidEthereumAddress(String address) {
        if (address == null) {
            log.debug("Ethereum address validation failed: null value. RequestId: {}", MDC.get("requestId"));
            return false;
        }
        boolean valid = ETHEREUM_ADDRESS_PATTERN.matcher(address).matches();
        if (!valid) {
            log.debug("Ethereum address validation failed for: {}. RequestId: {}", address, MDC.get("requestId"));
        }
        return valid;
    }

    public static boolean isPositiveNumber(Number number) {
        return number != null && number.doubleValue() > 0;
    }

    public static boolean isNonNegativeNumber(Number number) {
        return number != null && number.doubleValue() >= 0;
    }

    public static boolean isInRange(Number number, Number min, Number max) {
        if (number == null) return false;
        double value = number.doubleValue();
        return value >= min.doubleValue() && value <= max.doubleValue();
    }

    public static boolean hasText(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean hasMinLength(String text, int minLength) {
        return text != null && text.length() >= minLength;
    }

    public static boolean hasMaxLength(String text, int maxLength) {
        return text != null && text.length() <= maxLength;
    }

    public static boolean isFutureTimestamp(java.time.temporal.TemporalAccessor timestamp) {
        if (timestamp == null) return false;
        return java.time.Instant.from(timestamp).isAfter(java.time.Instant.now());
    }

    public static boolean isPastTimestamp(java.time.temporal.TemporalAccessor timestamp) {
        if (timestamp == null) return false;
        return java.time.Instant.from(timestamp).isBefore(java.time.Instant.now());
    }
}