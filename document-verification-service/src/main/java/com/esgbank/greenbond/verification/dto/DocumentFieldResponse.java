package com.esgbank.greenbond.verification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Document field response")
public class DocumentFieldResponse {

    @Schema(description = "Field name")
    private String fieldName;

    @Schema(description = "Field value")
    private String fieldValue;

    @Schema(description = "Confidence score")
    private Double confidence;

    @Schema(description = "Data type")
    private String dataType;

    @Schema(description = "Is verified")
    private Boolean isVerified;

    @Schema(description = "Verification source")
    private String verificationSource;
}