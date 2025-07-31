package com.healthfirst.service;

import com.healthfirst.dto.PatientRegistrationRequest;
import com.healthfirst.dto.ProviderRegistrationRequest;
import com.healthfirst.enums.Gender;
import com.healthfirst.repository.PatientRepository;
import com.healthfirst.repository.ProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ValidationService {

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private PatientRepository patientRepository;

    // Predefined list of valid specializations
    private final List<String> validSpecializations = Arrays.asList(
        "Cardiology", "Dermatology", "Emergency Medicine", "Family Medicine", 
        "Internal Medicine", "Neurology", "Obstetrics and Gynecology", "Oncology",
        "Ophthalmology", "Orthopedic Surgery", "Otolaryngology", "Pathology",
        "Pediatrics", "Psychiatry", "Radiology", "Surgery", "Urology",
        "Anesthesiology", "Gastroenterology", "Nephrology", "Pulmonology",
        "Rheumatology", "Endocrinology", "Hematology", "Infectious Disease"
    );

    private final Pattern phonePattern = Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    private final Pattern licensePattern = Pattern.compile("^[A-Za-z0-9]+$");
    private final Pattern passwordPattern = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );
    private final Pattern zipPattern = Pattern.compile("^\\d{5}(-\\d{4})?$");
    private final Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    /**
     * Validate provider registration request
     */
    public List<String> validateProviderRegistration(ProviderRegistrationRequest request) {
        List<String> errors = new ArrayList<>();

        // Basic field validation
        validateBasicFields(request, errors);

        // Business logic validation
        validateBusinessRules(request, errors);

        // Password confirmation validation
        validatePasswordConfirmation(request, errors);

        // Uniqueness validation
        validateUniqueness(request, errors);

        return errors;
    }

    /**
     * Validate patient registration request
     */
    public List<String> validatePatientRegistration(PatientRegistrationRequest request) {
        List<String> errors = new ArrayList<>();

        // Basic field validation for patients
        validatePatientBasicFields(request, errors);

        // Patient-specific business rules
        validatePatientBusinessRules(request, errors);

        // Password confirmation validation (reused)
        validatePatientPasswordConfirmation(request, errors);

        // Patient uniqueness validation
        validatePatientUniqueness(request, errors);

        return errors;
    }

    // ========== Provider Validation Methods (Existing) ==========

    private void validateBasicFields(ProviderRegistrationRequest request, List<String> errors) {
        // Name validation
        if (request.getFirstName() == null || request.getFirstName().trim().length() < 2) {
            errors.add("First name must be at least 2 characters long");
        }
        if (request.getLastName() == null || request.getLastName().trim().length() < 2) {
            errors.add("Last name must be at least 2 characters long");
        }

        // Email validation
        if (request.getEmail() == null || !emailPattern.matcher(request.getEmail().trim().toLowerCase()).matches()) {
            errors.add("Please provide a valid email address");
        }

        // Phone validation
        if (request.getPhoneNumber() == null || !phonePattern.matcher(request.getPhoneNumber()).matches()) {
            errors.add("Phone number must be in valid international format");
        }

        // Password validation
        if (request.getPassword() == null || !passwordPattern.matcher(request.getPassword()).matches()) {
            errors.add("Password must contain at least 8 characters including uppercase, lowercase, number and special character");
        }

        // License number validation
        if (request.getLicenseNumber() == null || !licensePattern.matcher(request.getLicenseNumber()).matches()) {
            errors.add("License number must be alphanumeric");
        }

        // Years of experience validation
        if (request.getYearsOfExperience() == null || request.getYearsOfExperience() < 0 || request.getYearsOfExperience() > 50) {
            errors.add("Years of experience must be between 0 and 50");
        }

        // Address validation
        if (request.getClinicAddress() != null) {
            if (request.getClinicAddress().getZip() == null || !zipPattern.matcher(request.getClinicAddress().getZip()).matches()) {
                errors.add("ZIP code must be in valid format (12345 or 12345-6789)");
            }
        }
    }

    private void validateBusinessRules(ProviderRegistrationRequest request, List<String> errors) {
        // Specialization validation
        if (request.getSpecialization() != null && 
            !validSpecializations.contains(request.getSpecialization())) {
            errors.add("Please select a valid specialization from the predefined list");
        }

        // Data trimming and normalization
        if (request.getEmail() != null) {
            request.setEmail(request.getEmail().trim().toLowerCase());
        }
        if (request.getPhoneNumber() != null) {
            request.setPhoneNumber(request.getPhoneNumber().trim());
        }
        if (request.getLicenseNumber() != null) {
            request.setLicenseNumber(request.getLicenseNumber().trim().toUpperCase());
        }
    }

    private void validatePasswordConfirmation(ProviderRegistrationRequest request, List<String> errors) {
        if (request.getPassword() != null && request.getConfirmPassword() != null) {
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                errors.add("Password and confirm password do not match");
            }
        }
    }

    private void validateUniqueness(ProviderRegistrationRequest request, List<String> errors) {
        // Email uniqueness
        if (request.getEmail() != null && providerRepository.existsByEmail(request.getEmail())) {
            errors.add("Email address is already registered");
        }

        // Phone uniqueness
        if (request.getPhoneNumber() != null && providerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            errors.add("Phone number is already registered");
        }

        // License number uniqueness
        if (request.getLicenseNumber() != null && providerRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            errors.add("License number is already registered");
        }
    }

    // ========== Patient Validation Methods (New) ==========

    private void validatePatientBasicFields(PatientRegistrationRequest request, List<String> errors) {
        // Name validation
        if (request.getFirstName() == null || request.getFirstName().trim().length() < 2) {
            errors.add("First name must be at least 2 characters long");
        }
        if (request.getLastName() == null || request.getLastName().trim().length() < 2) {
            errors.add("Last name must be at least 2 characters long");
        }

        // Email validation
        if (request.getEmail() == null || !emailPattern.matcher(request.getEmail().trim().toLowerCase()).matches()) {
            errors.add("Please provide a valid email address");
        }

        // Phone validation
        if (request.getPhoneNumber() == null || !phonePattern.matcher(request.getPhoneNumber()).matches()) {
            errors.add("Phone number must be in valid international format");
        }

        // Password validation
        if (request.getPassword() == null || !passwordPattern.matcher(request.getPassword()).matches()) {
            errors.add("Password must contain at least 8 characters including uppercase, lowercase, number and special character");
        }

        // Date of birth validation
        if (request.getDateOfBirth() == null) {
            errors.add("Date of birth is required");
        } else {
            if (request.getDateOfBirth().isAfter(LocalDate.now())) {
                errors.add("Date of birth must be in the past");
            }
            
            // Age validation - must be 13 or older
            int age = LocalDate.now().getYear() - request.getDateOfBirth().getYear();
            if (age < 13) {
                errors.add("Patient must be at least 13 years old");
            }
        }

        // Gender validation
        if (request.getGender() == null) {
            errors.add("Gender is required");
        }

        // Address validation
        if (request.getAddress() != null) {
            if (request.getAddress().getZip() == null || !zipPattern.matcher(request.getAddress().getZip()).matches()) {
                errors.add("ZIP code must be in valid format (12345 or 12345-6789)");
            }
        }

        // Emergency contact validation (optional but if provided, must be valid)
        if (request.getEmergencyContact() != null) {
            if (request.getEmergencyContact().getPhone() != null && 
                !request.getEmergencyContact().getPhone().trim().isEmpty() &&
                !phonePattern.matcher(request.getEmergencyContact().getPhone()).matches()) {
                errors.add("Emergency contact phone must be in valid international format");
            }
        }

        // Medical history validation
        if (request.getMedicalHistory() != null) {
            for (String condition : request.getMedicalHistory()) {
                if (condition != null && condition.length() > 200) {
                    errors.add("Medical condition must not exceed 200 characters");
                    break;
                }
            }
        }
    }

    private void validatePatientBusinessRules(PatientRegistrationRequest request, List<String> errors) {
        // Data trimming and normalization
        if (request.getEmail() != null) {
            request.setEmail(request.getEmail().trim().toLowerCase());
        }
        if (request.getPhoneNumber() != null) {
            request.setPhoneNumber(request.getPhoneNumber().trim());
        }

        // Sanitize medical history
        if (request.getMedicalHistory() != null) {
            for (int i = 0; i < request.getMedicalHistory().size(); i++) {
                String condition = request.getMedicalHistory().get(i);
                if (condition != null) {
                    request.getMedicalHistory().set(i, sanitizeInput(condition));
                }
            }
        }
    }

    private void validatePatientPasswordConfirmation(PatientRegistrationRequest request, List<String> errors) {
        if (request.getPassword() != null && request.getConfirmPassword() != null) {
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                errors.add("Password and confirm password do not match");
            }
        }
    }

    private void validatePatientUniqueness(PatientRegistrationRequest request, List<String> errors) {
        // Email uniqueness (check both patient and provider tables for security)
        if (request.getEmail() != null) {
            if (patientRepository.existsByEmail(request.getEmail())) {
                errors.add("Email address is already registered as a patient");
            }
            if (providerRepository.existsByEmail(request.getEmail())) {
                errors.add("Email address is already registered as a provider");
            }
        }

        // Phone uniqueness (check both patient and provider tables)
        if (request.getPhoneNumber() != null) {
            if (patientRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                errors.add("Phone number is already registered as a patient");
            }
            if (providerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                errors.add("Phone number is already registered as a provider");
            }
        }
    }

    // ========== Shared Utility Methods ==========

    /**
     * Get list of valid specializations
     */
    public List<String> getValidSpecializations() {
        return new ArrayList<>(validSpecializations);
    }

    /**
     * Sanitize input to prevent injection attacks
     */
    public String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove potentially dangerous characters
        return input.trim()
                   .replaceAll("[<>\"'%;()&+]", "")
                   .replaceAll("\\s+", " ");
    }

    /**
     * Validate age requirement (13+)
     */
    public boolean isValidAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return false;
        }
        int age = LocalDate.now().getYear() - dateOfBirth.getYear();
        return age >= 13;
    }
} 