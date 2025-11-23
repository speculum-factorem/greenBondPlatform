package com.esgbank.greenbond.gateway.controller;

import com.esgbank.greenbond.gateway.dto.AuthRequest;
import com.esgbank.greenbond.gateway.dto.AuthResponse;
import com.esgbank.greenbond.gateway.dto.UserInfo;
import com.esgbank.greenbond.gateway.service.AuthService;
// Временно отключено из-за проблем с совместимостью зависимостей
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
//@Tag(name = "Authentication", description = "API Gateway authentication endpoints")
public class AuthController {

    private final AuthService authService;

    // Эндпоинт для аутентификации пользователя и получения JWT токена
    @PostMapping("/login")
    //@Operation(summary = "User authentication", description = "Authenticate user and return JWT token")
    //@ApiResponses({
    //        @ApiResponse(responseCode = "200", description = "Authentication successful",
    //                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
    //        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
    //        @ApiResponse(responseCode = "400", description = "Invalid request")
    //})
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody AuthRequest authRequest) {
        log.info("Login attempt for user: {}", authRequest.getUsername());

        // Вызываем сервис аутентификации для проверки учетных данных
        return authService.authenticate(authRequest.getUsername(), authRequest.getPassword())
                .map(response -> {
                    log.info("Successful login for user: {}, requestId: {}",
                            authRequest.getUsername(), MDC.get("requestId"));
                    return ResponseEntity.ok(response);
                })
                .doOnError(error -> log.error("Login failed for user: {}. Error: {}",
                        authRequest.getUsername(), error.getMessage()));
    }

    // Эндпоинт для валидации текущего JWT токена и получения информации о пользователе
    @PostMapping("/validate")
    //@Operation(summary = "Validate token", description = "Validate JWT token and return user information")
    public Mono<ResponseEntity<UserInfo>> validateToken() {
        log.debug("Token validation request");
        // Валидируем токен из контекста безопасности и возвращаем информацию о пользователе
        return authService.validateCurrentToken()
                .map(ResponseEntity::ok)
                .doOnError(error -> log.warn("Token validation failed: {}", error.getMessage()));
    }
}