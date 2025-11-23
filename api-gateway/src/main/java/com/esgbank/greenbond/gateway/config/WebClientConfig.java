package com.esgbank.greenbond.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${app.services.bond-issuance.url:http://localhost:8081}")
    private String bondIssuanceServiceUrl;

    @Value("${app.services.impact-monitoring.url:http://localhost:8084}")
    private String impactMonitoringServiceUrl;

    @Value("${app.services.document-verification.url:http://localhost:8083}")
    private String documentVerificationServiceUrl;

    @Bean
    public WebClient.Builder webClientBuilder() {
        // Настройка пула соединений для оптимизации производительности
        ConnectionProvider connectionProvider = ConnectionProvider.builder("api-gateway-pool")
                .maxConnections(500) // Максимальное количество соединений
                .maxIdleTime(Duration.ofSeconds(20)) // Время простоя соединения
                .maxLifeTime(Duration.ofSeconds(60)) // Максимальное время жизни соединения
                .pendingAcquireTimeout(Duration.ofSeconds(60)) // Таймаут ожидания свободного соединения
                .evictInBackground(Duration.ofSeconds(120)) // Период проверки неактивных соединений
                .build();

        // Настройка HTTP клиента с таймаутами
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .responseTimeout(Duration.ofSeconds(30)) // Таймаут ответа от сервера
                .followRedirect(true); // Автоматическое следование редиректам

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)); // Максимальный размер буфера 10MB
    }

    // WebClient для коммуникации с Bond Issuance Service
    @Bean("bondIssuanceWebClient")
    public WebClient bondIssuanceWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(bondIssuanceServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // WebClient для коммуникации с Impact Monitoring Service
    @Bean("impactMonitoringWebClient")
    public WebClient impactMonitoringWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(impactMonitoringServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // WebClient для коммуникации с Document Verification Service
    @Bean("documentVerificationWebClient")
    public WebClient documentVerificationWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(documentVerificationServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}

