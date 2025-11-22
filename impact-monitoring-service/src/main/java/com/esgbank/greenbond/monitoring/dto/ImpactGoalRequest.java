package com.esgbank.greenbond.monitoring.dto;

import com.esgbank.greenbond.monitoring.model.enums.MetricType;
import com.esgbank.greenbond.monitoring.model.enums.MetricUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Schema(description = "Request for creating an impact goal")
public class ImpactGoalRequest {

    @NotBlank
    @Schema(description = "Bond ID", example = "BOND-123")
    private String bondId;

    @NotBlank
    @Schema(description = "Project ID", example = "PROJ-456")
    private String projectId;

    @NotBlank
    @Schema(description = "Goal name", example = "Carbon Reduction Target 2024")
    private String goalName;

    @Schema(description = "Goal description")
    private String description;

    @NotNull
    @Schema(description = "Metric type")
    private MetricType metricType;

    @NotNull
    @Schema(description = "Target value", example = "1000.0")
    private BigDecimal targetValue;

    @NotNull
    @Schema(description = "Target unit")
    private MetricUnit targetUnit;

    @NotNull
    @Future
    @Schema(description = "Target date")
    private LocalDateTime targetDate;

    @Schema(description = "Baseline value", example = "1500.0")
    private BigDecimal baselineValue;

    @Schema(description = "Baseline date")
    private LocalDateTime baselineDate;

    @Schema(description = "Verification method")
    private String verificationMethod;

    @Schema(description = "Reporting frequency")
    private String reportingFrequency;

    @Schema(description = "Key Performance Indicators")
    private Map<String, Object> kpis;
}