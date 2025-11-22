package com.esgbank.greenbond.monitoring.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

@Configuration
public class LoggingConfig {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeClientInfo(true);
        filter.setIncludeHeaders(true);
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        return filter;
    }

    @Bean
    public Filter mdcFilter() {
        return (request, response, chain) -> {
            try {
                setupMDC((HttpServletRequest) request);
                chain.doFilter(request, response);
            } finally {
                MDC.clear();
            }
        };
    }

    private void setupMDC(HttpServletRequest request) {
        MDC.clear();
        MDC.put("requestId", getOrGenerateRequestId(request));
        MDC.put("clientIp", getClientIp(request));
        MDC.put("userAgent", request.getHeader("User-Agent"));
        MDC.put("method", request.getMethod());
        MDC.put("path", request.getRequestURI());
        MDC.put("userId", request.getHeader("X-User-Name"));
        MDC.put("userRoles", request.getHeader("X-User-Roles"));
    }

    private String getOrGenerateRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        return requestId != null ? requestId : UUID.randomUUID().toString();
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}