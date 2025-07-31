package com.healthfirst.dto;

import com.healthfirst.enums.VerificationStatus;

import java.util.UUID;

public class ProviderRegistrationResponse {

    private boolean success;
    private String message;
    private ProviderData data;

    public ProviderRegistrationResponse() {}

    public ProviderRegistrationResponse(boolean success, String message, ProviderData data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static ProviderRegistrationResponse success(ProviderData data) {
        return new ProviderRegistrationResponse(true, 
            "Provider registered successfully. Verification email sent.", data);
    }

    public static ProviderRegistrationResponse error(String message) {
        return new ProviderRegistrationResponse(false, message, null);
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

    public ProviderData getData() {
        return data;
    }

    public void setData(ProviderData data) {
        this.data = data;
    }

    // Inner class for provider data
    public static class ProviderData {
        private UUID providerId;
        private String email;
        private VerificationStatus verificationStatus;

        public ProviderData() {}

        public ProviderData(UUID providerId, String email, VerificationStatus verificationStatus) {
            this.providerId = providerId;
            this.email = email;
            this.verificationStatus = verificationStatus;
        }

        public UUID getProviderId() {
            return providerId;
        }

        public void setProviderId(UUID providerId) {
            this.providerId = providerId;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public VerificationStatus getVerificationStatus() {
            return verificationStatus;
        }

        public void setVerificationStatus(VerificationStatus verificationStatus) {
            this.verificationStatus = verificationStatus;
        }
    }
} 