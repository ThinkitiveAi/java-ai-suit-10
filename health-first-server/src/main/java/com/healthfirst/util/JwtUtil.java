package com.healthfirst.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.healthfirst.entity.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpirationMs;

    private static final String ISSUER = "healthfirst-api";
    private static final String AUDIENCE = "healthfirst-providers";

    /**
     * Generate access token
     */
    public String generateAccessToken(Provider provider, boolean rememberMe) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
            
            long expirationTime = rememberMe ? (jwtExpirationMs * 24) : jwtExpirationMs; // 24 hours if remember_me
            Date expiryDate = new Date(System.currentTimeMillis() + expirationTime);

            return JWT.create()
                    .withIssuer(ISSUER)
                    .withAudience(AUDIENCE)
                    .withSubject(provider.getId().toString())
                    .withClaim("provider_id", provider.getId().toString())
                    .withClaim("email", provider.getEmail())
                    .withClaim("first_name", provider.getFirstName())
                    .withClaim("last_name", provider.getLastName())
                    .withClaim("specialization", provider.getSpecialization())
                    .withClaim("verification_status", provider.getVerificationStatus().toString())
                    .withClaim("is_active", provider.getIsActive())
                    .withClaim("role", "PROVIDER")
                    .withIssuedAt(new Date())
                    .withExpiresAt(expiryDate)
                    .sign(algorithm);

        } catch (JWTCreationException e) {
            logger.error("Error creating JWT token for provider {}", provider.getId(), e);
            throw new RuntimeException("Error creating JWT token", e);
        }
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(Provider provider, boolean rememberMe) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
            
            long expirationTime = rememberMe ? (refreshExpirationMs * 4) : refreshExpirationMs; // 30 days if remember_me
            Date expiryDate = new Date(System.currentTimeMillis() + expirationTime);

            return JWT.create()
                    .withIssuer(ISSUER)
                    .withAudience(AUDIENCE)
                    .withSubject(provider.getId().toString())
                    .withClaim("provider_id", provider.getId().toString())
                    .withClaim("token_type", "refresh")
                    .withClaim("jti", UUID.randomUUID().toString()) // JWT ID for uniqueness
                    .withIssuedAt(new Date())
                    .withExpiresAt(expiryDate)
                    .sign(algorithm);

        } catch (JWTCreationException e) {
            logger.error("Error creating refresh token for provider {}", provider.getId(), e);
            throw new RuntimeException("Error creating refresh token", e);
        }
    }

    /**
     * Validate and decode JWT token
     */
    public DecodedJWT validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .withAudience(AUDIENCE)
                    .build();
            
            return verifier.verify(token);

        } catch (JWTVerificationException e) {
            logger.warn("Invalid JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    /**
     * Extract provider ID from token
     */
    public UUID getProviderIdFromToken(String token) {
        DecodedJWT decodedJWT = validateToken(token);
        String providerIdStr = decodedJWT.getClaim("provider_id").asString();
        return UUID.fromString(providerIdStr);
    }

    /**
     * Extract email from token
     */
    public String getEmailFromToken(String token) {
        DecodedJWT decodedJWT = validateToken(token);
        return decodedJWT.getClaim("email").asString();
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT decodedJWT = validateToken(token);
            return decodedJWT.getExpiresAt().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Get expiration time from token
     */
    public LocalDateTime getExpirationFromToken(String token) {
        DecodedJWT decodedJWT = validateToken(token);
        return LocalDateTime.ofInstant(
            decodedJWT.getExpiresAt().toInstant(),
            ZoneId.systemDefault()
        );
    }

    /**
     * Extract JWT ID (for refresh tokens)
     */
    public String getJwtIdFromToken(String token) {
        DecodedJWT decodedJWT = validateToken(token);
        return decodedJWT.getClaim("jti").asString();
    }

    /**
     * Check if token is a refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            DecodedJWT decodedJWT = validateToken(token);
            String tokenType = decodedJWT.getClaim("token_type").asString();
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get token expiration in seconds
     */
    public long getTokenExpirationInSeconds(boolean rememberMe) {
        long expirationTime = rememberMe ? (jwtExpirationMs * 24) : jwtExpirationMs;
        return expirationTime / 1000; // Convert to seconds
    }

    /**
     * Get refresh token expiration in seconds
     */
    public long getRefreshTokenExpirationInSeconds(boolean rememberMe) {
        long expirationTime = rememberMe ? (refreshExpirationMs * 4) : refreshExpirationMs;
        return expirationTime / 1000; // Convert to seconds
    }

    /**
     * Extract all claims from token as a formatted string (for logging)
     */
    public String getTokenInfo(String token) {
        try {
            DecodedJWT decodedJWT = validateToken(token);
            return String.format("Token{providerId=%s, email=%s, issued=%s, expires=%s}",
                decodedJWT.getClaim("provider_id").asString(),
                decodedJWT.getClaim("email").asString(),
                decodedJWT.getIssuedAt(),
                decodedJWT.getExpiresAt()
            );
        } catch (Exception e) {
            return "Invalid token";
        }
    }

    // ========== PATIENT TOKEN METHODS ==========

    /**
     * Generate access token for patient (30 minutes fixed as per requirements)
     */
    public String generateAccessToken(com.healthfirst.entity.Patient patient) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
            
            // Fixed 30 minutes expiration for patients as per requirements
            long expirationTime = 30 * 60 * 1000; // 30 minutes in milliseconds
            Date expiryDate = new Date(System.currentTimeMillis() + expirationTime);

            return JWT.create()
                    .withIssuer(ISSUER)
                    .withAudience(AUDIENCE)
                    .withSubject(patient.getId().toString())
                    .withClaim("patient_id", patient.getId().toString())
                    .withClaim("email", patient.getEmail())
                    .withClaim("first_name", patient.getFirstName())
                    .withClaim("last_name", patient.getLastName())
                    .withClaim("date_of_birth", patient.getDateOfBirth().toString())
                    .withClaim("gender", patient.getGender().toString())
                    .withClaim("email_verified", patient.getEmailVerified())
                    .withClaim("phone_verified", patient.getPhoneVerified())
                    .withClaim("is_active", patient.getIsActive())
                    .withClaim("role", "PATIENT")
                    .withIssuedAt(new Date())
                    .withExpiresAt(expiryDate)
                    .sign(algorithm);

        } catch (JWTCreationException e) {
            logger.error("Error creating JWT token for patient {}", patient.getId(), e);
            throw new RuntimeException("Error creating JWT token", e);
        }
    }

    /**
     * Extract patient ID from token
     */
    public UUID getPatientIdFromToken(String token) {
        DecodedJWT decodedJWT = validateToken(token);
        String patientIdStr = decodedJWT.getClaim("patient_id").asString();
        return patientIdStr != null ? UUID.fromString(patientIdStr) : null;
    }

    /**
     * Get token expiration in seconds for patient tokens (fixed 30 minutes)
     */
    public long getPatientTokenExpirationInSeconds() {
        return 30 * 60; // 30 minutes
    }

    /**
     * Check if token belongs to a patient
     */
    public boolean isPatientToken(String token) {
        try {
            DecodedJWT decodedJWT = validateToken(token);
            String role = decodedJWT.getClaim("role").asString();
            return "PATIENT".equals(role);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if token belongs to a provider
     */
    public boolean isProviderToken(String token) {
        try {
            DecodedJWT decodedJWT = validateToken(token);
            String role = decodedJWT.getClaim("role").asString();
            return "PROVIDER".equals(role);
        } catch (Exception e) {
            return false;
        }
    }
} 