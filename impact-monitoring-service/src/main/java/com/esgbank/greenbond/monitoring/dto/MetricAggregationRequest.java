package com.esgbank.greenbond.monitoring.dto;

import com.esgbank.greenbond.monitoring.model.enums.MetricType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Request for metric aggregation")
public class MetricAggregationRequest {

    @NotBlank
    @Schema(description = "Bond ID", example = "BOND-123")
    private String bondId;

    @Schema(description = "Project ID", example = "PROJ-456")
    private String projectId;

    @NotNull
    @Schema(description = "Metric type")
    private MetricType metricType;

    @NotNull
    @Schema(description = "Start time for aggregation")
    private LocalDateTime startTime;

    @NotNull
    @Schema(description = "End time for aggregation")
    private LocalDateTime endTime;

    @Schema(description = "Aggregation interval", example = "1h, 1d, 1w")
    private String interval;

    @Schema(description = "Aggregation function", example = "sum, avg, min, max")
    private String aggregationFunction;
}