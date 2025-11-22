package com.esgbank.greenbond.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import java.util.UUID;

@Slf4j
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info("Configuring API Gateway routes");

        return builder.routes()
                // Bond Issuance Service
                .route("bond-issuance-service", r -> r.path("/api/v1/bonds/**")
                        .and().method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                        .filters(f -> f
                                .prefixPath("/issuance")
                                .filter(new MDCFilter())
                                .rewritePath("/api/v1/bonds/(?<segment>.*)", "/api/v1/bonds/${segment}")
                                .addRequestHeader("X-Service-Name", "bond-issuance")
                        )
                        .uri("lb://bond-issuance-service"))

                // Impact Monitoring Service
                .route("impact-monitoring-service", r -> r.path("/api/v1/impact/**")
                        .and().method(HttpMethod.GET, HttpMethod.POST)
                        .filters(f -> f
                                .prefixPath("/monitoring")
                                .filter(new MDCFilter())
                                .rewritePath("/api/v1/impact/(?<segment>.*)", "/api/v1/impact/${segment}")
                                .addRequestHeader("X-Service-Name", "impact-monitoring")
                        )
                        .uri("lb://impact-monitoring-service"))

                // Document Verification Service
                .route("document-verification-service", r -> r.path("/api/v1/documents/**")
                        .and().method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT)
                        .filters(f -> f
                                .prefixPath("/verification")
                                .filter(new MDCFilter())
                                .rewritePath("/api/v1/documents/(?<segment>.*)", "/api/v1/documents/${segment}")
                                .addRequestHeader("X-Service-Name", "document-verification")
                        )
                        .uri("lb://document-verification-service"))

                // Swagger Documentation routes
                .route("bond-issuance-swagger", r -> r.path("/v3/api-docs/issuance")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri("lb://bond-issuance-service"))

                .route("impact-monitoring-swagger", r -> r.path("/v3/api-docs/monitoring")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri("lb://impact-monitoring-service"))

                .route("document-verification-swagger", r -> r.path("/v3/api-docs/verification")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri("lb://document-verification-service"))
                .build();
    }
}