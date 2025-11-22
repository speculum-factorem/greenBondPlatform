package com.esgbank.greenbond.monitoring.dto;

import com.esgbank.greenbond.monitoring.model.enums.MetricType;
import com.esgbank.greenbond.monitoring.model.enums.MetricUnit;
import com.esgbank.greenbond.monitoring.model.enums.GoalStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Impact goal response")
public class ImpactGoalResponse {

    @Schema(description = "Goal ID")
    private String id;

    @Schema(description = "Unique goal identifier")
    private String goalId;

    @Schema(description = "Bond ID")
    private String bondId;

    @Schema(description = "Project ID")
    private String projectId;

    @Schema(description = "Goal name")
    private String goalName;

    @Schema(description = "Goal description")
    private String description;

    @Schema(description = "Metric type")
    private MetricType metricType;

    @Schema(description = "Target value")
    private BigDecimal targetValue;

    @Schema(description = "Target unit")
    private MetricUnit targetUnit;

    @Schema(description = "Target date")
    private LocalDateTime targetDate;

    @Schema(description = "Current value")
    private BigDecimal currentValue;

    @Schema(description = "Progress percentage")
    private BigDecimal progressPercentage;

    @Schema(description = "Goal status")
    private GoalStatus status;

    @Schema(description = "Baseline value")
    private BigDecimal baselineValue;

    @Schema(description = "Baseline date")
    private LocalDateTime baselineDate;

    @Schema(description = "Key Performance Indicators")
    private Map<String, Object> kpis;

    @Schema(description = "Verification method")
    private String verificationMethod;

    @Schema(description = "Reporting frequency")
    private String reportingFrequency;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}