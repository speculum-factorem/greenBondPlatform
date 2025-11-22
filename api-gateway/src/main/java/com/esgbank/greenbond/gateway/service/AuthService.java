package com.esgbank.greenbond.gateway.service;

import com.esgbank.greenbond.gateway.dto.AuthResponse;
import com.esgbank.greenbond.gateway.dto.UserInfo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AuthService {

    @Value("${app.jwt.secret:defaultSecretKeyForJWTTokenGenerationInGreenBondPlatform}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:3600}")
    private Long jwtExpiration;

    public Mono<AuthResponse> authenticate(String username, String password) {
        log.debug("Authenticating user: {}", username);

        // TODO: Integrate with real authentication service
        // For now, mock authentication based on username pattern
        return validateCredentials(username, password)
                .map(userInfo -> {
                    String token = generateToken(userInfo);
                    log.info("Generated JWT token for user: {}", username);

                    return AuthResponse.builder()
                            .accessToken(token)
                            .tokenType("Bearer")
                            .expiresIn(jwtExpiration)
                            .user(userInfo)
                            .build();
                });
    }

    public Mono<UserInfo> validateCurrentToken() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getName)
                .flatMap(username -> {
                    // TODO: Fetch user info from user service
                    log.debug("Validating token for user: {}", username);
                    return createMockUserInfo(username);
                });
    }

    private Mono<UserInfo> validateCredentials(String username, String password) {
        // Mock validation - replace with real authentication
        if ("password".equals(password)) {
            return createMockUserInfo(username);
        }
        return Mono.error(new RuntimeException("Invalid credentials"));
    }

    private Mono<UserInfo> createMockUserInfo(String username) {
        UserInfo userInfo = UserInfo.builder()
                .userId(generateUserId(username))
                .username(username)
                .email(username)
                .roles(determineRoles(username))
                .userType(determineUserType(username))
                .build();

        log.debug("Created user info: {}", userInfo);
        return Mono.just(userInfo);
    }

    private String generateToken(UserInfo userInfo) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(userInfo.getUsername())
                .claim("userId", userInfo.getUserId())
                .claim("roles", userInfo.getRoles())
                .claim("userType", userInfo.getUserType())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plusSeconds(jwtExpiration)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private List<String> determineRoles(String username) {
        if (username.contains("issuer")) return List.of("ISSUER", "USER");
        if (username.contains("investor")) return List.of("INVESTOR", "USER");
        if (username.contains("auditor")) return List.of("AUDITOR", "USER");
        if (username.contains("admin")) return List.of("ADMIN", "USER");
        return List.of("USER");
    }

    private String determineUserType(String username) {
        if (username.contains("issuer")) return "ISSUER";
        if (username.contains("investor")) return "INVESTOR";
        if (username.contains("auditor")) return "AUDITOR";
        if (username.contains("admin")) return "ADMIN";
        return "USER";
    }

    private String generateUserId(String username) {
        return "user-" + Math.abs(username.hashCode());
    }
}