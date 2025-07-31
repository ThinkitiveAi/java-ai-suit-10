package com.healthfirst.service;

import com.healthfirst.dto.PatientLoginRequest;
import com.healthfirst.dto.PatientLoginResponse;
import com.healthfirst.dto.ProviderLoginRequest;
import com.healthfirst.dto.ProviderLoginResponse;
import com.healthfirst.dto.RefreshTokenRequest;
import com.healthfirst.entity.Patient;
import com.healthfirst.entity.Provider;
import com.healthfirst.entity.RefreshToken;
import com.healthfirst.enums.VerificationStatus;
import com.healthfirst.repository.PatientRepository;
import com.healthfirst.repository.ProviderRepository;
import com.healthfirst.util.JwtUtil;
import com.healthfirst.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;
    
    private final Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private final Pattern phonePattern = Pattern.compile("^\\+?[1-9]\\d{1,14}$");

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordUtil passwordUtil;

    /**
     * Authenticate provider and generate tokens
     */
    public ProviderLoginResponse loginProvider(ProviderLoginRequest request, String userAgent, String ipAddress) {
        try {
            logger.info("Login attempt for identifier: {} from IP: {}", request.getIdentifier(), ipAddress);

            // Find provider by email or phone
            Optional<Provider> providerOpt = findProviderByIdentifier(request.getIdentifier());
            
            if (providerOpt.isEmpty()) {
                logger.warn("Provider not found for identifier: {}", request.getIdentifier());
                return ProviderLoginResponse.error("Invalid credentials", "INVALID_CREDENTIALS");
            }

            Provider provider = providerOpt.get();

            // Check if account is locked
            if (provider.isAccountLocked()) {
                logger.warn("Account is locked for provider: {}", provider.getId());
                return ProviderLoginResponse.error(
                    "Account is temporarily locked due to multiple failed login attempts", 
                    "ACCOUNT_LOCKED"
                );
            }

            // Check if account is active
            if (!provider.getIsActive()) {
                logger.warn("Inactive account login attempt for provider: {}", provider.getId());
                return ProviderLoginResponse.error("Account is inactive", "ACCOUNT_INACTIVE");
            }

            // Check if account is verified
            if (provider.getVerificationStatus() != VerificationStatus.VERIFIED) {
                logger.warn("Unverified account login attempt for provider: {}", provider.getId());
                return ProviderLoginResponse.error("Account is not verified", "ACCOUNT_NOT_VERIFIED");
            }

            // Verify password
            if (!passwordUtil.verifyPassword(request.getPassword(), provider.getPasswordHash())) {
                handleFailedLogin(provider);
                logger.warn("Invalid password for provider: {}", provider.getId());
                return ProviderLoginResponse.error("Invalid credentials", "INVALID_CREDENTIALS");
            }

            // Successful login - generate tokens
            boolean rememberMe = request.getRememberMe() != null && request.getRememberMe();
            
            String accessToken = jwtUtil.generateAccessToken(provider, rememberMe);
            String refreshToken = jwtUtil.generateRefreshToken(provider, rememberMe);

            // Store refresh token
            tokenService.createRefreshToken(provider, refreshToken, rememberMe, userAgent, ipAddress);

            // Update login tracking
            provider.recordSuccessfulLogin();
            providerRepository.save(provider);

            // Create response data
            ProviderLoginResponse.ProviderData providerData = new ProviderLoginResponse.ProviderData(
                provider.getId(),
                provider.getFirstName(),
                provider.getLastName(),
                provider.getEmail(),
                provider.getSpecialization(),
                provider.getVerificationStatus(),
                provider.getIsActive()
            );

            ProviderLoginResponse.LoginData loginData = new ProviderLoginResponse.LoginData(
                accessToken,
                refreshToken,
                jwtUtil.getTokenExpirationInSeconds(rememberMe),
                providerData
            );

            logger.info("Successful login for provider: {}", provider.getId());
            return ProviderLoginResponse.success(loginData);

        } catch (Exception e) {
            logger.error("Error during login for identifier: {}", request.getIdentifier(), e);
            return ProviderLoginResponse.error("Login failed. Please try again later.", "LOGIN_ERROR");
        }
    }

    /**
     * Refresh access token using refresh token
     */
    public ProviderLoginResponse refreshToken(RefreshTokenRequest request, String userAgent, String ipAddress) {
        try {
            logger.info("Token refresh attempt from IP: {}", ipAddress);

            // Validate refresh token
            Optional<RefreshToken> tokenOpt = tokenService.validateRefreshToken(request.getRefreshToken());
            
            if (tokenOpt.isEmpty()) {
                logger.warn("Invalid refresh token provided");
                return ProviderLoginResponse.error("Invalid refresh token", "INVALID_REFRESH_TOKEN");
            }

            RefreshToken refreshTokenEntity = tokenOpt.get();
            Provider provider = refreshTokenEntity.getProvider();

            // Check if provider is still active and verified
            if (!provider.getIsActive() || provider.getVerificationStatus() != VerificationStatus.VERIFIED) {
                logger.warn("Provider account status changed, revoking tokens for provider: {}", provider.getId());
                tokenService.revokeAllTokensForProvider(provider);
                return ProviderLoginResponse.error("Account status has changed", "ACCOUNT_STATUS_CHANGED");
            }

            // Generate new tokens
            boolean rememberMe = jwtUtil.getRefreshTokenExpirationInSeconds(false) * 1000 < 
                               (refreshTokenEntity.getExpiresAt().toEpochSecond(java.time.ZoneOffset.UTC) * 1000 - System.currentTimeMillis());
            
            String newAccessToken = jwtUtil.generateAccessToken(provider, rememberMe);
            String newRefreshToken = jwtUtil.generateRefreshToken(provider, rememberMe);

            // Rotate refresh token (revoke old, create new)
            tokenService.rotateRefreshToken(refreshTokenEntity, newRefreshToken, rememberMe, userAgent, ipAddress);

            // Create response data
            ProviderLoginResponse.ProviderData providerData = new ProviderLoginResponse.ProviderData(
                provider.getId(),
                provider.getFirstName(),
                provider.getLastName(),
                provider.getEmail(),
                provider.getSpecialization(),
                provider.getVerificationStatus(),
                provider.getIsActive()
            );

            ProviderLoginResponse.LoginData loginData = new ProviderLoginResponse.LoginData(
                newAccessToken,
                newRefreshToken,
                jwtUtil.getTokenExpirationInSeconds(rememberMe),
                providerData
            );

            logger.info("Token refreshed successfully for provider: {}", provider.getId());
            return ProviderLoginResponse.success(loginData);

        } catch (Exception e) {
            logger.error("Error during token refresh", e);
            return ProviderLoginResponse.error("Token refresh failed", "REFRESH_ERROR");
        }
    }

    /**
     * Logout provider (revoke refresh token)
     */
    public boolean logoutProvider(String refreshToken) {
        try {
            tokenService.revokeRefreshToken(refreshToken);
            logger.info("Provider logged out successfully");
            return true;

        } catch (Exception e) {
            logger.error("Error during logout", e);
            return false;
        }
    }

    /**
     * Logout all sessions for provider
     */
    public boolean logoutAllSessions(String refreshToken) {
        try {
            // Validate token to get provider
            Optional<RefreshToken> tokenOpt = tokenService.validateRefreshToken(refreshToken);
            
            if (tokenOpt.isPresent()) {
                Provider provider = tokenOpt.get().getProvider();
                tokenService.revokeAllTokensForProvider(provider);
                logger.info("All sessions logged out for provider: {}", provider.getId());
                return true;
            } else {
                logger.warn("Invalid refresh token provided for logout all");
                return false;
            }

        } catch (Exception e) {
            logger.error("Error during logout all sessions", e);
            return false;
        }
    }

    /**
     * Find provider by email or phone number
     */
    private Optional<Provider> findProviderByIdentifier(String identifier) {
        if (emailPattern.matcher(identifier).matches()) {
            return providerRepository.findByEmail(identifier);
        } else if (phonePattern.matcher(identifier).matches()) {
            return providerRepository.findByPhoneNumber(identifier);
        } else {
            logger.warn("Invalid identifier format: {}", identifier);
            return Optional.empty();
        }
    }

    /**
     * Handle failed login attempt
     */
    private void handleFailedLogin(Provider provider) {
        provider.incrementFailedAttempts();
        
        if (provider.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            provider.lockAccount(LOCKOUT_DURATION_MINUTES);
            logger.warn("Account locked for provider {} due to {} failed attempts", 
                       provider.getId(), provider.getFailedLoginAttempts());
        }
        
        providerRepository.save(provider);
    }

    /**
     * Validate access token
     */
    public boolean isTokenValid(String token) {
        try {
            jwtUtil.validateToken(token);
            return !jwtUtil.isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get provider from access token
     */
    public Optional<Provider> getProviderFromToken(String token) {
        try {
            UUID providerId = jwtUtil.getProviderIdFromToken(token);
            return providerRepository.findById(providerId);
        } catch (Exception e) {
            logger.error("Error extracting provider from token", e);
            return Optional.empty();
        }
    }

    // ========== PATIENT AUTHENTICATION METHODS ==========

    /**
     * Authenticate patient and generate token
     */
    public PatientLoginResponse loginPatient(PatientLoginRequest request, String ipAddress) {
        try {
            logger.info("Patient login attempt for email: {} from IP: {}", 
                       maskEmail(request.getEmail()), ipAddress);

            // Find patient by email
            Optional<Patient> patientOpt = patientRepository.findByEmail(request.getEmail().toLowerCase().trim());
            
            if (patientOpt.isEmpty()) {
                logger.warn("Patient not found for email: {}", maskEmail(request.getEmail()));
                return PatientLoginResponse.error("Invalid credentials", "INVALID_CREDENTIALS");
            }

            Patient patient = patientOpt.get();

            // Check if account is locked
            if (patient.isAccountLocked()) {
                logger.warn("Account is locked for patient: {}", patient.getId());
                return PatientLoginResponse.error(
                    "Account is temporarily locked due to multiple failed login attempts", 
                    "ACCOUNT_LOCKED"
                );
            }

            // Check if account is active
            if (!patient.getIsActive()) {
                logger.warn("Inactive account login attempt for patient: {}", patient.getId());
                return PatientLoginResponse.error("Account is inactive", "ACCOUNT_INACTIVE");
            }

            // Verify password
            if (!passwordUtil.verifyPassword(request.getPassword(), patient.getPasswordHash())) {
                handlePatientFailedLogin(patient);
                logger.warn("Invalid password for patient: {}", patient.getId());
                return PatientLoginResponse.error("Invalid credentials", "INVALID_CREDENTIALS");
            }

            // Successful login - generate token
            String accessToken = jwtUtil.generateAccessToken(patient);

            // Update login tracking
            patient.recordSuccessfulLogin();
            patientRepository.save(patient);

            // Create response data (HIPAA-compliant subset)
            PatientLoginResponse.PatientData patientData = new PatientLoginResponse.PatientData(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getEmail(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getEmailVerified(),
                patient.getPhoneVerified(),
                patient.getIsActive()
            );

            PatientLoginResponse.LoginData loginData = new PatientLoginResponse.LoginData(
                accessToken,
                jwtUtil.getPatientTokenExpirationInSeconds(),
                patientData
            );

            logger.info("Successful login for patient: {}", patient.getId());
            return PatientLoginResponse.success(loginData);

        } catch (Exception e) {
            logger.error("Error during patient login for email: {}", 
                        maskEmail(request.getEmail()), e);
            return PatientLoginResponse.error("Login failed. Please try again later.", "LOGIN_ERROR");
        }
    }

    /**
     * Handle failed login attempt for patient
     */
    private void handlePatientFailedLogin(Patient patient) {
        patient.incrementFailedAttempts();
        
        if (patient.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            patient.lockAccount(LOCKOUT_DURATION_MINUTES);
            logger.warn("Account locked for patient {} after {} failed attempts", 
                       patient.getId(), MAX_FAILED_ATTEMPTS);
        }
        
        patientRepository.save(patient);
    }

    /**
     * Validate patient token
     */
    public boolean isPatientTokenValid(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return false;
            }

            jwtUtil.validateToken(token);
            return jwtUtil.isPatientToken(token);
        } catch (Exception e) {
            logger.debug("Invalid patient token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get patient from token
     */
    public Optional<Patient> getPatientFromToken(String token) {
        try {
            if (!isPatientTokenValid(token)) {
                return Optional.empty();
            }

            UUID patientId = jwtUtil.getPatientIdFromToken(token);
            return patientRepository.findById(patientId);
        } catch (Exception e) {
            logger.error("Error extracting patient from token", e);
            return Optional.empty();
        }
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
} 