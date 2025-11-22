package com.esgbank.greenbond.monitoring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data quality information")
public class DataQualityResponse {

    @Schema(description = "Confidence score (0-1)")
    private Double confidenceScore;

    @Schema(description = "Is verified")
    private Boolean isVerified;

    @Schema(description = "Verification method")
    private String verificationMethod;

    @Schema(description = "Number of data points")
    private Integer dataPoints;

    @Schema(description = "Standard deviation")
    private Double standardDeviation;

    @Schema(description = "Quality status")
    private String qualityStatus;

    @Schema(description = "Quality metrics")
    private Map<String, Object> qualityMetrics;
}