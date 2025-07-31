package com.healthfirst.service;

import com.healthfirst.dto.PatientRegistrationRequest;
import com.healthfirst.dto.PatientRegistrationResponse;
import com.healthfirst.entity.Patient;
import com.healthfirst.repository.PatientRepository;
import com.healthfirst.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private PasswordUtil passwordUtil;

    /**
     * Register a new patient with HIPAA-compliant handling
     */
    public PatientRegistrationResponse registerPatient(PatientRegistrationRequest request) {
        try {
            // HIPAA Compliance: Log only non-sensitive information
            logger.info("Starting patient registration for email: {}", 
                       request.getEmail() != null ? maskEmail(request.getEmail()) : "null");

            // Validate the request
            List<String> validationErrors = validationService.validatePatientRegistration(request);
            if (!validationErrors.isEmpty()) {
                logger.warn("Validation failed for patient registration: {}", validationErrors);
                return PatientRegistrationResponse.error("Validation failed: " + String.join(", ", validationErrors), "VALIDATION_ERROR");
            }

            // Sanitize inputs (HIPAA compliance - prevent injection)
            sanitizeRequest(request);

            // Hash the password with bcrypt (≥ 12 salt rounds)
            String hashedPassword = passwordUtil.hashPassword(request.getPassword());

            // Create patient entity
            Patient patient = createPatientFromRequest(request, hashedPassword);

            // Save patient to database
            Patient savedPatient = patientRepository.save(patient);
            logger.info("Patient saved successfully with ID: {}", savedPatient.getId());

            // Create response with only necessary information (HIPAA compliance)
            PatientRegistrationResponse.PatientData patientData = 
                new PatientRegistrationResponse.PatientData(
                    savedPatient.getId(),
                    savedPatient.getEmail(),
                    maskPhoneNumber(savedPatient.getPhoneNumber()),
                    savedPatient.getEmailVerified(),
                    savedPatient.getPhoneVerified()
                );

            logger.info("Patient registration completed successfully for email: {}", 
                       maskEmail(savedPatient.getEmail()));
            
            return PatientRegistrationResponse.success(
                "Patient registration successful. Please verify your email and phone number.", 
                patientData
            );

        } catch (Exception e) {
            logger.error("Unexpected error during patient registration", e);
            return PatientRegistrationResponse.error("Registration failed. Please try again later.", "REGISTRATION_ERROR");
        }
    }

    /**
     * Create patient entity from request
     */
    private Patient createPatientFromRequest(PatientRegistrationRequest request, String hashedPassword) {
        Patient patient = new Patient();
        
        // Basic information
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setEmail(request.getEmail());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setPasswordHash(hashedPassword);
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        
        // Address information
        patient.setAddress(request.getAddress());
        
        // Optional information
        if (request.getEmergencyContact() != null) {
            patient.setEmergencyContact(request.getEmergencyContact());
        }
        
        if (request.getMedicalHistory() != null && !request.getMedicalHistory().isEmpty()) {
            patient.setMedicalHistory(request.getMedicalHistory());
        }
        
        if (request.getInsuranceInfo() != null) {
            patient.setInsuranceInfo(request.getInsuranceInfo());
        }
        
        // Default values
        patient.setEmailVerified(false);
        patient.setPhoneVerified(false);
        patient.setIsActive(true);
        patient.setFailedLoginAttempts(0);
        
        return patient;
    }

    /**
     * Sanitize request inputs
     */
    private void sanitizeRequest(PatientRegistrationRequest request) {
        if (request.getFirstName() != null) {
            request.setFirstName(validationService.sanitizeInput(request.getFirstName()));
        }
        if (request.getLastName() != null) {
            request.setLastName(validationService.sanitizeInput(request.getLastName()));
        }
        
        // Sanitize medical history
        if (request.getMedicalHistory() != null) {
            for (int i = 0; i < request.getMedicalHistory().size(); i++) {
                String condition = request.getMedicalHistory().get(i);
                if (condition != null) {
                    request.getMedicalHistory().set(i, validationService.sanitizeInput(condition));
                }
            }
        }
    }

    /**
     * Check if email is already registered
     */
    public boolean isEmailRegistered(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return patientRepository.existsByEmail(email.trim().toLowerCase());
    }

    /**
     * Check if phone number is already registered
     */
    public boolean isPhoneRegistered(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        return patientRepository.existsByPhoneNumber(phoneNumber.trim());
    }

    /**
     * Mask email for logging (HIPAA compliance)
     */
    private String maskEmail(String email) {
        if (email == null || email.length() < 3) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***@***";
        }
        
        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);
        
        if (localPart.length() <= 2) {
            return "**" + domainPart;
        }
        
        return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + domainPart;
    }

    /**
     * Mask phone number for logging (HIPAA compliance)
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "***";
        }
        
        // Show only last 4 digits
        String lastFour = phoneNumber.substring(phoneNumber.length() - 4);
        return "***-***-" + lastFour;
    }
} 