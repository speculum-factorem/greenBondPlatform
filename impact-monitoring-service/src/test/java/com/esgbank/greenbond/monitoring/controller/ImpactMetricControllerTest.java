package com.esgbank.greenbond.monitoring.controller;

import com.esgbank.greenbond.monitoring.dto.ImpactMetricRequest;
import com.esgbank.greenbond.monitoring.dto.ImpactMetricResponse;
import com.esgbank.greenbond.monitoring.model.enums.DataSourceType;
import com.esgbank.greenbond.monitoring.model.enums.MetricType;
import com.esgbank.greenbond.monitoring.model.enums.MetricUnit;
import com.esgbank.greenbond.monitoring.service.ImpactMetricService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImpactMetricController.class)
class ImpactMetricControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ImpactMetricService impactMetricService;

    @Test
    void shouldCreateMetricSuccessfully() throws Exception {
        // Given
        ImpactMetricRequest request = createMetricRequest();
        ImpactMetricResponse response = createMetricResponse();

        when(impactMetricService.createMetric(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/impact/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metricId").value("METRIC-123"))
                .andExpect(jsonPath("$.bondId").value("BOND-123"))
                .andExpect(jsonPath("$.metricType").value("CARBON_EMISSIONS_REDUCTION"))
                .andExpect(jsonPath("$.value").value(150.5));
    }

    @Test
    void shouldGetMetricSuccessfully() throws Exception {
        // Given
        ImpactMetricResponse response = createMetricResponse();
        when(impactMetricService.getMetric("METRIC-123")).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/impact/metrics/METRIC-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metricId").value("METRIC-123"))
                .andExpect(jsonPath("$.bondId").value("BOND-123"));
    }

    @Test
    void shouldGetMetricsByBond() throws Exception {
        // Given
        ImpactMetricResponse response = createMetricResponse();
        Page<ImpactMetricResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(impactMetricService.getMetricsByBond(eq("BOND-123"), any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/impact/metrics/bond/BOND-123")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].metricId").value("METRIC-123"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldDeleteMetric() throws Exception {
        // Given & When & Then
        mockMvc.perform(delete("/api/v1/impact/metrics/METRIC-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Impact metric deleted successfully"))
                .andExpect(jsonPath("$.metricId").value("METRIC-123"));
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

    private ImpactMetricResponse createMetricResponse() {
        return ImpactMetricResponse.builder()
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
                .build();
    }
}