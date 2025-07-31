package com.healthfirst.dto;

import java.util.UUID;

public class PatientRegistrationResponse {

    private boolean success;
    private String message;
    private PatientData data;
    private String errorCode;

    // Default constructor
    public PatientRegistrationResponse() {}

    // Success constructor
    public PatientRegistrationResponse(boolean success, String message, PatientData data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Error constructor
    public PatientRegistrationResponse(boolean success, String message, String errorCode) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
    }

    // Factory methods
    public static PatientRegistrationResponse success(String message, PatientData data) {
        return new PatientRegistrationResponse(true, message, data);
    }

    public static PatientRegistrationResponse error(String message) {
        return new PatientRegistrationResponse(false, message, "REGISTRATION_ERROR");
    }

    public static PatientRegistrationResponse error(String message, String errorCode) {
        return new PatientRegistrationResponse(false, message, errorCode);
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

    public PatientData getData() {
        return data;
    }

    public void setData(PatientData data) {
        this.data = data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    // Inner class for patient data
    public static class PatientData {
        private UUID id;
        private String email;
        private String phoneNumber;
        private boolean emailVerified;
        private boolean phoneVerified;

        public PatientData() {}

        public PatientData(UUID id, String email, String phoneNumber, boolean emailVerified, boolean phoneVerified) {
            this.id = id;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.emailVerified = emailVerified;
            this.phoneVerified = phoneVerified;
        }

        // Getters and Setters
        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public boolean isEmailVerified() {
            return emailVerified;
        }

        public void setEmailVerified(boolean emailVerified) {
            this.emailVerified = emailVerified;
        }

        public boolean isPhoneVerified() {
            return phoneVerified;
        }

        public void setPhoneVerified(boolean phoneVerified) {
            this.phoneVerified = phoneVerified;
        }

        @Override
        public String toString() {
            return "PatientData{" +
                    "id=" + id +
                    ", email='" + email + '\'' +
                    ", phoneNumber='" + phoneNumber + '\'' +
                    ", emailVerified=" + emailVerified +
                    ", phoneVerified=" + phoneVerified +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "PatientRegistrationResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }
} 