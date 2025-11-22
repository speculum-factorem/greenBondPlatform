package com.esgbank.greenbond.monitoring.model;

import com.esgbank.greenbond.monitoring.model.enums.MetricType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalProgress {

    private String goalId;
    private String goalName;
    private MetricType metricType;
    private BigDecimal targetValue;
    private BigDecimal currentValue;
    private BigDecimal progressPercentage;
    private BigDecimal remainingValue;
    private String status;
}