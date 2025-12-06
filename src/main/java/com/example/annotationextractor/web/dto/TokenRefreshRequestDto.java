package com.example.annotationextractor.web.dto;

public class TokenRefreshRequestDto {

    private String refreshToken;

    public TokenRefreshRequestDto() {
    }

    public TokenRefreshRequestDto(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

