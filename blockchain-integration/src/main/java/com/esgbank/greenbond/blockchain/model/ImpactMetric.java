package com.esgbank.greenbond.blockchain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Environmental impact metric")
public class ImpactMetric {

    @Schema(description = "Bond ID")
    private String bondId;

    @Schema(description = "Metric type (CO2_REDUCTION, ENERGY_PRODUCTION, etc.)")
    private String metricType;

    @Schema(description = "Metric value")
    private Double value;

    @Schema(description = "Unit of measurement")
    private String unit;

    @Schema(description = "Timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "Data source")
    private String source;

    @Schema(description = "Data hash for verification")
    private String dataHash;

    @Schema(description = "Blockchain transaction hash")
    private String transactionHash;
}