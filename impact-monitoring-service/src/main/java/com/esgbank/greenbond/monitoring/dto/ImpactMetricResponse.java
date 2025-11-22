package com.esgbank.greenbond.monitoring.dto;

import com.esgbank.greenbond.monitoring.model.enums.MetricType;
import com.esgbank.greenbond.monitoring.model.enums.MetricUnit;
import com.esgbank.greenbond.monitoring.model.enums.DataSourceType;
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
@Schema(description = "Impact metric response")
public class ImpactMetricResponse {

    @Schema(description = "Metric ID")
    private String id;

    @Schema(description = "Unique metric identifier")
    private String metricId;

    @Schema(description = "Bond ID")
    private String bondId;

    @Schema(description = "Project ID")
    private String projectId;

    @Schema(description = "Metric type")
    private MetricType metricType;

    @Schema(description = "Metric value")
    private BigDecimal value;

    @Schema(description = "Metric unit")
    private MetricUnit unit;

    @Schema(description = "Timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "Data source type")
    private DataSourceType sourceType;

    @Schema(description = "Source ID")
    private String sourceId;

    @Schema(description = "Device ID")
    private String deviceId;

    @Schema(description = "Location")
    private String location;

    @Schema(description = "Metadata")
    private Map<String, Object> metadata;

    @Schema(description = "Data quality information")
    private DataQualityResponse dataQuality;

    @Schema(description = "Blockchain transaction hash")
    private String blockchainTxHash;

    @Schema(description = "Blockchain recorded at")
    private LocalDateTime blockchainRecordedAt;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}