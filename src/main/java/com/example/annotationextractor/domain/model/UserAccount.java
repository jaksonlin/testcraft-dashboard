package com.example.annotationextractor.domain.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Domain model representing an application user.
 *
 * This model is intentionally persistence-agnostic – the JDBC layer
 * is responsible for mapping it to concrete tables (users, roles, etc).
 */
public class UserAccount {

    private Long id;
    private String username;
    /**
     * Encoded password (e.g. bcrypt or delegating encoder format).
     */
    private String passwordHash;
    private boolean enabled;
    /**
     * True if the user is still using the initial default password.
     * Can be used to force password change on next login in the future.
     */
    private boolean defaultPasswordInUse;
    private List<String> roles;
    private LocalDateTime createdAt;

    public UserAccount() {
    }

    public UserAccount(Long id,
                       String username,
                       String passwordHash,
                       boolean enabled,
                       boolean defaultPasswordInUse,
                       List<String> roles,
                       LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.defaultPasswordInUse = defaultPasswordInUse;
        this.roles = roles;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDefaultPasswordInUse() {
        return defaultPasswordInUse;
    }

    public void setDefaultPasswordInUse(boolean defaultPasswordInUse) {
        this.defaultPasswordInUse = defaultPasswordInUse;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


