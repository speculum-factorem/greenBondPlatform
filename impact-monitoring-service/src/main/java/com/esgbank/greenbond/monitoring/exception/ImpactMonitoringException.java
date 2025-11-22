package com.esgbank.greenbond.monitoring.exception;

import lombok.Getter;

@Getter
public class ImpactMonitoringException extends RuntimeException {

    private final String errorCode;

    public ImpactMonitoringException(String message) {
        super(message);
        this.errorCode = "IMPACT_MONITORING_ERROR";
    }

    public ImpactMonitoringException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "IMPACT_MONITORING_ERROR";
    }

    public ImpactMonitoringException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}