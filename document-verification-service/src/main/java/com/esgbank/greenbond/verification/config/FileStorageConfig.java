package com.esgbank.greenbond.verification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.file-storage")
public class FileStorageConfig {

    private String uploadDir = "./uploads";
    private long maxFileSize = 10485760L; // 10MB
    private String[] allowedFileTypes = {"pdf", "doc", "docx", "jpg", "jpeg", "png"};
    private int maxDocumentsPerRequest = 10;
}