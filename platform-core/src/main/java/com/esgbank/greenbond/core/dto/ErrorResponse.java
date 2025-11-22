package com.esgbank.greenbond.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response")
public class ErrorResponse {

    @Schema(description = "Error code", example = "VALIDATION_ERROR")
    private String errorCode;

    @Schema(description = "Error message", example = "Validation failed")
    private String message;

    @Schema(description = "Detailed error description")
    private String detail;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Request path", example = "/api/v1/bonds")
    private String path;

    @Schema(description = "Request method", example = "POST")
    private String method;

    @Schema(description = "Error timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "Request ID for tracing")
    private String requestId;

    @Schema(description = "Field-level validation errors")
    private Map<String, String> fieldErrors;

    @Schema(description = "List of global errors")
    private List<String> globalErrors;

    @Schema(description = "Stack trace (only in development)")
    private String stackTrace;

    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder()
                .timestamp(LocalDateTime.now());
    }
}