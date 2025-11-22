package com.esgbank.greenbond.monitoring.service;

import com.esgbank.greenbond.monitoring.dto.MetricAggregationRequest;
import com.esgbank.greenbond.monitoring.dto.MetricAggregationResponse;
import com.esgbank.greenbond.monitoring.dto.TimeSeriesPoint;
import com.esgbank.greenbond.monitoring.model.ImpactMetric;
import com.esgbank.greenbond.monitoring.model.enums.MetricType;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeSeriesService {

    private final InfluxDBClient influxDBClient;
    private final com.esgbank.greenbond.monitoring.config.InfluxDBProperties influxDBProperties;

    public void storeMetricInTimeSeries(ImpactMetric metric) {
        log.debug("Storing metric in time series: {}", metric.getMetricId());

        try (WriteApi writeApi = influxDBClient.getWriteApi()) {
            Point point = Point.measurement("impact_metrics")
                    .addTag("bond_id", metric.getBondId())
                    .addTag("project_id", metric.getProjectId())
                    .addTag("metric_type", metric.getMetricType().name())
                    .addTag("source_type", metric.getSourceType().name())
                    .addTag("device_id", metric.getDeviceId())
                    .addTag("location", metric.getLocation())
                    .addField("value", metric.getValue().doubleValue())
                    .addField("confidence_score", metric.getDataQuality().getConfidenceScore())
                    .time(metric.getTimestamp().toInstant(ZoneOffset.UTC), WritePrecision.MS);

            writeApi.writePoint(influxDBProperties.getBucket(), influxDBProperties.getOrg(), point);

            log.debug("Metric stored in time series successfully: {}", metric.getMetricId());

        } catch (Exception e) {
            log.error("Failed to store metric in time series: {}. Error: {}",
                    metric.getMetricId(), e.getMessage(), e);
            throw new RuntimeException("Time series storage failed", e);
        }
    }

    public MetricAggregationResponse calculateAggregation(MetricAggregationRequest request) {
        log.debug("Calculating aggregation for bond: {}, metric: {}",
                request.getBondId(), request.getMetricType());

        String fluxQuery = buildAggregationQuery(request);

        try {
            List<FluxTable> tables = influxDBClient.getQueryApi().query(fluxQuery, influxDBProperties.getOrg());

            return parseAggregationResults(tables, request);

        } catch (Exception e) {
            log.error("Aggregation query failed for bond: {}. Error: {}",
                    request.getBondId(), e.getMessage(), e);
            throw new RuntimeException("Aggregation calculation failed", e);
        }
    }

    public Map<MetricType, BigDecimal> getBondMetricsSummary(String bondId) {
        log.debug("Getting metrics summary for bond: {}", bondId);

        Map<MetricType, BigDecimal> summary = new HashMap<>();

        // Query for each metric type
        String fluxQuery = String.format(
                "from(bucket:\"%s\") " +
                        "|> range(start: -1y) " +
                        "|> filter(fn: (r) => r._measurement == \"impact_metrics\") " +
                        "|> filter(fn: (r) => r.bond_id == \"%s\") " +
                        "|> group(columns: [\"metric_type\"]) " +
                        "|> sum()",
                influxDBProperties.getBucket(), bondId);

        try {
            List<FluxTable> tables = influxDBClient.getQueryApi().query(fluxQuery, influxDBProperties.getOrg());

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    String metricTypeStr = record.getValueByKey("metric_type").toString();
                    Double sumValue = (Double) record.getValue();

                    try {
                        MetricType metricType = MetricType.valueOf(metricTypeStr);
                        summary.put(metricType, BigDecimal.valueOf(sumValue));
                    } catch (IllegalArgumentException e) {
                        log.warn("Unknown metric type in time series: {}", metricTypeStr);
                    }
                }
            }

            return summary;

        } catch (Exception e) {
            log.error("Metrics summary query failed for bond: {}. Error: {}", bondId, e.getMessage(), e);
            throw new RuntimeException("Metrics summary calculation failed", e);
        }
    }

    public void deleteMetricFromTimeSeries(ImpactMetric metric) {
        log.debug("Deleting metric from time series: {}", metric.getMetricId());

        // InfluxDB doesn't support direct deletion by tags in community edition
        // This would require enterprise features or a different approach
        log.warn("Time series deletion not implemented for metric: {}", metric.getMetricId());
    }

    private String buildAggregationQuery(MetricAggregationRequest request) {
        String rangeStart = request.getStartTime().toInstant(ZoneOffset.UTC).toString();
        String rangeStop = request.getEndTime().toInstant(ZoneOffset.UTC).toString();

        String interval = request.getInterval() != null ? request.getInterval() : "1h";
        String aggregation = request.getAggregationFunction() != null ?
                request.getAggregationFunction() : "mean";

        return String.format(
                "from(bucket:\"%s\") " +
                        "|> range(start: %s, stop: %s) " +
                        "|> filter(fn: (r) => r._measurement == \"impact_metrics\") " +
                        "|> filter(fn: (r) => r.bond_id == \"%s\") " +
                        "|> filter(fn: (r) => r.metric_type == \"%s\") " +
                        "|> aggregateWindow(every: %s, fn: %s, createEmpty: false) " +
                        "|> yield(name: \"aggregated\")",
                influxDBProperties.getBucket(), rangeStart, rangeStop,
                request.getBondId(), request.getMetricType().name(), interval, aggregation);
    }

    private MetricAggregationResponse parseAggregationResults(List<FluxTable> tables, MetricAggregationRequest request) {
        MetricAggregationResponse response = MetricAggregationResponse.builder()
                .bondId(request.getBondId())
                .projectId(request.getProjectId())
                .metricType(request.getMetricType().name())
                .interval(request.getInterval())
                .aggregationFunction(request.getAggregationFunction())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .timeSeries(new ArrayList<>())
                .statistics(new HashMap<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal min = null;
        BigDecimal max = null;
        int count = 0;

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Double value = (Double) record.getValue();
                Instant time = (Instant) record.getValueByKey("_time");

                if (value != null) {
                    BigDecimal decimalValue = BigDecimal.valueOf(value);

                    // Update statistics
                    total = total.add(decimalValue);
                    if (min == null || decimalValue.compareTo(min) < 0) min = decimalValue;
                    if (max == null || decimalValue.compareTo(max) > 0) max = decimalValue;
                    count++;

                    // Add to time series
                    LocalDateTime timestamp = LocalDateTime.ofInstant(time, ZoneId.systemDefault());
                    response.getTimeSeries().add(TimeSeriesPoint.builder()
                            .timestamp(timestamp)
                            .value(decimalValue)
                            .count(1)
                            .build());
                }
            }
        }

        // Calculate final statistics
        response.setTotalValue(total);
        response.setMinValue(min != null ? min : BigDecimal.ZERO);
        response.setMaxValue(max != null ? max : BigDecimal.ZERO);
        response.setDataPointCount(count);
        response.setAverageValue(count > 0 ? total.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        response.getStatistics().put("variance", calculateVariance(response.getTimeSeries(), response.getAverageValue()));
        response.getStatistics().put("standardDeviation", calculateStandardDeviation(response.getTimeSeries(), response.getAverageValue()));

        return response;
    }

    private BigDecimal calculateVariance(List<TimeSeriesPoint> timeSeries, BigDecimal mean) {
        if (timeSeries.size() <= 1) return BigDecimal.ZERO;

        BigDecimal sumSquaredDifferences = BigDecimal.ZERO;
        for (TimeSeriesPoint point : timeSeries) {
            BigDecimal difference = point.getValue().subtract(mean);
            sumSquaredDifferences = sumSquaredDifferences.add(difference.pow(2));
        }

        return sumSquaredDifferences.divide(BigDecimal.valueOf(timeSeries.size() - 1), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateStandardDeviation(List<TimeSeriesPoint> timeSeries, BigDecimal mean) {
        BigDecimal variance = calculateVariance(timeSeries, mean);
        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
    }
}