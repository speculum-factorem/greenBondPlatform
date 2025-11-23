package com.esgbank.greenbond.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@Component
public class LoggingFilter implements GatewayFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Засекаем время начала обработки запроса для измерения производительности
        long startTime = System.currentTimeMillis();

        // Продолжаем цепочку фильтров и логируем результат
        return chain.filter(exchange)
                .doOnSuccess(aVoid -> {
                    // Логируем успешное завершение запроса
                    logRequestCompletion(exchange, startTime, null);
                })
                .doOnError(throwable -> {
                    // Логируем ошибку при обработке запроса
                    logRequestCompletion(exchange, startTime, throwable);
                });
    }

    // Логирование завершения запроса с метриками производительности
    private void logRequestCompletion(ServerWebExchange exchange, long startTime, Throwable throwable) {
        // Вычисляем время обработки запроса
        long duration = System.currentTimeMillis() - startTime;
        HttpStatusCode status = exchange.getResponse().getStatusCode();

        if (throwable != null) {
            // Логируем ошибку с метриками
            log.error("Request failed - Status: {}, Duration: {}ms, Error: {}",
                    status, duration, throwable.getMessage());
        } else {
            // Логируем успешное завершение с размером ответа
            String responseSize = exchange.getResponse().getHeaders().getFirst("Content-Length");
            log.info("Request completed - Status: {}, Duration: {}ms, Size: {} bytes",
                    status, duration,
                    Objects.toString(responseSize, "unknown"));
        }

        // Добавляем метрики в MDC для структурированного логирования
        MDC.put("duration", String.valueOf(duration));
        MDC.put("status", status != null ? String.valueOf(status.value()) : "unknown");
    }

    @Override
    public int getOrder() {
        // Низший приоритет - этот фильтр должен выполняться последним
        return Ordered.LOWEST_PRECEDENCE;
    }
}