package com.esgbank.greenbond.monitoring.controller;

import com.esgbank.greenbond.monitoring.dto.ImpactMetricRequest;
import com.esgbank.greenbond.monitoring.integration.IoTIntegrationService;
import com.esgbank.greenbond.monitoring.service.ImpactMetricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/impact/iot")
@RequiredArgsConstructor
@Tag(name = "IoT Integration", description = "APIs for IoT device integration and data collection")
public class IoTController {

    private final IoTIntegrationService ioTIntegrationService;
    private final ImpactMetricService impactMetricService;

    @PostMapping("/devices/{deviceId}/sync")
    @Operation(summary = "Sync IoT device data", description = "Sync data from an IoT device and create metrics")
    public ResponseEntity<Map<String, Object>> syncDeviceData(
            @Parameter(description = "Device ID") @PathVariable String deviceId,
            @Parameter(description = "Bond ID") @RequestParam String bondId) {

        log.info("REST API: Syncing IoT device data for device: {}, bond: {}", deviceId, bondId);

        if (!ioTIntegrationService.validateDevice(deviceId)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "INVALID_DEVICE",
                    "message", "Invalid device ID: " + deviceId
            ));
        }

        List<ImpactMetricRequest> metrics = ioTIntegrationService.fetchIoTData(deviceId, bondId);
        int createdCount = 0;

        for (ImpactMetricRequest metric : metrics) {
            try {
                impactMetricService.createMetric(metric);
                createdCount++;
            } catch (Exception e) {
                log.error("Failed to create metric from IoT data: {}", e.getMessage());
            }
        }

        return ResponseEntity.ok(Map.of(
                "deviceId", deviceId,
                "bondId", bondId,
                "metricsReceived", metrics.size(),
                "metricsCreated", createdCount,
                "syncTimestamp", java.time.LocalDateTime.now()
        ));
    }

    @GetMapping("/devices/{deviceId}/status")
    @Operation(summary = "Get device status", description = "Get status information for an IoT device")
    public ResponseEntity<Map<String, Object>> getDeviceStatus(
            @Parameter(description = "Device ID") @PathVariable String deviceId) {

        log.debug("REST API: Getting device status for: {}", deviceId);

        if (!ioTIntegrationService.validateDevice(deviceId)) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> status = ioTIntegrationService.getDeviceStatus(deviceId);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/devices/{deviceId}/sync-async")
    @Operation(summary = "Sync IoT device data async", description = "Sync data from an IoT device asynchronously")
    public ResponseEntity<Map<String, String>> syncDeviceDataAsync(
            @Parameter(description = "Device ID") @PathVariable String deviceId,
            @Parameter(description = "Bond ID") @RequestParam String bondId) {

        log.info("REST API: Starting async IoT data sync for device: {}, bond: {}", deviceId, bondId);

        if (!ioTIntegrationService.validateDevice(deviceId)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "INVALID_DEVICE",
                    "message", "Invalid device ID: " + deviceId
            ));
        }

        // Start async processing
        ioTIntegrationService.fetchIoTDataAsync(deviceId, bondId)
                .subscribe(metrics -> {
                    log.info("Async IoT data processing completed for device: {}, metrics: {}",
                            deviceId, metrics.size());
                }, error -> {
                    log.error("Async IoT data processing failed for device: {}. Error: {}",
                            deviceId, error.getMessage());
                });

        return ResponseEntity.ok(Map.of(
                "message", "Async IoT data sync started",
                "deviceId", deviceId,
                "bondId", bondId,
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}