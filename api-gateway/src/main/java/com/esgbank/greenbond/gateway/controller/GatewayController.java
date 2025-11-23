package com.esgbank.greenbond.gateway.controller;

// Временно отключено из-за проблем с совместимостью зависимостей
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/gateway")
@RequiredArgsConstructor
//@Tag(name = "Gateway", description = "API Gateway management endpoints")
public class GatewayController {

    @GetMapping("/health")
    //@Operation(summary = "Gateway health check", description = "Check API Gateway status")
    public Mono<ResponseEntity<Map<String, String>>> health() {
        log.debug("Health check request");
        return Mono.just(ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "api-gateway",
                "timestamp", String.valueOf(System.currentTimeMillis())
        )));
    }

    @GetMapping("/info")
    //@Operation(summary = "Gateway information", description = "Get API Gateway information and routes")
    public Mono<ResponseEntity<Map<String, Object>>> info() {
        log.debug("Gateway info request");
        return Mono.just(ResponseEntity.ok(Map.of(
                "name", "Green Bond Platform API Gateway",
                "version", "1.0.0",
                "description", "Digitally Native Green Bond Platform",
                "routes", Map.of(
                        "bond-issuance", "/api/v1/bonds/**",
                        "impact-monitoring", "/api/v1/impact/**",
                        "document-verification", "/api/v1/documents/**"
                )
        )));
    }
}