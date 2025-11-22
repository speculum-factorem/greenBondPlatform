package com.esgbank.greenbond.gateway.service;

import com.esgbank.greenbond.gateway.dto.AuthResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldAuthenticateUserWithValidCredentials() {
        StepVerifier.create(authService.authenticate("issuer@company.com", "password"))
                .expectNextMatches(response ->
                        response.getAccessToken() != null &&
                                response.getUser() != null)
                .verifyComplete();
    }

    @Test
    void shouldFailAuthenticationWithInvalidCredentials() {
        StepVerifier.create(authService.authenticate("user@company.com", "wrong-password"))
                .expectError()
                .verify();
    }
}