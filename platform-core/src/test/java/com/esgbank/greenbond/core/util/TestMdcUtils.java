package com.esgbank.greenbond.core.util;

import org.slf4j.MDC;

/**
 * Utility class for managing MDC (Mapped Diagnostic Context) in tests.
 * Provides methods to set up and clear MDC context for consistent logging during tests.
 */
public class TestMdcUtils {

    private static final String REQUEST_ID_KEY = "requestId";
    private static final String USERNAME_KEY = "username";
    private static final String CLIENT_IP_KEY = "clientIp";

    /**
     * Sets up test MDC context with default values.
     * This ensures that tests have consistent logging context.
     */
    public static void setupTestMdc() {
        MDC.put(REQUEST_ID_KEY, "test-request-" + System.currentTimeMillis());
        MDC.put(USERNAME_KEY, "test-user");
        MDC.put(CLIENT_IP_KEY, "127.0.0.1");
    }

    /**
     * Sets up test MDC context with custom values.
     *
     * @param requestId Custom request ID
     * @param username Custom username
     * @param clientIp Custom client IP
     */
    public static void setupTestMdc(String requestId, String username, String clientIp) {
        if (requestId != null) {
            MDC.put(REQUEST_ID_KEY, requestId);
        }
        if (username != null) {
            MDC.put(USERNAME_KEY, username);
        }
        if (clientIp != null) {
            MDC.put(CLIENT_IP_KEY, clientIp);
        }
    }

    /**
     * Clears all MDC context.
     * Should be called in @AfterEach or @AfterAll methods to clean up test context.
     */
    public static void clearTestMdc() {
        MDC.clear();
    }

    /**
     * Removes a specific key from MDC.
     *
     * @param key The key to remove
     */
    public static void removeMdcKey(String key) {
        MDC.remove(key);
    }

    /**
     * Gets the current request ID from MDC.
     *
     * @return The request ID or null if not set
     */
    public static String getRequestId() {
        return MDC.get(REQUEST_ID_KEY);
    }

    /**
     * Gets the current username from MDC.
     *
     * @return The username or null if not set
     */
    public static String getUsername() {
        return MDC.get(USERNAME_KEY);
    }
}

