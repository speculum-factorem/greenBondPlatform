package com.esgbank.greenbond.core.util;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;

import java.util.Map;
import java.util.UUID;

/**
 * Utility class for MDC (Mapped Diagnostic Context) operations
 */
@UtilityClass
public class MdcUtils {

    public static final String REQUEST_ID_KEY = "requestId";
    public static final String SESSION_ID_KEY = "sessionId";
    public static final String CLIENT_IP_KEY = "clientIp";
    public static final String USER_AGENT_KEY = "userAgent";
    public static final String USER_ID_KEY = "userId";
    public static final String CORRELATION_ID_KEY = "correlationId";

    public static void put(String key, String value) {
        if (key != null && value != null) {
            MDC.put(key, value);
        }
    }

    public static String get(String key) {
        return MDC.get(key);
    }

    public static void remove(String key) {
        MDC.remove(key);
    }

    public static void clear() {
        MDC.clear();
    }

    public static String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    public static void initRequestContext() {
        clear();
        put(REQUEST_ID_KEY, generateRequestId());
    }

    public static void initRequestContext(String requestId) {
        clear();
        put(REQUEST_ID_KEY, requestId != null ? requestId : generateRequestId());
    }

    public static void setRequestContext(Map<String, String> context) {
        clear();
        if (context != null) {
            context.forEach(MdcUtils::put);
        }
    }

    public static Map<String, String> getCopyOfContextMap() {
        MDCAdapter mdcAdapter = MDC.getMDCAdapter();
        if (mdcAdapter != null) {
            return mdcAdapter.getCopyOfContextMap();
        }
        return Map.of();
    }

    public static void setUserId(String userId) {
        put(USER_ID_KEY, userId);
    }

    public static void setClientIp(String clientIp) {
        put(CLIENT_IP_KEY, clientIp);
    }

    public static void setSessionId(String sessionId) {
        put(SESSION_ID_KEY, sessionId);
    }

    public static void setUserAgent(String userAgent) {
        put(USER_AGENT_KEY, userAgent);
    }

    public static void setCorrelationId(String correlationId) {
        put(CORRELATION_ID_KEY, correlationId);
    }

    public static String getRequestId() {
        return get(REQUEST_ID_KEY);
    }

    public static String getUserId() {
        return get(USER_ID_KEY);
    }

    public static String getClientIp() {
        return get(CLIENT_IP_KEY);
    }

    public static Runnable wrapWithMdc(Runnable runnable) {
        Map<String, String> context = getCopyOfContextMap();
        return () -> {
            Map<String, String> previous = getCopyOfContextMap();
            try {
                setRequestContext(context);
                runnable.run();
            } finally {
                setRequestContext(previous);
            }
        };
    }
}