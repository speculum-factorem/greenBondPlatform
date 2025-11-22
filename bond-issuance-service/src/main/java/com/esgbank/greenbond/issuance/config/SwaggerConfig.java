package com.esgbank.greenbond.issuance.config;

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
    public OpenAPI bondIssuanceOpenAPI() {
        log.info("Configuring OpenAPI documentation for Bond Issuance Service");

        return new OpenAPI()
                .info(new Info()
                        .title("Bond Issuance API")
                        .description("Green Bond Platform - Bond Issuance Service")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("ESG Bank Bond Team")
                                .email("bonds@esgbank.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}