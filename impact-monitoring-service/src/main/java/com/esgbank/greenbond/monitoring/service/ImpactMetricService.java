package com.esgbank.greenbond.monitoring.service;

import com.esgbank.greenbond.monitoring.dto.ImpactMetricRequest;
import com.esgbank.greenbond.monitoring.dto.ImpactMetricResponse;
import com.esgbank.greenbond.monitoring.dto.MetricAggregationRequest;
import com.esgbank.greenbond.monitoring.dto.MetricAggregationResponse;
import com.esgbank.greenbond.monitoring.exception.ImpactMonitoringException;
import com.esgbank.greenbond.monitoring.exception.MetricNotFoundException;
import com.esgbank.greenbond.monitoring.integration.BlockchainService;
import com.esgbank.greenbond.monitoring.mapper.ImpactMetricMapper;
import com.esgbank.greenbond.monitoring.model.ImpactMetric;
import com.esgbank.greenbond.monitoring.model.enums.MetricType;
import com.esgbank.greenbond.monitoring.repository.ImpactMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Сервис для управления ESG-метриками воздействия.
 * Обеспечивает сбор, валидацию, оценку качества данных и хранение метрик.
 * Интегрируется с InfluxDB для временных рядов и блокчейном для неизменяемости данных.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImpactMetricService {

    private final ImpactMetricRepository metricRepository;
    private final ImpactMetricMapper metricMapper;
    private final TimeSeriesService timeSeriesService;
    private final DataQualityService dataQualityService;
    private final BlockchainService blockchainService;

    /**
     * Создает новую ESG-метрику воздействия.
     * Валидирует данные, оценивает качество, сохраняет в MongoDB и InfluxDB, записывает в блокчейн.
     *
     * @param request запрос на создание метрики
     * @return ImpactMetricResponse информация о созданной метрике
     */
    @Transactional
    public ImpactMetricResponse createMetric(ImpactMetricRequest request) {
        // Получаем requestId из MDC для трейсинга запроса
        String requestId = MDC.get("requestId");
        log.info("Creating impact metric for bond: {}, metric: {}, requestId: {}",
                request.getBondId(), request.getMetricType(), requestId);

        try {
            // Валидируем данные метрики (формат, диапазон значений)
            validateMetricRequest(request);

            // Оцениваем качество данных (полнота, точность, актуальность)
            var dataQuality = dataQualityService.assessDataQuality(request);

            // Создаем сущность метрики из запроса
            ImpactMetric metric = metricMapper.toEntity(request);
            metric.setDataQuality(dataQuality);

            // Сохраняем метрику в MongoDB
            ImpactMetric savedMetric = metricRepository.save(metric);

            // Сохраняем метрику в InfluxDB для временных рядов и аналитики
            timeSeriesService.storeMetricInTimeSeries(savedMetric);

            // Записываем метрику в блокчейн для неизменяемости и прозрачности
            blockchainService.recordImpactMetric(savedMetric);

            log.info("Impact metric created successfully: {}, bond: {}",
                    savedMetric.getMetricId(), request.getBondId());

            return metricMapper.toResponse(savedMetric);

        } catch (Exception e) {
            log.error("Failed to create impact metric for bond: {}. Error: {}",
                    request.getBondId(), e.getMessage(), e);
            throw new ImpactMonitoringException("Impact metric creation failed: " + e.getMessage(), e);
        }
    }

    // Получение метрики по ID
    public ImpactMetricResponse getMetric(String metricId) {
        log.debug("Fetching impact metric: {}", metricId);

        // Находим метрику в MongoDB
        ImpactMetric metric = metricRepository.findByMetricId(metricId)
                .orElseThrow(() -> new MetricNotFoundException("Impact metric not found: " + metricId));

        return metricMapper.toResponse(metric);
    }

    // Получение метрик по облигации с пагинацией
    public Page<ImpactMetricResponse> getMetricsByBond(String bondId, Pageable pageable) {
        log.debug("Fetching metrics for bond: {}, page: {}", bondId, pageable.getPageNumber());

        // Получаем метрики из MongoDB с пагинацией
        Page<ImpactMetric> metrics = metricRepository.findByBondId(bondId, pageable);
        return metrics.map(metricMapper::toResponse);
    }

    public Page<ImpactMetricResponse> getMetricsByBondAndType(String bondId, MetricType metricType, Pageable pageable) {
        log.debug("Fetching metrics for bond: {}, type: {}, page: {}", bondId, metricType, pageable.getPageNumber());

        Page<ImpactMetric> metrics = metricRepository.findByBondIdAndMetricType(bondId, metricType, pageable);
        return metrics.map(metricMapper::toResponse);
    }

    public List<ImpactMetricResponse> getMetricsByBondTypeAndTimeRange(
            String bondId, MetricType metricType, LocalDateTime start, LocalDateTime end) {

        log.debug("Fetching metrics for bond: {}, type: {}, range: {} to {}",
                bondId, metricType, start, end);

        List<ImpactMetric> metrics = metricRepository.findMetricsByBondTypeAndTimeRange(bondId, metricType, start, end);
        return metrics.stream().map(metricMapper::toResponse).toList();
    }

    public MetricAggregationResponse getMetricAggregation(MetricAggregationRequest request) {
        log.debug("Calculating metric aggregation for bond: {}, type: {}",
                request.getBondId(), request.getMetricType());

        try {
            return timeSeriesService.calculateAggregation(request);
        } catch (Exception e) {
            log.error("Metric aggregation failed for bond: {}. Error: {}",
                    request.getBondId(), e.getMessage(), e);
            throw new ImpactMonitoringException("Metric aggregation failed: " + e.getMessage(), e);
        }
    }

    public Map<MetricType, BigDecimal> getBondMetricsSummary(String bondId) {
        log.debug("Calculating metrics summary for bond: {}", bondId);

        try {
            return timeSeriesService.getBondMetricsSummary(bondId);
        } catch (Exception e) {
            log.error("Metrics summary calculation failed for bond: {}. Error: {}",
                    bondId, e.getMessage(), e);
            throw new ImpactMonitoringException("Metrics summary calculation failed: " + e.getMessage(), e);
        }
    }

    public List<ImpactMetricResponse> getLatestMetrics(String bondId, MetricType metricType, int limit) {
        log.debug("Fetching latest {} metrics for bond: {}, type: {}", limit, bondId, metricType);

        var pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        List<ImpactMetric> metrics = metricRepository.findLatestMetrics(bondId, metricType, pageable);
        return metrics.stream().map(metricMapper::toResponse).toList();
    }

    @Transactional
    public void deleteMetric(String metricId) {
        log.info("Deleting impact metric: {}", metricId);

        ImpactMetric metric = metricRepository.findByMetricId(metricId)
                .orElseThrow(() -> new MetricNotFoundException("Impact metric not found: " + metricId));

        try {
            // Удаляем из базы данных временных рядов
            timeSeriesService.deleteMetricFromTimeSeries(metric);

            // Удаляем из MongoDB
            metricRepository.delete(metric);

            log.info("Impact metric deleted successfully: {}", metricId);

        } catch (Exception e) {
            log.error("Failed to delete impact metric: {}. Error: {}", metricId, e.getMessage(), e);
            throw new ImpactMonitoringException("Impact metric deletion failed: " + e.getMessage(), e);
        }
    }

    private void validateMetricRequest(ImpactMetricRequest request) {
        if (request.getValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new ImpactMonitoringException("Metric value cannot be negative");
        }

        if (request.getTimestamp().isAfter(LocalDateTime.now().plusHours(1))) {
            throw new ImpactMonitoringException("Metric timestamp cannot be in the future");
        }

        // Проверяем совместимость единицы измерения с типом метрики
        if (!isValidUnitForMetricType(request.getMetricType(), request.getUnit())) {
            throw new ImpactMonitoringException(
                    "Invalid unit " + request.getUnit() + " for metric type " + request.getMetricType());
        }
    }

    private boolean isValidUnitForMetricType(MetricType metricType, com.esgbank.greenbond.monitoring.model.enums.MetricUnit unit) {
        // Бизнес-логика валидации единиц измерения для типов метрик
        // For now, return true for all combinations
        return true;
    }

    public long getMetricCountByBond(String bondId) {
        return metricRepository.countByBondId(bondId);
    }

    public long getMetricCountByBondAndType(String bondId, MetricType metricType) {
        return metricRepository.countByBondIdAndMetricType(bondId, metricType);
    }
}