package com.esgbank.greenbond.monitoring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Time series data point")
public class TimeSeriesPoint {

    @Schema(description = "Timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "Value")
    private BigDecimal value;

    @Schema(description = "Data point count")
    private Integer count;
}