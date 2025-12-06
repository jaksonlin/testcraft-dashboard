package com.example.annotationextractor.web.dto;

public class TokenGenerationResponseDto {

    private String token;
    private String refreshToken;
    private String username;
    private String tokenType = "Bearer";
    private long expiresIn; // milliseconds until expiration

    public TokenGenerationResponseDto() {
    }

    public TokenGenerationResponseDto(String token, String refreshToken, String username, long expiresIn) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.username = username;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}

