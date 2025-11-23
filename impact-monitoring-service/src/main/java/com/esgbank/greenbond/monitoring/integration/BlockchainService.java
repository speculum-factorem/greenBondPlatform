package com.esgbank.greenbond.monitoring.integration;

import com.esgbank.greenbond.monitoring.model.ImpactMetric;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainService {

    // Запись ESG-метрики в блокчейн для неизменяемости данных
    public void recordImpactMetric(ImpactMetric metric) {
        log.info("Recording impact metric on blockchain: {}", metric.getMetricId());

        try {
            // В реальной реализации здесь должен быть вызов blockchain-integration сервиса
            // для записи хеша метрики в блокчейн

            // Mock реализация: генерируем хеш транзакции
            String transactionHash = generateTransactionHash(metric);
            metric.setBlockchainTxHash(transactionHash);
            metric.setBlockchainRecordedAt(java.time.LocalDateTime.now());

            log.info("Impact metric recorded on blockchain: {}, txHash: {}",
                    metric.getMetricId(), transactionHash);

        } catch (Exception e) {
            log.error("Failed to record impact metric on blockchain: {}. Error: {}",
                    metric.getMetricId(), e.getMessage(), e);
            // Не выбрасываем исключение чтобы не блокировать процесс создания метрики
        }
    }

    // Генерация хеша транзакции из данных метрики
    private String generateTransactionHash(ImpactMetric metric) {
        // Формируем строку из ключевых данных метрики
        String data = metric.getMetricId() +
                metric.getBondId() +
                metric.getMetricType() +
                metric.getValue() +
                metric.getTimestamp().toString();

        // Генерируем хеш (в реальной реализации используется SHA-256)
        return "IMPACT-" + Integer.toHexString(data.hashCode());
    }
}