package com.esgbank.greenbond.monitoring.model;

import com.esgbank.greenbond.monitoring.model.enums.MetricType;
import com.esgbank.greenbond.monitoring.model.enums.MetricUnit;
import com.esgbank.greenbond.monitoring.model.enums.DataSourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "impact_metrics")
@CompoundIndex(name = "bond_timestamp_idx", def = "{'bondId': 1, 'timestamp': -1}")
@CompoundIndex(name = "bond_metric_timestamp_idx", def = "{'bondId': 1, 'metricType': 1, 'timestamp': -1}")
public class ImpactMetric {

    @Id
    private String id;

    @Indexed
    private String metricId;

    @Indexed
    private String bondId;

    @Indexed
    private String projectId;

    @Indexed
    private MetricType metricType;

    @Indexed
    private LocalDateTime timestamp;

    private BigDecimal value;

    private MetricUnit unit;

    private DataSourceType sourceType;

    private String sourceId;

    private String deviceId;

    private String location;

    private Map<String, Object> metadata;

    private DataQuality dataQuality;

    private String blockchainTxHash;

    private LocalDateTime blockchainRecordedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    public void generateMetricId() {
        if (metricId == null) {
            metricId = "METRIC-" + System.currentTimeMillis();
        }
    }
}