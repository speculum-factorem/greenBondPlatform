package com.esgbank.greenbond.verification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentField {

    private String fieldName;
    private String fieldValue;
    private Double confidence;
    private String dataType;
    private Boolean isVerified;
    private String verificationSource;
}