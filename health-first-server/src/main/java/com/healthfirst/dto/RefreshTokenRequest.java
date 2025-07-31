package com.healthfirst.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    // Default constructor
    public RefreshTokenRequest() {}

    // Constructor
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Getters and Setters
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        return "RefreshTokenRequest{" +
                "refreshToken='[PROTECTED]'" +
                '}';
    }
} 