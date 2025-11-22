package com.esgbank.greenbond.monitoring.exception;

public class GoalNotFoundException extends ImpactMonitoringException {

    public GoalNotFoundException(String message) {
        super("GOAL_NOT_FOUND", message);
    }
}