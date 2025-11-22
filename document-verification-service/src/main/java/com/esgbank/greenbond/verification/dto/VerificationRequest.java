package com.esgbank.greenbond.verification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request for document verification")
public class VerificationRequest {

    @NotBlank
    @Schema(description = "Verification comment")
    private String comment;

    @Schema(description = "Verification result")
    private Boolean isApproved;

    @Schema(description = "Additional verification data")
    private String verificationData;
}