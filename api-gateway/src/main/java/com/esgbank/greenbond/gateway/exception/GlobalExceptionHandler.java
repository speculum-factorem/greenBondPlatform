package com.esgbank.greenbond.gateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for API Gateway.
 * Handles all exceptions and provides consistent error responses.
 */
@Slf4j
@Order(-2)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Exception occurred: {}", ex.getMessage(), ex);

        HttpStatus status = determineHttpStatus(ex);
        Map<String, Object> errorResponse = createErrorResponse(ex, status, exchange);

        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        DataBuffer buffer = bufferFactory.wrap(serializeErrorResponse(errorResponse).getBytes(StandardCharsets.UTF_8));

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof AuthenticationException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (ex instanceof ResponseStatusException) {
            org.springframework.http.HttpStatusCode statusCode = ((ResponseStatusException) ex).getStatusCode();
            // Convert HttpStatusCode to HttpStatus
            if (statusCode instanceof HttpStatus) {
                return (HttpStatus) statusCode;
            }
            // If it's a custom status code, try to get the value and map it
            int statusValue = statusCode.value();
            try {
                return HttpStatus.valueOf(statusValue);
            } catch (IllegalArgumentException e) {
                return HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
        if (ex instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private Map<String, Object> createErrorResponse(Throwable ex, HttpStatus status, ServerWebExchange exchange) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", getErrorCode(ex));
        response.put("message", getErrorMessage(ex));
        response.put("status", status.value());
        response.put("path", exchange.getRequest().getPath().value());
        response.put("timestamp", Instant.now().toString());
        response.put("requestId", MDC.get("requestId"));

        if (log.isDebugEnabled()) {
            response.put("details", ex.getMessage());
        }

        return response;
    }

    private String getErrorCode(Throwable ex) {
        if (ex instanceof AuthenticationException) {
            return "UNAUTHORIZED";
        }
        if (ex instanceof IllegalArgumentException) {
            return "BAD_REQUEST";
        }
        if (ex instanceof ResponseStatusException) {
            return "HTTP_ERROR";
        }
        return "INTERNAL_ERROR";
    }

    private String getErrorMessage(Throwable ex) {
        if (ex instanceof AuthenticationException) {
            return "Authentication failed";
        }
        if (ex instanceof ResponseStatusException) {
            return ex.getMessage();
        }
        if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
            return ex.getMessage();
        }
        return "An unexpected error occurred";
    }

    private String serializeErrorResponse(Map<String, Object> errorResponse) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(errorResponse);
        } catch (Exception e) {
            log.error("Failed to serialize error response", e);
            return "{\"error\":\"INTERNAL_ERROR\",\"message\":\"Failed to serialize error response\"}";
        }
    }
}

