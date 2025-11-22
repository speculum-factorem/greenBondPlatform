package com.esgbank.greenbond.monitoring.model;

import com.esgbank.greenbond.monitoring.model.enums.MetricType;
import com.esgbank.greenbond.monitoring.model.enums.MetricUnit;
import com.esgbank.greenbond.monitoring.model.enums.GoalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "impact_goals")
public class ImpactGoal {

    @Id
    private String id;

    @Indexed
    private String goalId;

    @Indexed
    private String bondId;

    @Indexed
    private String projectId;

    private String goalName;

    private String description;

    private MetricType metricType;

    private BigDecimal targetValue;

    private MetricUnit targetUnit;

    private LocalDateTime targetDate;

    private BigDecimal currentValue;

    private BigDecimal progressPercentage;

    private GoalStatus status;

    private BigDecimal baselineValue;

    private LocalDateTime baselineDate;

    private Map<String, Object> kpis;

    private String verificationMethod;

    private String reportingFrequency;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    public void generateGoalId() {
        if (goalId == null) {
            goalId = "GOAL-" + System.currentTimeMillis();
        }
    }
}