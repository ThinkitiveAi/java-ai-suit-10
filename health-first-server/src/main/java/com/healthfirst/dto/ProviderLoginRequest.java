package com.healthfirst.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProviderLoginRequest {

    @NotBlank(message = "Identifier (email or phone) is required")
    private String identifier; // email or phone_number

    @NotBlank(message = "Password is required")
    @Size(min = 1, message = "Password cannot be empty")
    private String password;

    private Boolean rememberMe = false; // optional, extends token expiry

    // Default constructor
    public ProviderLoginRequest() {}

    // Constructor
    public ProviderLoginRequest(String identifier, String password, Boolean rememberMe) {
        this.identifier = identifier;
        this.password = password;
        this.rememberMe = rememberMe;
    }

    // Getters and Setters
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    @Override
    public String toString() {
        return "ProviderLoginRequest{" +
                "identifier='" + identifier + '\'' +
                ", rememberMe=" + rememberMe +
                '}';
    }
} 