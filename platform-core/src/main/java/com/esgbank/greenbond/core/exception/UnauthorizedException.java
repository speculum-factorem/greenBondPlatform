package com.esgbank.greenbond.core.exception;

public class UnauthorizedException extends PlatformException {

    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }

    public UnauthorizedException(String message, String details) {
        super("UNAUTHORIZED", message, details);
    }
}