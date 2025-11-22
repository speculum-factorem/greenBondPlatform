package com.esgbank.greenbond.core.exception;

public class ResourceNotFoundException extends PlatformException {

    public ResourceNotFoundException(String resourceName, String identifier) {
        super("RESOURCE_NOT_FOUND",
                String.format("%s not found with identifier: %s", resourceName, identifier));
    }

    public ResourceNotFoundException(String resourceName, String identifier, String details) {
        super("RESOURCE_NOT_FOUND",
                String.format("%s not found with identifier: %s", resourceName, identifier),
                details);
    }
}