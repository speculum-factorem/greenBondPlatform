package com.esgbank.greenbond.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication request")
public class AuthRequest {

    @NotBlank
    @Schema(description = "Username", example = "issuer@company.com")
    private String username;

    @NotBlank
    @Schema(description = "Password", example = "securePassword123")
    private String password;
}