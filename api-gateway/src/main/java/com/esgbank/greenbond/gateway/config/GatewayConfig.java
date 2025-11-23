package com.esgbank.greenbond.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.esgbank.greenbond.gateway.filter.MDCFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final MDCFilter mdcFilter;

    @Value("${app.gateway.use-direct-urls:false}")
    private boolean useDirectUrls;

    @Value("${app.services.bond-issuance.url:http://localhost:8081}")
    private String bondIssuanceServiceUrl;

    @Value("${app.services.impact-monitoring.url:http://localhost:8084}")
    private String impactMonitoringServiceUrl;

    @Value("${app.services.document-verification.url:http://localhost:8083}")
    private String documentVerificationServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info("Configuring API Gateway routes (useDirectUrls: {})", useDirectUrls);

        // Определяем URI для сервисов: либо прямые URL, либо через Eureka Service Discovery
        String bondIssuanceUri = useDirectUrls ? bondIssuanceServiceUrl : "lb://bond-issuance-service";
        String impactMonitoringUri = useDirectUrls ? impactMonitoringServiceUrl : "lb://impact-monitoring-service";
        String documentVerificationUri = useDirectUrls ? documentVerificationServiceUrl : "lb://document-verification-service";

        log.info("Bond Issuance Service URI: {}", bondIssuanceUri);
        log.info("Impact Monitoring Service URI: {}", impactMonitoringUri);
        log.info("Document Verification Service URI: {}", documentVerificationUri);

        return builder.routes()
                // Маршрут для Bond Issuance Service
                .route("bond-issuance-service", r -> r.path("/api/v1/bonds/**")
                        .and().method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                        .filters(f -> f
                                .filter(mdcFilter) // Добавляем MDC фильтр для трейсинга
                                .rewritePath("/api/v1/bonds/(?<segment>.*)", "/api/v1/bonds/${segment}")
                                .addRequestHeader("X-Service-Name", "bond-issuance")
                                .retry(retryConfig -> retryConfig // Настройка retry для надежности
                                        .setRetries(2)
                                        .setMethods(HttpMethod.GET, HttpMethod.POST)
                                        .setBackoff(java.time.Duration.ofMillis(100), java.time.Duration.ofMillis(1000), 2, false))
                        )
                        .uri(bondIssuanceUri))

                // Маршрут для Impact Monitoring Service
                .route("impact-monitoring-service", r -> r.path("/api/v1/impact/**")
                        .and().method(HttpMethod.GET, HttpMethod.POST)
                        .filters(f -> f
                                .filter(mdcFilter) // Добавляем MDC фильтр для трейсинга
                                .rewritePath("/api/v1/impact/(?<segment>.*)", "/api/v1/impact/${segment}")
                                .addRequestHeader("X-Service-Name", "impact-monitoring")
                                .retry(retryConfig -> retryConfig // Настройка retry для надежности
                                        .setRetries(2)
                                        .setMethods(HttpMethod.GET)
                                        .setBackoff(java.time.Duration.ofMillis(100), java.time.Duration.ofMillis(1000), 2, false))
                        )
                        .uri(impactMonitoringUri))

                // Маршрут для Document Verification Service
                .route("document-verification-service", r -> r.path("/api/v1/documents/**")
                        .and().method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT)
                        .filters(f -> f
                                .filter(mdcFilter) // Добавляем MDC фильтр для трейсинга
                                .rewritePath("/api/v1/documents/(?<segment>.*)", "/api/v1/documents/${segment}")
                                .addRequestHeader("X-Service-Name", "document-verification")
                                .retry(retryConfig -> retryConfig // Настройка retry для надежности
                                        .setRetries(2)
                                        .setMethods(HttpMethod.GET)
                                        .setBackoff(java.time.Duration.ofMillis(100), java.time.Duration.ofMillis(1000), 2, false))
                        )
                        .uri(documentVerificationUri))

                // Маршруты для Swagger документации
                .route("bond-issuance-swagger", r -> r.path("/v3/api-docs/issuance")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri(bondIssuanceUri))

                .route("impact-monitoring-swagger", r -> r.path("/v3/api-docs/monitoring")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri(impactMonitoringUri))

                .route("document-verification-swagger", r -> r.path("/v3/api-docs/verification")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri(documentVerificationUri))
                .build();
    }
}