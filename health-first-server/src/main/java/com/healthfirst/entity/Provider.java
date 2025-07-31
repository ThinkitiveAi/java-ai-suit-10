package com.healthfirst.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.healthfirst.entity.embedded.ClinicAddress;
import com.healthfirst.enums.VerificationStatus;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "providers")
public class Provider {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number should be valid international format")
    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @JsonIgnore
    @NotBlank(message = "Password is required")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NotBlank(message = "Specialization is required")
    @Size(min = 3, max = 100, message = "Specialization must be between 3 and 100 characters")
    @Column(name = "specialization", nullable = false, length = 100)
    private String specialization;

    @NotBlank(message = "License number is required")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "License number must be alphanumeric")
    @Column(name = "license_number", nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @NotNull(message = "Years of experience is required")
    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 50, message = "Years of experience cannot exceed 50")
    @Column(name = "years_of_experience", nullable = false)
    private Integer yearsOfExperience;

    @Valid
    @Embedded
    @NotNull(message = "Clinic address is required")
    private ClinicAddress clinicAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "license_document_url")
    private String licenseDocumentUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Authentication security fields
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Column(name = "last_successful_login")
    private LocalDateTime lastSuccessfulLogin;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Default constructor
    public Provider() {}

    // Constructor
    public Provider(String firstName, String lastName, String email, String phoneNumber, 
                   String passwordHash, String specialization, String licenseNumber, 
                   Integer yearsOfExperience, ClinicAddress clinicAddress) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.yearsOfExperience = yearsOfExperience;
        this.clinicAddress = clinicAddress;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public ClinicAddress getClinicAddress() {
        return clinicAddress;
    }

    public void setClinicAddress(ClinicAddress clinicAddress) {
        this.clinicAddress = clinicAddress;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getLicenseDocumentUrl() {
        return licenseDocumentUrl;
    }

    public void setLicenseDocumentUrl(String licenseDocumentUrl) {
        this.licenseDocumentUrl = licenseDocumentUrl;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getAccountLockedUntil() {
        return accountLockedUntil;
    }

    public void setAccountLockedUntil(LocalDateTime accountLockedUntil) {
        this.accountLockedUntil = accountLockedUntil;
    }

    public LocalDateTime getLastSuccessfulLogin() {
        return lastSuccessfulLogin;
    }

    public void setLastSuccessfulLogin(LocalDateTime lastSuccessfulLogin) {
        this.lastSuccessfulLogin = lastSuccessfulLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Authentication security methods
    public boolean isAccountLocked() {
        return accountLockedUntil != null && accountLockedUntil.isAfter(LocalDateTime.now());
    }

    public void recordSuccessfulLogin() {
        this.lastSuccessfulLogin = LocalDateTime.now();
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
    }

    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
    }

    public void lockAccount(int lockDurationMinutes) {
        this.accountLockedUntil = LocalDateTime.now().plusMinutes(lockDurationMinutes);
    }

    @Override
    public String toString() {
        return "Provider{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", specialization='" + specialization + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", yearsOfExperience=" + yearsOfExperience +
                ", verificationStatus=" + verificationStatus +
                ", isActive=" + isActive +
                ", failedLoginAttempts=" + failedLoginAttempts +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 