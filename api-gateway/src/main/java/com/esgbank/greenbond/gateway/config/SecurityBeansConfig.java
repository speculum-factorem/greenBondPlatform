package com.esgbank.greenbond.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security beans configuration.
 */
@Configuration
public class SecurityBeansConfig {

    /**
     * BCrypt password encoder for secure password hashing.
     * BCrypt automatically handles salt generation and is industry standard.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Strength factor 12 (good balance between security and performance)
    }
}

