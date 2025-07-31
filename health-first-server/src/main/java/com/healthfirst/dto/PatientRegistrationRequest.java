package com.healthfirst.dto;

import com.healthfirst.entity.embedded.EmergencyContact;
import com.healthfirst.entity.embedded.InsuranceInfo;
import com.healthfirst.entity.embedded.PatientAddress;
import com.healthfirst.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public class PatientRegistrationRequest {

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

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @Valid
    @NotNull(message = "Address is required")
    private PatientAddress address;

    @Valid
    private EmergencyContact emergencyContact;

    private List<@Size(max = 200, message = "Medical condition must not exceed 200 characters") String> medicalHistory;

    @Valid
    private InsuranceInfo insuranceInfo;

    // Default constructor
    public PatientRegistrationRequest() {}

    // Constructor
    public PatientRegistrationRequest(String firstName, String lastName, String email, 
                                    String phoneNumber, String password, String confirmPassword,
                                    LocalDate dateOfBirth, Gender gender, PatientAddress address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
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

    public PatientAddress getAddress() {
        return address;
    }

    public void setAddress(PatientAddress address) {
        this.address = address;
    }

    public EmergencyContact getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(EmergencyContact emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public List<String> getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(List<String> medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public InsuranceInfo getInsuranceInfo() {
        return insuranceInfo;
    }

    public void setInsuranceInfo(InsuranceInfo insuranceInfo) {
        this.insuranceInfo = insuranceInfo;
    }

    @Override
    public String toString() {
        return "PatientRegistrationRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", gender=" + gender +
                ", address=" + address +
                ", emergencyContact=" + emergencyContact +
                ", insuranceInfo=" + insuranceInfo +
                '}';
    }
} 