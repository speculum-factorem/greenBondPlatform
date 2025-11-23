package com.esgbank.greenbond.gateway.config;

import com.esgbank.greenbond.gateway.model.User;
import com.esgbank.greenbond.gateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Initializes default users for the application.
 * Creates test users with different roles for demonstration purposes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Initializing default users...");

        // Default password for all test users: "password123"
        String defaultPassword = passwordEncoder.encode("password123");
        LocalDateTime now = LocalDateTime.now();

        List<User> defaultUsers = List.of(
                createUser("admin", "admin@esgbank.com", defaultPassword, "ADMIN",
                        List.of("ADMIN", "USER"), User.UserStatus.ACTIVE, now),
                createUser("issuer1", "issuer1@esgbank.com", defaultPassword, "ISSUER",
                        List.of("ISSUER", "USER"), User.UserStatus.ACTIVE, now),
                createUser("issuer2", "issuer2@esgbank.com", defaultPassword, "ISSUER",
                        List.of("ISSUER", "USER"), User.UserStatus.ACTIVE, now),
                createUser("investor1", "investor1@esgbank.com", defaultPassword, "INVESTOR",
                        List.of("INVESTOR", "USER"), User.UserStatus.ACTIVE, now),
                createUser("investor2", "investor2@esgbank.com", defaultPassword, "INVESTOR",
                        List.of("INVESTOR", "USER"), User.UserStatus.ACTIVE, now),
                createUser("auditor1", "auditor1@esgbank.com", defaultPassword, "AUDITOR",
                        List.of("AUDITOR", "USER"), User.UserStatus.ACTIVE, now),
                createUser("auditor2", "auditor2@esgbank.com", defaultPassword, "AUDITOR",
                        List.of("AUDITOR", "USER"), User.UserStatus.ACTIVE, now),
                createUser("user1", "user1@esgbank.com", defaultPassword, "USER",
                        List.of("USER"), User.UserStatus.ACTIVE, now)
        );

        Flux.fromIterable(defaultUsers)
                .flatMap(user -> 
                    userRepository.existsByUsername(user.getUsername())
                            .flatMap(exists -> {
                                if (!exists) {
                                    return userRepository.save(user)
                                            .doOnSuccess(u -> log.info("Created default user: {}", u.getUsername()));
                                } else {
                                    log.debug("User already exists: {}", user.getUsername());
                                    return Mono.empty();
                                }
                            })
                )
                .then()
                .doOnSuccess(v -> log.info("User initialization completed"))
                .doOnError(error -> log.error("Error initializing users: {}", error.getMessage(), error))
                .block();
    }

    private User createUser(String username, String email, String passwordHash, 
                           String userType, List<String> roles, User.UserStatus status, 
                           LocalDateTime createdAt) {
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(username)
                .email(email)
                .passwordHash(passwordHash)
                .userType(userType)
                .status(status != null ? status.name() : User.UserStatus.INACTIVE.name())
                .failedLoginAttempts(0)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .version(0L)
                .build();
        user.setRolesList(roles);
        return user;
    }
}

