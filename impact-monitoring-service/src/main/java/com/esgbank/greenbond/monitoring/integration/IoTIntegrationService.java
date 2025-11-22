package com.esgbank.greenbond.monitoring.integration;

import com.esgbank.greenbond.monitoring.dto.ImpactMetricRequest;
import com.esgbank.greenbond.monitoring.model.enums.DataSourceType;
import com.esgbank.greenbond.monitoring.model.enums.MetricType;
import com.esgbank.greenbond.monitoring.model.enums.MetricUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IoTIntegrationService {

    private final WebClient webClient;

    public List<ImpactMetricRequest> fetchIoTData(String deviceId, String bondId) {
        log.info("Fetching IoT data for device: {}, bond: {}", deviceId, bondId);

        try {
            // Mock implementation - in real scenario, this would call IoT platform APIs
            return simulateIoTData(deviceId, bondId);

        } catch (Exception e) {
            log.error("Failed to fetch IoT data for device: {}. Error: {}", deviceId, e.getMessage(), e);
            return List.of();
        }
    }

    public Mono<List<ImpactMetricRequest>> fetchIoTDataAsync(String deviceId, String bondId) {
        return Mono.fromCallable(() -> fetchIoTData(deviceId, bondId))
                .doOnSubscribe(subscription -> log.debug("Starting async IoT data fetch for device: {}", deviceId))
                .doOnSuccess(metrics -> log.debug("Async IoT data fetch completed for device: {}, metrics: {}",
                        deviceId, metrics.size()))
                .doOnError(error -> log.error("Async IoT data fetch failed for device: {}. Error: {}",
                        deviceId, error.getMessage()));
    }

    private List<ImpactMetricRequest> simulateIoTData(String deviceId, String bondId) {
        // Simulate IoT sensor data
        LocalDateTime now = LocalDateTime.now();

        ImpactMetricRequest solarEnergy = ImpactMetricRequest.builder()
                .bondId(bondId)
                .projectId("PROJ-" + bondId)
                .metricType(MetricType.SOLAR_ENERGY_GENERATED)
                .value(BigDecimal.valueOf(1250.75))
                .unit(MetricUnit.KILOWATT_HOURS)
                .timestamp(now.minusMinutes(5))
                .sourceType(DataSourceType.IOT_SENSOR)
                .sourceId("solar-inverter-001")
                .deviceId(deviceId)
                .location("Solar Farm A")
                .metadata(Map.of(
                        "inverter_efficiency", 0.95,
                        "panel_temperature", 45.2,
                        "solar_irradiance", 850.5
                ))
                .build();

        ImpactMetricRequest carbonReduction = ImpactMetricRequest.builder()
                .bondId(bondId)
                .projectId("PROJ-" + bondId)
                .metricType(MetricType.CARBON_EMISSIONS_REDUCTION)
                .value(BigDecimal.valueOf(0.85)) // tons of CO2
                .unit(MetricUnit.TONS_CO2)
                .timestamp(now.minusMinutes(10))
                .sourceType(DataSourceType.IOT_SENSOR)
                .sourceId("carbon-calculator-001")
                .deviceId(deviceId)
                .location("Solar Farm A")
                .metadata(Map.of(
                        "calculation_method", "real-time",
                        "grid_carbon_intensity", 0.45,
                        "calculation_timestamp", now.toString()
                ))
                .build();

        return List.of(solarEnergy, carbonReduction);
    }

    public boolean validateDevice(String deviceId) {
        log.debug("Validating IoT device: {}", deviceId);

        // Mock device validation
        return deviceId != null && deviceId.startsWith("device-");
    }

    public Map<String, Object> getDeviceStatus(String deviceId) {
        log.debug("Getting status for IoT device: {}", deviceId);

        return Map.of(
                "deviceId", deviceId,
                "status", "ONLINE",
                "lastSeen", LocalDateTime.now().minusMinutes(2),
                "batteryLevel", 85,
                "signalStrength", "EXCELLENT",
                "firmwareVersion", "2.1.4"
        );
    }
}