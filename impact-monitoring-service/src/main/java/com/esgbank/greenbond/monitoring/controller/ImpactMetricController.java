package com.esgbank.greenbond.monitoring.controller;

import com.esgbank.greenbond.monitoring.dto.ImpactMetricRequest;
import com.esgbank.greenbond.monitoring.dto.ImpactMetricResponse;
import com.esgbank.greenbond.monitoring.dto.MetricAggregationRequest;
import com.esgbank.greenbond.monitoring.dto.MetricAggregationResponse;
import com.esgbank.greenbond.monitoring.model.enums.MetricType;
import com.esgbank.greenbond.monitoring.service.ImpactMetricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/impact/metrics")
@RequiredArgsConstructor
@Tag(name = "Impact Metrics", description = "APIs for managing and querying impact metrics")
public class ImpactMetricController {

    private final ImpactMetricService impactMetricService;

    @PostMapping
    @Operation(summary = "Create an impact metric", description = "Create a new impact metric measurement")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Metric created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid metric data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ImpactMetricResponse> createMetric(
            @Parameter(description = "Impact metric request")
            @Valid @RequestBody ImpactMetricRequest request) {

        log.info("REST API: Creating impact metric for bond: {}, type: {}",
                request.getBondId(), request.getMetricType());

        ImpactMetricResponse response = impactMetricService.createMetric(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{metricId}")
    @Operation(summary = "Get impact metric", description = "Get detailed information about an impact metric")
    public ResponseEntity<ImpactMetricResponse> getMetric(
            @Parameter(description = "Metric ID") @PathVariable String metricId) {

        log.debug("REST API: Getting impact metric: {}", metricId);

        ImpactMetricResponse response = impactMetricService.getMetric(metricId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bond/{bondId}")
    @Operation(summary = "Get metrics by bond", description = "Get paginated list of metrics for a bond")
    public ResponseEntity<Page<ImpactMetricResponse>> getMetricsByBond(
            @Parameter(description = "Bond ID") @PathVariable String bondId,
            @PageableDefault(size = 50) Pageable pageable) {

        log.debug("REST API: Getting metrics for bond: {}, page: {}", bondId, pageable.getPageNumber());

        Page<ImpactMetricResponse> metrics = impactMetricService.getMetricsByBond(bondId, pageable);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/bond/{bondId}/type/{metricType}")
    @Operation(summary = "Get metrics by bond and type", description = "Get paginated list of metrics for a bond and metric type")
    public ResponseEntity<Page<ImpactMetricResponse>> getMetricsByBondAndType(
            @Parameter(description = "Bond ID") @PathVariable String bondId,
            @Parameter(description = "Metric type") @PathVariable MetricType metricType,
            @PageableDefault(size = 50) Pageable pageable) {

        log.debug("REST API: Getting metrics for bond: {}, type: {}, page: {}",
                bondId, metricType, pageable.getPageNumber());

        Page<ImpactMetricResponse> metrics = impactMetricService.getMetricsByBondAndType(bondId, metricType, pageable);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/bond/{bondId}/type/{metricType}/range")
    @Operation(summary = "Get metrics by time range", description = "Get metrics for a bond and type within a time range")
    public ResponseEntity<List<ImpactMetricResponse>> getMetricsByTimeRange(
            @Parameter(description = "Bond ID") @PathVariable String bondId,
            @Parameter(description = "Metric type") @PathVariable MetricType metricType,
            @Parameter(description = "Start time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "End time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        log.debug("REST API: Getting metrics for bond: {}, type: {}, range: {} to {}",
                bondId, metricType, start, end);

        List<ImpactMetricResponse> metrics = impactMetricService.getMetricsByBondTypeAndTimeRange(bondId, metricType, start, end);
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/aggregate")
    @Operation(summary = "Aggregate metrics", description = "Calculate aggregated metrics for a time period")
    public ResponseEntity<MetricAggregationResponse> aggregateMetrics(
            @Parameter(description = "Aggregation request") @Valid @RequestBody MetricAggregationRequest request) {

        log.debug("REST API: Calculating aggregation for bond: {}, type: {}",
                request.getBondId(), request.getMetricType());

        MetricAggregationResponse response = impactMetricService.getMetricAggregation(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bond/{bondId}/summary")
    @Operation(summary = "Get bond metrics summary", description = "Get summary of all metrics for a bond")
    public ResponseEntity<Map<MetricType, BigDecimal>> getBondMetricsSummary(
            @Parameter(description = "Bond ID") @PathVariable String bondId) {

        log.debug("REST API: Getting metrics summary for bond: {}", bondId);

        Map<MetricType, BigDecimal> summary = impactMetricService.getBondMetricsSummary(bondId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/bond/{bondId}/type/{metricType}/latest")
    @Operation(summary = "Get latest metrics", description = "Get the latest metrics for a bond and type")
    public ResponseEntity<List<ImpactMetricResponse>> getLatestMetrics(
            @Parameter(description = "Bond ID") @PathVariable String bondId,
            @Parameter(description = "Metric type") @PathVariable MetricType metricType,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "10") int limit) {

        log.debug("REST API: Getting latest {} metrics for bond: {}, type: {}", limit, bondId, metricType);

        List<ImpactMetricResponse> metrics = impactMetricService.getLatestMetrics(bondId, metricType, limit);
        return ResponseEntity.ok(metrics);
    }

    @DeleteMapping("/{metricId}")
    @Operation(summary = "Delete a metric", description = "Delete an impact metric")
    public ResponseEntity<Map<String, String>> deleteMetric(
            @Parameter(description = "Metric ID") @PathVariable String metricId) {

        log.info("REST API: Deleting impact metric: {}", metricId);

        impactMetricService.deleteMetric(metricId);

        return ResponseEntity.ok(Map.of(
                "message", "Impact metric deleted successfully",
                "metricId", metricId,
                "requestId", MDC.get("requestId")
        ));
    }

    @GetMapping("/bond/{bondId}/stats")
    @Operation(summary = "Get bond metrics statistics", description = "Get statistics about metrics for a bond")
    public ResponseEntity<Map<String, Object>> getBondMetricsStats(
            @Parameter(description = "Bond ID") @PathVariable String bondId) {

        log.debug("REST API: Getting metrics statistics for bond: {}", bondId);

        long totalMetrics = impactMetricService.getMetricCountByBond(bondId);

        return ResponseEntity.ok(Map.of(
                "bondId", bondId,
                "totalMetrics", totalMetrics,
                "metricTypes", impactMetricService.getBondMetricsSummary(bondId).keySet().size(),
                "timestamp", LocalDateTime.now()
        ));
    }
}