package com.esgbank.greenbond.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User entity for authentication and authorization.
 * Uses Spring Data R2DBC for reactive database access.
 * 
 * Note: R2DBC doesn't support List<String> directly, so we use String
 * and convert to/from List in the service layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User {

    @Id
    private String id;

    @Column("username")
    private String username;

    @Column("email")
    private String email;

    @Column("password_hash")
    private String passwordHash; // BCrypt hashed password

    @Column("user_type")
    private String userType; // ISSUER, INVESTOR, AUDITOR, ADMIN, USER

    @Column("roles")
    private String roles; // Stored as comma-separated string, converted to List in service

    @Column("status")
    private String status; // ACTIVE, LOCKED, EXPIRED, INACTIVE

    @Column("failed_login_attempts")
    private Integer failedLoginAttempts;

    @Column("last_login_at")
    private LocalDateTime lastLoginAt;

    @Column("account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    /**
     * Converts roles string to List.
     */
    public List<String> getRolesList() {
        if (roles == null || roles.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (String role : roles.split(",")) {
            String trimmed = role.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    /**
     * Sets roles from List.
     */
    public void setRolesList(List<String> rolesList) {
        if (rolesList == null || rolesList.isEmpty()) {
            this.roles = null;
        } else {
            this.roles = String.join(",", rolesList);
        }
    }

    /**
     * Gets UserStatus enum from string.
     */
    public UserStatus getStatusEnum() {
        if (status == null) {
            return UserStatus.INACTIVE;
        }
        try {
            return UserStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return UserStatus.INACTIVE;
        }
    }

    /**
     * Sets status from enum.
     */
    public void setStatusEnum(UserStatus statusEnum) {
        this.status = statusEnum != null ? statusEnum.name() : null;
    }

    public boolean isAccountLocked() {
        UserStatus statusEnum = getStatusEnum();
        return statusEnum == UserStatus.LOCKED ||
                (accountLockedUntil != null && accountLockedUntil.isAfter(LocalDateTime.now()));
    }

    public boolean isActive() {
        return getStatusEnum() == UserStatus.ACTIVE && !isAccountLocked();
    }

    public enum UserStatus {
        ACTIVE,
        LOCKED,
        EXPIRED,
        INACTIVE
    }
}

