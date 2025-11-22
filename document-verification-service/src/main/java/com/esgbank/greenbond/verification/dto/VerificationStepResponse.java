package com.esgbank.greenbond.verification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Verification step response")
public class VerificationStepResponse {

    @Schema(description = "Step name")
    private String stepName;

    @Schema(description = "Status")
    private String status;

    @Schema(description = "Performed by")
    private String performedBy;

    @Schema(description = "Performed at")
    private LocalDateTime performedAt;

    @Schema(description = "Comments")
    private String comments;

    @Schema(description = "Details")
    private Map<String, Object> details;
}