package com.example.annotationextractor.web.dto;

import java.util.List;

public class CreateUserRequestDto {

    private String username;
    /**
     * List of role names, e.g. ["ROLE_USER"] or ["ROLE_ADMIN","ROLE_USER"].
     */
    private List<String> roles;

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
}


