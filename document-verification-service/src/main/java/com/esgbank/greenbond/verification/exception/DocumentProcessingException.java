package com.esgbank.greenbond.verification.exception;

import lombok.Getter;

@Getter
public class DocumentProcessingException extends RuntimeException {

    private final String errorCode;

    public DocumentProcessingException(String message) {
        super(message);
        this.errorCode = "DOCUMENT_PROCESSING_ERROR";
    }

    public DocumentProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "DOCUMENT_PROCESSING_ERROR";
    }

    public DocumentProcessingException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}