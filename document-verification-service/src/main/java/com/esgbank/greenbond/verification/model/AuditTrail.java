package com.esgbank.greenbond.verification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_trails")
public class AuditTrail {

    @Id
    private String id;

    @Indexed
    private String documentId;

    @Indexed
    private String bondId;

    private String action;

    private String performedBy;

    private String userRole;

    private LocalDateTime performedAt;

    private String description;

    private Map<String, Object> oldValues;

    private Map<String, Object> newValues;

    private String ipAddress;

    private String userAgent;
}