package com.esgbank.greenbond.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:Green Bond Platform}")
    private String applicationName;

    @Value("${spring.application.version:1.0.0}")
    private String applicationVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName)
                        .version(applicationVersion)
                        .description("""
                            Digital Native Green Bond Platform API
                            
                            This platform provides end-to-end digital issuance and management 
                            of green bonds with blockchain integration for transparency and trust.
                            """)
                        .contact(new Contact()
                                .name("ESG Bank API Support")
                                .email("api-support@esgbank.com")
                                .url("https://esgbank.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://esgbank.com/terms")))
                .servers(List.of(
                        new Server()
                                .url("https://api.esgbank.com")
                                .description("Production Server"),
                        new Server()
                                .url("https://api-staging.esgbank.com")
                                .description("Staging Server"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")
                ));
    }
}