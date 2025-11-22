package com.esgbank.greenbond.monitoring.exception;

public class MetricNotFoundException extends ImpactMonitoringException {

    public MetricNotFoundException(String message) {
        super("METRIC_NOT_FOUND", message);
    }
}