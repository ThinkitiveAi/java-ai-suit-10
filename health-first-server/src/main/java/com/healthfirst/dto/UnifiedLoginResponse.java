package com.healthfirst.dto;

import com.healthfirst.enums.Gender;
import com.healthfirst.enums.VerificationStatus;

import java.time.LocalDate;
import java.util.UUID;

public class UnifiedLoginResponse {

    private boolean success;
    private String message;
    private String errorCode;
    private LoginData data;

    // Default constructor
    public UnifiedLoginResponse() {}

    // Constructor
    public UnifiedLoginResponse(boolean success, String message, String errorCode, LoginData data) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
        this.data = data;
    }

    // Static factory methods
    public static UnifiedLoginResponse success(LoginData data) {
        return new UnifiedLoginResponse(true, "Login successful", null, data);
    }

    public static UnifiedLoginResponse error(String message, String errorCode) {
        return new UnifiedLoginResponse(false, message, errorCode, null);
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
        private String refreshToken; // null for patients
        private long expiresIn;
        private String tokenType = "Bearer";
        private String userType; // "patient" or "provider"
        private UserData user; // polymorphic user data

        public LoginData() {}

        public LoginData(String accessToken, String refreshToken, long expiresIn, String userType, UserData user) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
            this.userType = userType;
            this.user = user;
        }

        // For patient login (no refresh token)
        public LoginData(String accessToken, long expiresIn, String userType, UserData user) {
            this.accessToken = accessToken;
            this.expiresIn = expiresIn;
            this.userType = userType;
            this.user = user;
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

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public UserData getUser() {
            return user;
        }

        public void setUser(UserData user) {
            this.user = user;
        }
    }

    // Base class for user data (to be extended by provider/patient specific data)
    public static abstract class UserData {
        protected UUID id;
        protected String firstName;
        protected String lastName;
        protected String email;
        protected boolean isActive;

        public UserData() {}

        public UserData(UUID id, String firstName, String lastName, String email, boolean isActive) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
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

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            isActive = active;
        }
    }

    // Provider-specific user data
    public static class ProviderData extends UserData {
        private String specialization;
        private VerificationStatus verificationStatus;

        public ProviderData() {}

        public ProviderData(UUID id, String firstName, String lastName, String email, 
                           String specialization, VerificationStatus verificationStatus, boolean isActive) {
            super(id, firstName, lastName, email, isActive);
            this.specialization = specialization;
            this.verificationStatus = verificationStatus;
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
    }

    // Patient-specific user data
    public static class PatientData extends UserData {
        private LocalDate dateOfBirth;
        private Gender gender;
        private boolean emailVerified;
        private boolean phoneVerified;

        public PatientData() {}

        public PatientData(UUID id, String firstName, String lastName, String email, 
                          LocalDate dateOfBirth, Gender gender, boolean emailVerified, 
                          boolean phoneVerified, boolean isActive) {
            super(id, firstName, lastName, email, isActive);
            this.dateOfBirth = dateOfBirth;
            this.gender = gender;
            this.emailVerified = emailVerified;
            this.phoneVerified = phoneVerified;
        }

        public LocalDate getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public Gender getGender() {
            return gender;
        }

        public void setGender(Gender gender) {
            this.gender = gender;
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
    }
} 