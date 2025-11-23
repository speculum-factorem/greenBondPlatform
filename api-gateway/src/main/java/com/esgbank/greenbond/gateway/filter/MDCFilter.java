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
            // Очищаем MDC перед установкой новых значений
            MDC.clear();

            // Получаем requestId из заголовка или генерируем новый
            String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-ID");
            if (requestId == null) {
                requestId = UUID.randomUUID().toString();
            }

            // Извлекаем информацию о клиенте для логирования
            String clientIp = getClientIp(exchange.getRequest());
            String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");

            // Устанавливаем значения в MDC для трейсинга запроса через все сервисы
            MDC.put("requestId", requestId);
            MDC.put("clientIp", clientIp);
            MDC.put("userAgent", userAgent != null ? userAgent : "unknown");
            MDC.put("path", exchange.getRequest().getPath().value());
            MDC.put("method", exchange.getRequest().getMethod().name());

            log.info("Incoming request: {} {} from {}",
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getPath(),
                    clientIp);

            // Добавляем requestId в заголовки ответа для клиента
            exchange.getResponse().getHeaders().add("X-Request-ID", requestId);

            // Продолжаем цепочку фильтров и очищаем MDC после завершения
            return chain.filter(exchange)
                    .doOnSuccess(v -> log.info("Request completed successfully"))
                    .doOnError(e -> log.error("Request failed with error: {}", e.getMessage()))
                    .doFinally(signalType -> {
                        log.info("Request processing completed with signal: {}", signalType);
                        MDC.clear(); // Очищаем MDC после обработки запроса
                    });

        } catch (Exception e) {
            log.error("Error in MDC filter: {}", e.getMessage());
            MDC.clear();
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        // Высший приоритет - этот фильтр должен выполняться первым
        return Ordered.HIGHEST_PRECEDENCE;
    }

    // Извлечение реального IP адреса клиента (учитывает прокси и load balancer)
    private String getClientIp(ServerHttpRequest request) {
        // Проверяем заголовок X-Forwarded-For (используется за прокси/load balancer)
        String xfHeader = request.getHeaders().getFirst("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0].trim();
        }
        // Если заголовка нет - берем IP напрямую из запроса
        return request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }
}
