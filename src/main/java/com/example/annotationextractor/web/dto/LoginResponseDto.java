package com.example.annotationextractor.web.dto;

import java.util.List;

public class LoginResponseDto {

    private String token;
    private String username;
    private List<String> roles;
    // Indicates whether the user's default (initial) password is still in use
    private boolean defaultPasswordInUse;

    public LoginResponseDto() {
    }

    public LoginResponseDto(String token, String username, List<String> roles, boolean defaultPasswordInUse) {
        this.token = token;
        this.username = username;
        this.roles = roles;
        this.defaultPasswordInUse = defaultPasswordInUse;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean isDefaultPasswordInUse() {
        return defaultPasswordInUse;
    }

    public void setDefaultPasswordInUse(boolean defaultPasswordInUse) {
        this.defaultPasswordInUse = defaultPasswordInUse;
    }
}


