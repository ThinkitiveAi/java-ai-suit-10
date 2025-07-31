package com.healthfirst.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UnifiedLoginRequest {

    @NotBlank(message = "Identifier (email or phone) is required")
    private String identifier; // email or phone_number

    @NotBlank(message = "Password is required")
    @Size(min = 1, message = "Password cannot be empty")
    private String password;

    private Boolean rememberMe = false; // optional, extends token expiry (providers only)
    
    private String userType; // optional: "patient" or "provider", auto-detected if not provided

    // Default constructor
    public UnifiedLoginRequest() {}

    // Constructor
    public UnifiedLoginRequest(String identifier, String password, Boolean rememberMe, String userType) {
        this.identifier = identifier;
        this.password = password;
        this.rememberMe = rememberMe;
        this.userType = userType;
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

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    @Override
    public String toString() {
        return "UnifiedLoginRequest{" +
                "identifier='" + identifier + '\'' +
                ", userType='" + userType + '\'' +
                ", rememberMe=" + rememberMe +
                '}';
    }
} 