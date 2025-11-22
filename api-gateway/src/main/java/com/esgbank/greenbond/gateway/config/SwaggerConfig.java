package com.esgbank.greenbond.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI greenBondOpenAPI() {
        log.info("Configuring OpenAPI documentation for Green Bond Platform");

        return new OpenAPI()
                .info(new Info()
                        .title("Green Bond Platform API")
                        .description("Digitally Native Green Bond Platform - API Gateway")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("ESG Bank API Support")
                                .email("api-support@esgbank.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("/issuance").description("Bond Issuance Service"),
                        new Server().url("/monitoring").description("Impact Monitoring Service"),
                        new Server().url("/verification").description("Document Verification Service")
                ));
    }

    @Bean
    public SwaggerUiConfigProperties swaggerUiConfig() {
        SwaggerUiConfigProperties config = new SwaggerUiConfigProperties();
        config.setUrlsPrimaryName("Gateway APIs");
        return config;
    }
}