package com.esgbank.greenbond.monitoring.integration;

import com.esgbank.greenbond.monitoring.model.ImpactMetric;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainService {

    public void recordImpactMetric(ImpactMetric metric) {
        log.info("Recording impact metric on blockchain: {}", metric.getMetricId());

        try {
            // In a real implementation, this would call the blockchain integration service
            // to record the impact metric hash on the blockchain

            // Mock implementation
            String transactionHash = generateTransactionHash(metric);
            metric.setBlockchainTxHash(transactionHash);
            metric.setBlockchainRecordedAt(java.time.LocalDateTime.now());

            log.info("Impact metric recorded on blockchain: {}, txHash: {}",
                    metric.getMetricId(), transactionHash);

        } catch (Exception e) {
            log.error("Failed to record impact metric on blockchain: {}. Error: {}",
                    metric.getMetricId(), e.getMessage(), e);
            // Don't throw exception to avoid blocking the metric creation process
        }
    }

    private String generateTransactionHash(ImpactMetric metric) {
        String data = metric.getMetricId() +
                metric.getBondId() +
                metric.getMetricType() +
                metric.getValue() +
                metric.getTimestamp().toString();

        return "IMPACT-" + Integer.toHexString(data.hashCode());
    }
}