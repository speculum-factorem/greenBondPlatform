package com.esgbank.greenbond.core.exception;

public class BusinessRuleException extends PlatformException {

    public BusinessRuleException(String message) {
        super("BUSINESS_RULE_VIOLATION", message);
    }

    public BusinessRuleException(String ruleCode, String message) {
        super(ruleCode, message);
    }

    public BusinessRuleException(String ruleCode, String message, String details) {
        super(ruleCode, message, details);
    }
}