package com.esgbank.greenbond.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@Component
public class LoggingFilter implements GatewayFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();

        return chain.filter(exchange)
                .doOnSuccessOrError((aVoid, throwable) -> {
                    long duration = System.currentTimeMillis() - startTime;
                    HttpStatus status = exchange.getResponse().getStatusCode();

                    if (throwable != null) {
                        log.error("Request failed - Status: {}, Duration: {}ms, Error: {}",
                                status, duration, throwable.getMessage());
                    } else {
                        String responseSize = exchange.getResponse().getHeaders().getFirst("Content-Length");
                        log.info("Request completed - Status: {}, Duration: {}ms, Size: {} bytes",
                                status, duration,
                                Objects.toString(responseSize, "unknown"));
                    }

                    // Log additional metrics
                    MDC.put("duration", String.valueOf(duration));
                    MDC.put("status", status != null ? String.valueOf(status.value()) : "unknown");
                });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}