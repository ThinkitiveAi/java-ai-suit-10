package com.healthfirst.dto;

import com.healthfirst.entity.embedded.ClinicAddress;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public class ProviderRegistrationRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number should be valid international format")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", 
             message = "Password must contain at least 8 characters, one uppercase, one lowercase, one number and one special character")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @NotBlank(message = "Specialization is required")
    @Size(min = 3, max = 100, message = "Specialization must be between 3 and 100 characters")
    private String specialization;

    @NotBlank(message = "License number is required")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "License number must be alphanumeric")
    private String licenseNumber;

    @NotNull(message = "Years of experience is required")
    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 50, message = "Years of experience cannot exceed 50")
    private Integer yearsOfExperience;

    @Valid
    @NotNull(message = "Clinic address is required")
    private ClinicAddress clinicAddress;

    // Default constructor
    public ProviderRegistrationRequest() {}

    // Constructor
    public ProviderRegistrationRequest(String firstName, String lastName, String email, 
                                     String phoneNumber, String password, String confirmPassword,
                                     String specialization, String licenseNumber, 
                                     Integer yearsOfExperience, ClinicAddress clinicAddress) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.yearsOfExperience = yearsOfExperience;
        this.clinicAddress = clinicAddress;
    }

    // Getters and Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
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

    @Override
    public String toString() {
        return "ProviderRegistrationRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", specialization='" + specialization + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", yearsOfExperience=" + yearsOfExperience +
                ", clinicAddress=" + clinicAddress +
                '}';
    }
} 