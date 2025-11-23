package com.esgbank.greenbond.gateway.dto;

// Временно отключено из-за проблем с совместимостью зависимостей
//import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@Schema(description = "Authentication response")
public class AuthResponse {

    //@Schema(description = "JWT access token")
    private String accessToken;

    //@Schema(description = "Token type")
    private String tokenType;

    //@Schema(description = "Expiration time in seconds")
    private Long expiresIn;

    //@Schema(description = "User information")
    private UserInfo user;
}