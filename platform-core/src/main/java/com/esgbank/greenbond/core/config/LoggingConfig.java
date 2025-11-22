package com.esgbank.greenbond.core.config;

import com.esgbank.greenbond.core.util.MdcUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
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
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        return filter;
    }

    @Bean
    public HandlerInterceptor mdcInterceptor() {
        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Object handler) {
                MdcUtils.initRequestContext();

                MdcUtils.setClientIp(getClientIp(request));
                MdcUtils.setSessionId(request.getSession().getId());
                MdcUtils.setUserAgent(request.getHeader("User-Agent"));

                String correlationId = request.getHeader("X-Correlation-ID");
                if (correlationId != null) {
                    MdcUtils.setCorrelationId(correlationId);
                }

                log.info("Request started: {} {} from {}",
                        request.getMethod(),
                        request.getRequestURI(),
                        MdcUtils.getClientIp());

                return true;
            }

            @Override
            public void afterCompletion(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Object handler,
                                        Exception ex) {
                try {
                    int status = response.getStatus();
                    String requestId = MdcUtils.getRequestId();

                    if (ex != null) {
                        log.error("Request completed with error: {} {} - Status: {} - RequestId: {}",
                                request.getMethod(), request.getRequestURI(), status, requestId, ex);
                    } else {
                        log.info("Request completed: {} {} - Status: {} - RequestId: {}",
                                request.getMethod(), request.getRequestURI(), status, requestId);
                    }
                } finally {
                    MdcUtils.clear();
                }
            }

            private String getClientIp(HttpServletRequest request) {
                String xfHeader = request.getHeader("X-Forwarded-For");
                if (xfHeader != null) {
                    return xfHeader.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        };
    }
}