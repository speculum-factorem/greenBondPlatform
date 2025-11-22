package com.esgbank.greenbond.monitoring.service;

import com.esgbank.greenbond.monitoring.dto.ImpactMetricRequest;
import com.esgbank.greenbond.monitoring.model.DataQuality;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataQualityService {

    private final Random random = new Random();

    public DataQuality assessDataQuality(ImpactMetricRequest request) {
        log.debug("Assessing data quality for metric: {}", request.getMetricType());

        double confidenceScore = calculateConfidenceScore(request);
        boolean isVerified = confidenceScore > 0.8; // Threshold for automatic verification

        return DataQuality.builder()
                .confidenceScore(confidenceScore)
                .isVerified(isVerified)
                .verificationMethod(determineVerificationMethod(request.getSourceType()))
                .dataPoints(1)
                .standardDeviation(calculateStandardDeviation(request))
                .qualityStatus(determineQualityStatus(confidenceScore))
                .qualityMetrics(calculateQualityMetrics(request, confidenceScore))
                .build();
    }

    private double calculateConfidenceScore(ImpactMetricRequest request) {
        double score = 0.0;

        // Source type confidence
        switch (request.getSourceType()) {
            case IOT_SENSOR:
                score += 0.3;
                break;
            case SMART_METER:
                score += 0.4;
                break;
            case API_INTEGRATION:
                score += 0.3;
                break;
            case MANUAL_ENTRY:
                score += 0.1;
                break;
            default:
                score += 0.2;
        }

        // Data completeness
        if (request.getDeviceId() != null && !request.getDeviceId().isEmpty()) {
            score += 0.2;
        }

        if (request.getLocation() != null && !request.getLocation().isEmpty()) {
            score += 0.2;
        }

        if (request.getMetadata() != null && !request.getMetadata().isEmpty()) {
            score += 0.1;
        }

        // Timestamp validity (not in future)
        if (request.getTimestamp().isBefore(java.time.LocalDateTime.now())) {
            score += 0.2;
        }

        // Add some randomness for simulation
        score += (random.nextDouble() * 0.1);

        return Math.min(score, 1.0);
    }

    private String determineVerificationMethod(com.esgbank.greenbond.monitoring.model.enums.DataSourceType sourceType) {
        switch (sourceType) {
            case IOT_SENSOR:
                return "AUTOMATIC_SENSOR_READING";
            case SMART_METER:
                return "CALIBRATED_METER";
            case API_INTEGRATION:
                return "EXTERNAL_API";
            case MANUAL_ENTRY:
                return "MANUAL_VERIFICATION_REQUIRED";
            default:
                return "UNVERIFIED";
        }
    }

    private Double calculateStandardDeviation(ImpactMetricRequest request) {
        // In a real implementation, this would calculate based on historical data
        // For now, return a simulated value
        return random.nextDouble() * request.getValue().doubleValue() * 0.1;
    }

    private String determineQualityStatus(double confidenceScore) {
        if (confidenceScore >= 0.9) return "EXCELLENT";
        if (confidenceScore >= 0.8) return "GOOD";
        if (confidenceScore >= 0.6) return "FAIR";
        if (confidenceScore >= 0.4) return "POOR";
        return "UNACCEPTABLE";
    }

    private Map<String, Object> calculateQualityMetrics(ImpactMetricRequest request, double confidenceScore) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("completeness", calculateCompleteness(request));
        metrics.put("timeliness", calculateTimeliness(request));
        metrics.put("accuracy", confidenceScore);
        metrics.put("consistency", random.nextDouble() * 0.2 + 0.8); // Simulated
        return metrics;
    }

    private double calculateCompleteness(ImpactMetricRequest request) {
        int totalFields = 5; // bondId, metricType, value, unit, timestamp
        int filledFields = 0;

        if (request.getBondId() != null && !request.getBondId().isEmpty()) filledFields++;
        if (request.getMetricType() != null) filledFields++;
        if (request.getValue() != null) filledFields++;
        if (request.getUnit() != null) filledFields++;
        if (request.getTimestamp() != null) filledFields++;

        return (double) filledFields / totalFields;
    }

    private double calculateTimeliness(ImpactMetricRequest request) {
        // Calculate how recent the data is
        long hoursDifference = java.time.Duration.between(
                request.getTimestamp(), java.time.LocalDateTime.now()).toHours();

        if (hoursDifference <= 1) return 1.0; // Within 1 hour
        if (hoursDifference <= 24) return 0.8; // Within 1 day
        if (hoursDifference <= 168) return 0.5; // Within 1 week
        return 0.2; // Older than 1 week
    }
}