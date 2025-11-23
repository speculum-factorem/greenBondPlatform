package com.esgbank.greenbond.gateway.service;

import com.esgbank.greenbond.gateway.dto.AuthResponse;
import com.esgbank.greenbond.gateway.dto.UserInfo;
import com.esgbank.greenbond.gateway.model.User;
import com.esgbank.greenbond.gateway.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.secret:defaultSecretKeyForJWTTokenGenerationInGreenBondPlatform}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:3600}")
    private Long jwtExpiration;

    @Value("${app.auth.max-failed-attempts:5}")
    private Integer maxFailedAttempts;

    @Value("${app.auth.lockout-duration-minutes:30}")
    private Integer lockoutDurationMinutes;

    public Mono<AuthResponse> authenticate(String username, String password) {
        log.debug("Authenticating user: {}", username);

        // Валидация входных данных: проверяем что username и password не пустые
        if (username == null || username.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Username cannot be empty"));
        }
        if (password == null || password.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Password cannot be empty"));
        }

        // Ищем пользователя по username, если не найден - пробуем по email
        return userRepository.findByUsername(username)
                .switchIfEmpty(userRepository.findByEmail(username)) // Также пробуем найти по email
                .flatMap(user -> {
                    // Проверяем не заблокирован ли аккаунт (защита от brute force)
                    if (user.isAccountLocked()) {
                        log.warn("Authentication attempt for locked account: {}", username);
                        return Mono.error(new BadCredentialsException("Account is locked. Please try again later."));
                    }

                    // Проверяем активен ли аккаунт
                    if (!user.isActive()) {
                        log.warn("Authentication attempt for inactive account: {}", username);
                        return Mono.error(new BadCredentialsException("Account is not active."));
                    }

                    // Проверяем статус аккаунта
                    if (user.getStatusEnum() != User.UserStatus.ACTIVE) {
                        log.warn("Authentication attempt for non-active account: {} (status: {})", 
                                username, user.getStatus());
                        return Mono.error(new BadCredentialsException("Account is not active."));
                    }

                    // Проверяем пароль используя BCrypt
                    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                        return handleFailedLogin(user);
                    }

                    // Успешная аутентификация: обновляем время последнего входа и генерируем токен
                    return handleSuccessfulLogin(user)
                            .map(u -> {
                                UserInfo userInfo = toUserInfo(u);
                                String token = generateToken(userInfo);
                                
                                log.info("User authenticated successfully: {} with roles: {}", 
                                        username, userInfo.getRoles());

                                return AuthResponse.builder()
                                        .accessToken(token)
                                        .tokenType("Bearer")
                                        .expiresIn(jwtExpiration)
                                        .user(userInfo)
                                        .build();
                            });
                })
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid credentials")))
                .cast(AuthResponse.class)
                .doOnError(error -> {
                    if (!(error instanceof BadCredentialsException)) {
                        log.error("Authentication error for user {}: {}", username, error.getMessage(), error);
                    }
                });
    }

    public Mono<UserInfo> validateCurrentToken() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getName)
                .flatMap(username -> {
                    log.debug("Validating token for user: {}", username);
                    return userRepository.findByUsername(username)
                            .map(this::toUserInfo)
                            .switchIfEmpty(Mono.error(new RuntimeException("User not found: " + username)));
                })
                .switchIfEmpty(Mono.error(new RuntimeException("No authentication found")));
    }

    private Mono<User> handleSuccessfulLogin(User user) {
        // Обновляем время последнего входа и сбрасываем счетчик неудачных попыток
        user.setLastLoginAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    private Mono<User> handleFailedLogin(User user) {
        // Увеличиваем счетчик неудачных попыток входа
        int failedAttempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()) + 1;
        user.setFailedLoginAttempts(failedAttempts);
        user.setUpdatedAt(LocalDateTime.now());

        // Если превышен лимит попыток - блокируем аккаунт на определенное время
        if (failedAttempts >= maxFailedAttempts) {
            user.setStatusEnum(User.UserStatus.LOCKED);
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
            log.warn("Account locked due to too many failed login attempts: {}", user.getUsername());
        }

        return userRepository.save(user)
                .then(Mono.error(new BadCredentialsException("Invalid credentials")));
    }

    private UserInfo toUserInfo(User user) {
        // Преобразуем User entity в DTO для передачи клиенту
        List<String> roles = user.getRolesList();
        if (roles == null || roles.isEmpty()) {
            roles = List.of("USER");
        }
        return UserInfo.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .userType(user.getUserType())
                .build();
    }

    private String generateToken(UserInfo userInfo) {
        // Создаем секретный ключ из конфигурации для подписи токена
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        // Генерируем JWT токен с информацией о пользователе и ролях
        return Jwts.builder()
                .setSubject(userInfo.getUsername())
                .claim("userId", userInfo.getUserId())
                .claim("roles", userInfo.getRoles())
                .claim("userType", userInfo.getUserType())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plusSeconds(jwtExpiration)))
                .signWith(key)
                .compact();
    }
}
