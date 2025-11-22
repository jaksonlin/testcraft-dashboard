package com.example.annotationextractor.web.dto;

import java.util.List;

public class UserSummaryDto {

    private Long id;
    private String username;
    private boolean enabled;
    private boolean defaultPasswordInUse;
    private List<String> roles;

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
}


