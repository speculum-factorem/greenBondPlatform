package com.esgbank.greenbond.monitoring.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI impactMonitoringOpenAPI() {
        log.info("Configuring OpenAPI documentation for Impact Monitoring Service");

        return new OpenAPI()
                .info(new Info()
                        .title("Impact Monitoring API")
                        .description("Green Bond Platform - Impact Monitoring Service")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("ESG Bank Impact Team")
                                .email("impact@esgbank.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}