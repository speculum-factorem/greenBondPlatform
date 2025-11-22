package com.esgbank.greenbond.monitoring.model;

import com.esgbank.greenbond.monitoring.model.enums.MetricType;
import com.esgbank.greenbond.monitoring.model.enums.MetricUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricSummary {

    private MetricType metricType;
    private BigDecimal totalValue;
    private MetricUnit unit;
    private BigDecimal averageValue;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private Integer dataPoints;
    private BigDecimal periodOverPeriodChange;
    private BigDecimal vsTarget;
}