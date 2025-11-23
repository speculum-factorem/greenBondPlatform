package com.esgbank.greenbond.gateway.repository;

import com.esgbank.greenbond.gateway.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Reactive repository for User entities.
 */
@Repository
public interface UserRepository extends ReactiveCrudRepository<User, String> {

    /**
     * Find user by username.
     *
     * @param username The username
     * @return Mono containing User if found
     */
    Mono<User> findByUsername(String username);

    /**
     * Find user by email.
     *
     * @param email The email
     * @return Mono containing User if found
     */
    Mono<User> findByEmail(String email);

    /**
     * Check if user exists by username.
     *
     * @param username The username
     * @return Mono containing true if exists
     */
    Mono<Boolean> existsByUsername(String username);

    /**
     * Check if user exists by email.
     *
     * @param email The email
     * @return Mono containing true if exists
     */
    Mono<Boolean> existsByEmail(String email);
}

