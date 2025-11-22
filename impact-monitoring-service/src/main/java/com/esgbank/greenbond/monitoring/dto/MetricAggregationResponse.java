package com.esgbank.greenbond.monitoring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Metric aggregation response")
public class MetricAggregationResponse {

    @Schema(description = "Bond ID")
    private String bondId;

    @Schema(description = "Project ID")
    private String projectId;

    @Schema(description = "Metric type")
    private String metricType;

    @Schema(description = "Aggregation interval")
    private String interval;

    @Schema(description = "Aggregation function")
    private String aggregationFunction;

    @Schema(description = "Start time")
    private LocalDateTime startTime;

    @Schema(description = "End time")
    private LocalDateTime endTime;

    @Schema(description = "Total value")
    private BigDecimal totalValue;

    @Schema(description = "Average value")
    private BigDecimal averageValue;

    @Schema(description = "Minimum value")
    private BigDecimal minValue;

    @Schema(description = "Maximum value")
    private BigDecimal maxValue;

    @Schema(description = "Data point count")
    private Integer dataPointCount;

    @Schema(description = "Time series data")
    private List<TimeSeriesPoint> timeSeries;

    @Schema(description = "Additional statistics")
    private Map<String, Object> statistics;
}