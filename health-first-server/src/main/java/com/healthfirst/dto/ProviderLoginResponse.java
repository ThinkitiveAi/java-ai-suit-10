package com.healthfirst.dto;

import com.healthfirst.enums.VerificationStatus;

import java.util.UUID;

public class ProviderLoginResponse {

    private boolean success;
    private String message;
    private String errorCode;
    private LoginData data;

    // Default constructor
    public ProviderLoginResponse() {}

    // Constructor
    public ProviderLoginResponse(boolean success, String message, String errorCode, LoginData data) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
        this.data = data;
    }

    // Static factory methods
    public static ProviderLoginResponse success(LoginData data) {
        return new ProviderLoginResponse(true, "Login successful", null, data);
    }

    public static ProviderLoginResponse error(String message, String errorCode) {
        return new ProviderLoginResponse(false, message, errorCode, null);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public LoginData getData() {
        return data;
    }

    public void setData(LoginData data) {
        this.data = data;
    }

    // Inner class for login data
    public static class LoginData {
        private String accessToken;
        private String refreshToken;
        private long expiresIn;
        private String tokenType = "Bearer";
        private ProviderData provider;

        public LoginData() {}

        public LoginData(String accessToken, String refreshToken, long expiresIn, ProviderData provider) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
            this.provider = provider;
        }

        // Getters and Setters
        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public long getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(long expiresIn) {
            this.expiresIn = expiresIn;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        public ProviderData getProvider() {
            return provider;
        }

        public void setProvider(ProviderData provider) {
            this.provider = provider;
        }
    }

    // Inner class for provider data
    public static class ProviderData {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
        private String specialization;
        private VerificationStatus verificationStatus;
        private boolean isActive;

        public ProviderData() {}

        public ProviderData(UUID id, String firstName, String lastName, String email, 
                           String specialization, VerificationStatus verificationStatus, boolean isActive) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.specialization = specialization;
            this.verificationStatus = verificationStatus;
            this.isActive = isActive;
        }

        // Getters and Setters
        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getSpecialization() {
            return specialization;
        }

        public void setSpecialization(String specialization) {
            this.specialization = specialization;
        }

        public VerificationStatus getVerificationStatus() {
            return verificationStatus;
        }

        public void setVerificationStatus(VerificationStatus verificationStatus) {
            this.verificationStatus = verificationStatus;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            isActive = active;
        }
    }
} 