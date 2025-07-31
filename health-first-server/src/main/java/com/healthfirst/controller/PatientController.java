package com.healthfirst.controller;

import com.healthfirst.dto.PatientRegistrationRequest;
import com.healthfirst.dto.PatientRegistrationResponse;
import com.healthfirst.middleware.RateLimitingService;
import com.healthfirst.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/patient")
@Tag(name = "Patient Management", description = "APIs for patient registration and management with HIPAA compliance")
public class PatientController {

    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    @Autowired
    private PatientService patientService;

    @Autowired
    private RateLimitingService rateLimitingService;

    @Operation(summary = "Register new patient", 
               description = "Register a new patient with comprehensive validation, HIPAA-compliant data handling, and age verification (13+)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Patient registered successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
        @ApiResponse(responseCode = "409", description = "Conflict - email/phone already exists"),
        @ApiResponse(responseCode = "422", description = "Unprocessable entity - business logic validation failed"),
        @ApiResponse(responseCode = "429", description = "Too many requests - rate limit exceeded"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerPatient(
            @Valid @RequestBody PatientRegistrationRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {

        try {
            // Get client IP address for rate limiting
            String clientIp = getClientIpAddress(httpRequest);
            logger.info("Patient registration attempt from IP: {}", clientIp);

            // Check rate limiting
            if (!rateLimitingService.isRegistrationAllowed(clientIp)) {
                int remaining = rateLimitingService.getRemainingAttempts(clientIp);
                logger.warn("Rate limit exceeded for IP: {}, remaining attempts: {}", clientIp, remaining);
                
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Too many registration attempts. Please try again later.");
                errorResponse.put("error_code", "RATE_LIMIT_EXCEEDED");
                errorResponse.put("remainingAttempts", remaining);
                
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
            }

            // Check for binding errors (Bean Validation)
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
                
                logger.warn("Validation errors in patient registration request: {}", errors);
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                    PatientRegistrationResponse.error("Validation failed: " + String.join(", ", errors), "VALIDATION_ERROR")
                );
            }

            // Process registration
            PatientRegistrationResponse response = patientService.registerPatient(request);
            
            if (response.isSuccess()) {
                logger.info("Patient registration successful for patient ID: {}", 
                           response.getData() != null ? response.getData().getId() : "unknown");
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                logger.warn("Patient registration failed: {}", response.getMessage());
                
                // Determine appropriate HTTP status code based on error
                HttpStatus status = determineHttpStatusFromError(response.getErrorCode());
                return ResponseEntity.status(status).body(response);
            }

        } catch (Exception e) {
            logger.error("Unexpected error during patient registration from IP: {}", 
                        getClientIpAddress(httpRequest), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PatientRegistrationResponse.error("Registration failed. Please try again later.", "REGISTRATION_ERROR"));
        }
    }

    @Operation(summary = "Check email availability", 
               description = "Check if an email address is already registered")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email availability checked"),
        @ApiResponse(responseCode = "400", description = "Bad request - invalid email format"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailAvailability(@RequestParam String email) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email is required");
                response.put("available", false);
                return ResponseEntity.badRequest().body(response);
            }

            boolean isRegistered = patientService.isEmailRegistered(email);
            
            response.put("success", true);
            response.put("available", !isRegistered);
            response.put("message", isRegistered ? "Email already registered" : "Email available");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error checking email availability", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Unable to check email availability");
            errorResponse.put("available", false);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Check phone availability", 
               description = "Check if a phone number is already registered")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phone availability checked"),
        @ApiResponse(responseCode = "400", description = "Bad request - invalid phone format"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/check-phone")
    public ResponseEntity<Map<String, Object>> checkPhoneAvailability(@RequestParam String phoneNumber) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Phone number is required");
                response.put("available", false);
                return ResponseEntity.badRequest().body(response);
            }

            boolean isRegistered = patientService.isPhoneRegistered(phoneNumber);
            
            response.put("success", true);
            response.put("available", !isRegistered);
            response.put("message", isRegistered ? "Phone number already registered" : "Phone number available");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error checking phone availability", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Unable to check phone availability");
            errorResponse.put("available", false);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Determine HTTP status code based on error code
     */
    private HttpStatus determineHttpStatusFromError(String errorCode) {
        return switch (errorCode) {
            case "VALIDATION_ERROR" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case "EMAIL_ALREADY_EXISTS", "PHONE_ALREADY_EXISTS" -> HttpStatus.CONFLICT;
            case "RATE_LIMIT_EXCEEDED" -> HttpStatus.TOO_MANY_REQUESTS;
            case "AGE_RESTRICTION" -> HttpStatus.UNPROCESSABLE_ENTITY;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    /**
     * Get client IP address from request (reused from provider pattern)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
} 