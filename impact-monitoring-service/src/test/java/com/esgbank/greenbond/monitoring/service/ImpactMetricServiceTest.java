package com.esgbank.greenbond.monitoring.service;

import com.esgbank.greenbond.monitoring.dto.ImpactMetricRequest;
import com.esgbank.greenbond.monitoring.exception.MetricNotFoundException;
import com.esgbank.greenbond.monitoring.integration.BlockchainService;
import com.esgbank.greenbond.monitoring.mapper.ImpactMetricMapper;
import com.esgbank.greenbond.monitoring.mapper.ImpactMetricMapperImpl;
import com.esgbank.greenbond.monitoring.model.ImpactMetric;
import com.esgbank.greenbond.monitoring.model.enums.DataSourceType;
import com.esgbank.greenbond.monitoring.model.enums.MetricType;
import com.esgbank.greenbond.monitoring.model.enums.MetricUnit;
import com.esgbank.greenbond.monitoring.repository.ImpactMetricRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImpactMetricServiceTest {

    @Mock
    private ImpactMetricRepository metricRepository;

    @Mock
    private TimeSeriesService timeSeriesService;

    @Mock
    private DataQualityService dataQualityService;

    @Mock
    private BlockchainService blockchainService;

    private ImpactMetricMapper metricMapper = new ImpactMetricMapperImpl();

    private ImpactMetricService impactMetricService;

    @BeforeEach
    void setUp() {
        impactMetricService = new ImpactMetricService(metricRepository, metricMapper,
                timeSeriesService, dataQualityService, blockchainService);
    }

    @Test
    void shouldCreateMetricSuccessfully() {
        // Given
        ImpactMetricRequest request = createMetricRequest();
        ImpactMetric metric = createMetric();

        when(metricRepository.save(any(ImpactMetric.class))).thenReturn(metric);
        when(dataQualityService.assessDataQuality(any())).thenReturn(metric.getDataQuality());
        doNothing().when(timeSeriesService).storeMetricInTimeSeries(any());
        doNothing().when(blockchainService).recordImpactMetric(any());

        // When
        var result = impactMetricService.createMetric(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMetricId()).isEqualTo("METRIC-123");
        assertThat(result.getBondId()).isEqualTo("BOND-123");

        verify(metricRepository).save(any(ImpactMetric.class));
        verify(timeSeriesService).storeMetricInTimeSeries(any());
        verify(blockchainService).recordImpactMetric(any());
    }

    @Test
    void shouldGetMetricSuccessfully() {
        // Given
        String metricId = "METRIC-123";
        ImpactMetric metric = createMetric();

        when(metricRepository.findByMetricId(metricId)).thenReturn(Optional.of(metric));

        // When
        var result = impactMetricService.getMetric(metricId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMetricId()).isEqualTo(metricId);
        assertThat(result.getValue()).isEqualTo(BigDecimal.valueOf(150.5));
    }

    @Test
    void shouldThrowExceptionWhenMetricNotFound() {
        // Given
        String metricId = "non-existent-metric";
        when(metricRepository.findByMetricId(metricId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> impactMetricService.getMetric(metricId))
                .isInstanceOf(MetricNotFoundException.class)
                .hasMessageContaining("Impact metric not found");
    }

    @Test
    void shouldGetMetricsByBond() {
        // Given
        String bondId = "BOND-123";
        Pageable pageable = PageRequest.of(0, 10);
        ImpactMetric metric = createMetric();
        Page<ImpactMetric> metricPage = new PageImpl<>(List.of(metric), pageable, 1);

        when(metricRepository.findByBondId(bondId, pageable)).thenReturn(metricPage);

        // When
        Page<ImpactMetric> result = metricRepository.findByBondId(bondId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBondId()).isEqualTo(bondId);
    }

    private ImpactMetricRequest createMetricRequest() {
        return ImpactMetricRequest.builder()
                .bondId("BOND-123")
                .projectId("PROJ-456")
                .metricType(MetricType.CARBON_EMISSIONS_REDUCTION)
                .value(BigDecimal.valueOf(150.5))
                .unit(MetricUnit.TONS_CO2)
                .timestamp(LocalDateTime.now().minusHours(1))
                .sourceType(DataSourceType.IOT_SENSOR)
                .sourceId("sensor-001")
                .deviceId("device-123")
                .location("Solar Farm A")
                .build();
    }

    private ImpactMetric createMetric() {
        return ImpactMetric.builder()
                .id("metric-uuid")
                .metricId("METRIC-123")
                .bondId("BOND-123")
                .projectId("PROJ-456")
                .metricType(MetricType.CARBON_EMISSIONS_REDUCTION)
                .value(BigDecimal.valueOf(150.5))
                .unit(MetricUnit.TONS_CO2)
                .timestamp(LocalDateTime.now().minusHours(1))
                .sourceType(DataSourceType.IOT_SENSOR)
                .sourceId("sensor-001")
                .deviceId("device-123")
                .location("Solar Farm A")
                .dataQuality(com.esgbank.greenbond.monitoring.model.DataQuality.builder()
                        .confidenceScore(0.95)
                        .isVerified(true)
                        .verificationMethod("AUTOMATIC_SENSOR_READING")
                        .dataPoints(1)
                        .standardDeviation(0.5)
                        .qualityStatus("EXCELLENT")
                        .build())
                .build();
    }
}