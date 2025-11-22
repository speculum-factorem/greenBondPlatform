package com.esgbank.greenbond.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class MDCFilter implements GatewayFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            // Clear and setup MDC
            MDC.clear();

            String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-ID");
            if (requestId == null) {
                requestId = UUID.randomUUID().toString();
            }

            String clientIp = getClientIp(exchange.getRequest());
            String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");

            MDC.put("requestId", requestId);
            MDC.put("clientIp", clientIp);
            MDC.put("userAgent", userAgent != null ? userAgent : "unknown");
            MDC.put("path", exchange.getRequest().getPath().value());
            MDC.put("method", exchange.getRequest().getMethod().name());

            log.info("Incoming request: {} {} from {}",
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getPath(),
                    clientIp);

            // Add request ID to response headers
            exchange.getResponse().getHeaders().add("X-Request-ID", requestId);

            return chain.filter(exchange)
                    .doOnSuccess(v -> log.info("Request completed successfully"))
                    .doOnError(e -> log.error("Request failed with error: {}", e.getMessage()))
                    .doFinally(signalType -> {
                        log.info("Request processing completed with signal: {}", signalType);
                        MDC.clear();
                    });

        } catch (Exception e) {
            log.error("Error in MDC filter: {}", e.getMessage());
            MDC.clear();
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private String getClientIp(ServerHttpRequest request) {
        String xfHeader = request.getHeaders().getFirst("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }
}