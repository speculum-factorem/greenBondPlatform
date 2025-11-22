package com.esgbank.greenbond.monitoring.model;

import com.esgbank.greenbond.monitoring.model.enums.ReportStatus;
import com.esgbank.greenbond.monitoring.model.enums.ReportType;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "impact_reports")
public class ImpactReport {

    @Id
    private String id;

    @Indexed
    private String reportId;

    @Indexed
    private String bondId;

    @Indexed
    private String projectId;

    private String reportName;

    private ReportType reportType;

    private ReportStatus status;

    private LocalDateTime reportingPeriodStart;

    private LocalDateTime reportingPeriodEnd;

    private LocalDateTime generatedAt;

    private String generatedBy;

    private Map<String, Object> executiveSummary;

    private List<MetricSummary> metricSummaries;

    private List<GoalProgress> goalProgress;

    private Map<String, Object> keyFindings;

    private Map<String, Object> recommendations;

    private String blockchainTxHash;

    private LocalDateTime blockchainRecordedAt;

    private Map<String, Object> metadata;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    public void generateReportId() {
        if (reportId == null) {
            reportId = "REPORT-" + System.currentTimeMillis();
        }
    }
}