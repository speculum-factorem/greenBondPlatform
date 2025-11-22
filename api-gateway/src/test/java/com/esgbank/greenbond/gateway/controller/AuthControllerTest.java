package com.esgbank.greenbond.gateway.controller;

import com.esgbank.greenbond.gateway.dto.AuthRequest;
import com.esgbank.greenbond.gateway.dto.AuthResponse;
import com.esgbank.greenbond.gateway.dto.UserInfo;
import com.esgbank.greenbond.gateway.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthService authService;

    @Test
    void shouldAuthenticateUserSuccessfully() {
        AuthRequest request = new AuthRequest("issuer@company.com", "password");
        AuthResponse response = AuthResponse.builder()
                .accessToken("jwt-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(UserInfo.builder()
                        .userId("user-123")
                        .username("issuer@company.com")
                        .roles(List.of("ISSUER"))
                        .build())
                .build();

        when(authService.authenticate(anyString(), anyString())).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("jwt-token")
                .jsonPath("$.user.username").isEqualTo("issuer@company.com");
    }

    @Test
    void shouldReturnBadRequestForInvalidInput() {
        AuthRequest invalidRequest = new AuthRequest("", "");

        webTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }
}