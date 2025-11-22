package com.esgbank.greenbond.monitoring.dto;

import com.esgbank.greenbond.monitoring.model.enums.MetricType;
import com.esgbank.greenbond.monitoring.model.enums.MetricUnit;
import com.esgbank.greenbond.monitoring.model.enums.DataSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Schema(description = "Request for creating an impact metric")
public class ImpactMetricRequest {

    @NotBlank
    @Schema(description = "Bond ID", example = "BOND-123")
    private String bondId;

    @NotBlank
    @Schema(description = "Project ID", example = "PROJ-456")
    private String projectId;

    @NotNull
    @Schema(description = "Metric type")
    private MetricType metricType;

    @NotNull
    @Schema(description = "Metric value", example = "150.5")
    private BigDecimal value;

    @NotNull
    @Schema(description = "Metric unit")
    private MetricUnit unit;

    @NotNull
    @Schema(description = "Timestamp of the measurement")
    private LocalDateTime timestamp;

    @NotNull
    @Schema(description = "Data source type")
    private DataSourceType sourceType;

    @Schema(description = "Source ID", example = "sensor-789")
    private String sourceId;

    @Schema(description = "Device ID", example = "device-123")
    private String deviceId;

    @Schema(description = "Location", example = "Solar Farm A")
    private String location;

    @Schema(description = "Additional metadata")
    private Map<String, Object> metadata;
}