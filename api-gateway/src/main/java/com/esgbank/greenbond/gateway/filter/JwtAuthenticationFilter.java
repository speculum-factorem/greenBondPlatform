package com.esgbank.greenbond.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    @Value("${app.jwt.secret:defaultSecretKeyForJWTTokenGenerationInGreenBondPlatform}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        log.debug("Processing JWT authentication for request: {}", exchange.getRequest().getPath());

        // Извлекаем токен из заголовка Authorization
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // Если токена нет, пропускаем запрос дальше (для публичных эндпоинтов)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No JWT token found in request");
            return chain.filter(exchange);
        }

        // Извлекаем токен (убираем префикс "Bearer ")
        String token = authHeader.substring(7);

        try {
            // Валидируем токен и извлекаем claims
            Claims claims = validateToken(token);
            String username = claims.getSubject();
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);

            log.info("Authenticated user: {} with roles: {}", username, roles);

            // Преобразуем роли в GrantedAuthority для Spring Security
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            // Создаем объект аутентификации для Spring Security
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            // Добавляем информацию о пользователе в MDC для логирования
            MDC.put("username", username);
            MDC.put("roles", String.join(",", roles));

            // Добавляем информацию о пользователе в заголовки для передачи в downstream сервисы
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", claims.get("userId", String.class))
                    .header("X-User-Name", username)
                    .header("X-User-Roles", String.join(",", roles))
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

            // Продолжаем цепочку фильтров с установленным контекстом безопасности
            return chain.filter(mutatedExchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

        } catch (Exception e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            // Если токен невалиден, возвращаем 401 Unauthorized
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private Claims validateToken(String token) {
        // Создаем секретный ключ из конфигурации для проверки подписи токена
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        // Парсим и валидируем токен, извлекаем claims
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}