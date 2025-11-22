package com.esgbank.greenbond.monitoring.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataQuality {

    private Double confidenceScore;
    private Boolean isVerified;
    private String verificationMethod;
    private Integer dataPoints;
    private Double standardDeviation;
    private String qualityStatus;
    private Map<String, Object> qualityMetrics;
}