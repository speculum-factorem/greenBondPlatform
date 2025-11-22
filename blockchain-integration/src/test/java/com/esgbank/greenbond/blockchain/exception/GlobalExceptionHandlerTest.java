package com.esgbank.greenbond.blockchain.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void shouldHandleBlockchainException() {
        // Given
        BlockchainException exception = new BlockchainException("Blockchain operation failed");

        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleBlockchainException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("BLOCKCHAIN_ERROR");
        assertThat(response.getBody().get("message")).isEqualTo("Blockchain operation failed");
    }

    @Test
    void shouldHandleGenericException() {
        // Given
        Exception exception = new Exception("Unexpected error");

        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().get("message")).isEqualTo("An unexpected error occurred");
    }
}