package com.esgbank.greenbond.verification.exception;

public class DocumentNotFoundException extends DocumentProcessingException {

    public DocumentNotFoundException(String message) {
        super("DOCUMENT_NOT_FOUND", message);
    }
}