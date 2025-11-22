package com.esgbank.greenbond.core.exception;

import lombok.Getter;

@Getter
public class PlatformException extends RuntimeException {

    private final String errorCode;
    private final String details;

    public PlatformException(String message) {
        super(message);
        this.errorCode = "PLATFORM_ERROR";
        this.details = null;
    }

    public PlatformException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "PLATFORM_ERROR";
        this.details = null;
    }

    public PlatformException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }

    public PlatformException(String errorCode, String message, String details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public PlatformException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = null;
    }

    public PlatformException(String errorCode, String message, String details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details;
    }
}