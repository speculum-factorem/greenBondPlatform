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

@Slf4j
@Service
@RequiredArgsConstructor
public class ImpactMetricService {

    private final ImpactMetricRepository metricRepository;
    private final ImpactMetricMapper metricMapper;
    private final TimeSeriesService timeSeriesService;
    private final DataQualityService dataQualityService;
    private final BlockchainService blockchainService;

    @Transactional
    public ImpactMetricResponse createMetric(ImpactMetricRequest request) {
        String requestId = MDC.get("requestId");
        log.info("Creating impact metric for bond: {}, metric: {}, requestId: {}",
                request.getBondId(), request.getMetricType(), requestId);

        try {
            // Validate metric data
            validateMetricRequest(request);

            // Calculate data quality
            var dataQuality = dataQualityService.assessDataQuality(request);

            // Create metric entity
            ImpactMetric metric = metricMapper.toEntity(request);
            metric.setDataQuality(dataQuality);

            ImpactMetric savedMetric = metricRepository.save(metric);

            // Store in time series database
            timeSeriesService.storeMetricInTimeSeries(savedMetric);

            // Record on blockchain for immutability
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

    public ImpactMetricResponse getMetric(String metricId) {
        log.debug("Fetching impact metric: {}", metricId);

        ImpactMetric metric = metricRepository.findByMetricId(metricId)
                .orElseThrow(() -> new MetricNotFoundException("Impact metric not found: " + metricId));

        return metricMapper.toResponse(metric);
    }

    public Page<ImpactMetricResponse> getMetricsByBond(String bondId, Pageable pageable) {
        log.debug("Fetching metrics for bond: {}, page: {}", bondId, pageable.getPageNumber());

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
            // Remove from time series database
            timeSeriesService.deleteMetricFromTimeSeries(metric);

            // Delete from MongoDB
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

        // Validate unit compatibility with metric type
        if (!isValidUnitForMetricType(request.getMetricType(), request.getUnit())) {
            throw new ImpactMonitoringException(
                    "Invalid unit " + request.getUnit() + " for metric type " + request.getMetricType());
        }
    }

    private boolean isValidUnitForMetricType(MetricType metricType, com.esgbank.greenbond.monitoring.model.enums.MetricUnit unit) {
        // This would contain the business logic for unit validation
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