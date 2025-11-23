package com.esgbank.greenbond.verification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationStep {

    private String stepName;
    private String status;
    private String performedBy;
    private LocalDateTime performedAt;
    private String comments;
    private Map<String, Object> details;
}