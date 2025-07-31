package com.healthfirst.service;

import com.healthfirst.dto.ProviderRegistrationRequest;
import com.healthfirst.dto.ProviderRegistrationResponse;
import com.healthfirst.entity.Provider;
import com.healthfirst.repository.ProviderRepository;
import com.healthfirst.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProviderService {

    private static final Logger logger = LoggerFactory.getLogger(ProviderService.class);

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordUtil passwordUtil;

    /**
     * Register a new provider
     */
    public ProviderRegistrationResponse registerProvider(ProviderRegistrationRequest request) {
        try {
            logger.info("Starting provider registration for email: {}", request.getEmail());

            // Validate the request
            List<String> validationErrors = validationService.validateProviderRegistration(request);
            if (!validationErrors.isEmpty()) {
                logger.warn("Validation failed for provider registration: {}", validationErrors);
                return ProviderRegistrationResponse.error("Validation failed: " + String.join(", ", validationErrors));
            }

            // Sanitize inputs
            sanitizeRequest(request);

            // Hash the password
            String hashedPassword = passwordUtil.hashPassword(request.getPassword());

            // Create provider entity
            Provider provider = createProviderFromRequest(request, hashedPassword);

            // Save provider to database
            Provider savedProvider = providerRepository.save(provider);
            logger.info("Provider saved successfully with ID: {}", savedProvider.getId());

            // Send verification email (async)
            try {
                emailService.sendVerificationEmail(
                    savedProvider.getEmail(),
                    savedProvider.getFirstName(),
                    savedProvider.getId()
                );
            } catch (Exception e) {
                logger.error("Failed to send verification email, but registration was successful", e);
                // Don't fail the registration if email fails
            }

            // Create response
            ProviderRegistrationResponse.ProviderData providerData = 
                new ProviderRegistrationResponse.ProviderData(
                    savedProvider.getId(),
                    savedProvider.getEmail(),
                    savedProvider.getVerificationStatus()
                );

            logger.info("Provider registration completed successfully for email: {}", request.getEmail());
            return ProviderRegistrationResponse.success(providerData);

        } catch (Exception e) {
            logger.error("Error during provider registration for email: {}", request.getEmail(), e);
            return ProviderRegistrationResponse.error("Registration failed. Please try again later.");
        }
    }

    /**
     * Get list of valid specializations
     */
    public List<String> getValidSpecializations() {
        return validationService.getValidSpecializations();
    }

    /**
     * Find provider by email
     */
    public Provider findByEmail(String email) {
        return providerRepository.findByEmail(email).orElse(null);
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        return providerRepository.existsByEmail(email);
    }

    /**
     * Check if phone number exists
     */
    public boolean phoneExists(String phoneNumber) {
        return providerRepository.existsByPhoneNumber(phoneNumber);
    }

    /**
     * Check if license number exists
     */
    public boolean licenseExists(String licenseNumber) {
        return providerRepository.existsByLicenseNumber(licenseNumber);
    }

    private void sanitizeRequest(ProviderRegistrationRequest request) {
        request.setFirstName(validationService.sanitizeInput(request.getFirstName()));
        request.setLastName(validationService.sanitizeInput(request.getLastName()));
        request.setEmail(validationService.sanitizeInput(request.getEmail()));
        request.setPhoneNumber(validationService.sanitizeInput(request.getPhoneNumber()));
        request.setSpecialization(validationService.sanitizeInput(request.getSpecialization()));
        request.setLicenseNumber(validationService.sanitizeInput(request.getLicenseNumber()));
        
        if (request.getClinicAddress() != null) {
            request.getClinicAddress().setStreet(validationService.sanitizeInput(request.getClinicAddress().getStreet()));
            request.getClinicAddress().setCity(validationService.sanitizeInput(request.getClinicAddress().getCity()));
            request.getClinicAddress().setState(validationService.sanitizeInput(request.getClinicAddress().getState()));
            request.getClinicAddress().setZip(validationService.sanitizeInput(request.getClinicAddress().getZip()));
        }
    }

    private Provider createProviderFromRequest(ProviderRegistrationRequest request, String hashedPassword) {
        Provider provider = new Provider();
        
        provider.setFirstName(request.getFirstName());
        provider.setLastName(request.getLastName());
        provider.setEmail(request.getEmail());
        provider.setPhoneNumber(request.getPhoneNumber());
        provider.setPasswordHash(hashedPassword);
        provider.setSpecialization(request.getSpecialization());
        provider.setLicenseNumber(request.getLicenseNumber());
        provider.setYearsOfExperience(request.getYearsOfExperience());
        provider.setClinicAddress(request.getClinicAddress());
        
        // Default values are set in the entity
        
        return provider;
    }
} 