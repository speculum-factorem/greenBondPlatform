package com.esgbank.greenbond.gateway.config;

import com.esgbank.greenbond.gateway.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        log.info("Configuring API Gateway security");

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Отключаем CSRF для API Gateway (stateless)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                
                // Настройка CORS для интеграции с frontend
                .cors(cors -> cors.configurationSource(exchange -> {
                    org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
                    config.setAllowedOrigins(java.util.List.of("*")); // В production указать точные origins
                    config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                    config.setAllowedHeaders(java.util.List.of("*"));
                    config.setExposedHeaders(java.util.List.of("Authorization", "X-Request-Id"));
                    config.setAllowCredentials(false); // Установить true если используются cookies
                    config.setMaxAge(3600L);
                    return config;
                }))

                .authorizeExchange(exchanges -> exchanges
                        // Публичные эндпоинты (не требуют аутентификации)
                        .pathMatchers("/actuator/health").permitAll()
                        .pathMatchers("/webjars/**", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .pathMatchers("/api/v1/auth/**").permitAll()

                        // Защищенные эндпоинты с ролевым доступом
                        .pathMatchers(HttpMethod.GET, "/api/v1/bonds/**").hasAnyRole("INVESTOR", "ISSUER", "ADMIN", "USER")
                        .pathMatchers(HttpMethod.POST, "/api/v1/bonds/**").hasAnyRole("ISSUER", "ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/bonds/**").hasAnyRole("ISSUER", "ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/bonds/**").hasAnyRole("ADMIN")
                        .pathMatchers("/api/v1/impact/**").hasAnyRole("INVESTOR", "ISSUER", "AUDITOR", "ADMIN", "USER")
                        .pathMatchers("/api/v1/documents/**").hasAnyRole("ISSUER", "AUDITOR", "ADMIN")
                        .pathMatchers("/api/v1/gateway/**").authenticated()

                        .anyExchange().authenticated()
                )

                // Добавляем JWT фильтр в цепочку безопасности
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
