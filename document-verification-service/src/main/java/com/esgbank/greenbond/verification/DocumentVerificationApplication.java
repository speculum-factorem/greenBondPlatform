package com.esgbank.greenbond.verification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties
public class DocumentVerificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentVerificationApplication.class, args);
        log.info("Document Verification Service started successfully");
    }
}