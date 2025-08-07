package com.healthfirst.controller;

import com.healthfirst.dto.ProviderRegistrationRequest;
import com.healthfirst.dto.ProviderRegistrationResponse;
import com.healthfirst.middleware.RateLimitingService;
import com.healthfirst.service.ProviderService;
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
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/provider")
@Tag(name = "Provider Registration", description = "APIs for healthcare provider registration and management")
public class ProviderController {

    private static final Logger logger = LoggerFactory.getLogger(ProviderController.class);

    @Autowired
    private ProviderService providerService;

    @Autowired
    private RateLimitingService rateLimitingService;

    @Operation(summary = "Register new healthcare provider", 
               description = "Register a new healthcare provider with validation and email verification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Provider registered successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
        @ApiResponse(responseCode = "409", description = "Conflict - email/phone/license already exists"),
        @ApiResponse(responseCode = "429", description = "Too many requests - rate limit exceeded"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerProvider(
            @Valid @RequestBody ProviderRegistrationRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {

        try {
            // Get client IP address
            String clientIp = getClientIpAddress(httpRequest);
            logger.info("Provider registration attempt from IP: {}", clientIp);

            // Check rate limiting
            if (!rateLimitingService.isRegistrationAllowed(clientIp)) {
                int remaining = rateLimitingService.getRemainingAttempts(clientIp);
                logger.warn("Rate limit exceeded for IP: {}, remaining attempts: {}", clientIp, remaining);
                
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Too many registration attempts. Please try again later.");
                errorResponse.put("remainingAttempts", remaining);
                
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
            }

            // Check for binding errors
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
                
                logger.warn("Validation errors in registration request: {}", errors);
                return ResponseEntity.badRequest().body(
                    ProviderRegistrationResponse.error("Validation failed: " + String.join(", ", errors))
                );
            }

            // Process registration
            ProviderRegistrationResponse response = providerService.registerProvider(request);
            
            if (response.isSuccess()) {
                logger.info("Provider registration successful for email: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                logger.warn("Provider registration failed for email: {}, reason: {}", 
                           request.getEmail(), response.getMessage());
                
                // Determine appropriate HTTP status code
                HttpStatus status = HttpStatus.BAD_REQUEST;
                if (response.getMessage().contains("already registered") || 
                    response.getMessage().contains("already exists")) {
                    status = HttpStatus.CONFLICT;
                }
                
                return ResponseEntity.status(status).body(response);
            }

        } catch (Exception e) {
            logger.error("Unexpected error during provider registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ProviderRegistrationResponse.error("Registration failed. Please try again later."));
        }
    }

    @Operation(summary = "Get valid specializations", 
               description = "Retrieve list of valid medical specializations for provider registration")
    @ApiResponse(responseCode = "200", description = "List of valid specializations retrieved successfully")
    @GetMapping("/specializations")
    public ResponseEntity<Map<String, Object>> getValidSpecializations() {
        try {
            List<String> specializations = providerService.getValidSpecializations();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", specializations);
            response.put("count", specializations.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving specializations", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve specializations");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Check email availability", 
               description = "Check if an email address is available for registration")
    @ApiResponse(responseCode = "200", description = "Email availability checked successfully")
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailAvailability(@RequestParam String email) {
        try {
            boolean exists = providerService.emailExists(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("available", !exists);
            response.put("email", email);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error checking email availability", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to check email availability");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Check phone availability", 
               description = "Check if a phone number is available for registration")
    @ApiResponse(responseCode = "200", description = "Phone availability checked successfully")
    @GetMapping("/check-phone")
    public ResponseEntity<Map<String, Object>> checkPhoneAvailability(@RequestParam String phoneNumber) {
        try {
            boolean exists = providerService.phoneExists(phoneNumber);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("available", !exists);
            response.put("phoneNumber", phoneNumber);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error checking phone availability", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to check phone availability");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Check license availability", 
               description = "Check if a license number is available for registration")
    @ApiResponse(responseCode = "200", description = "License availability checked successfully")
    @GetMapping("/check-license")
    public ResponseEntity<Map<String, Object>> checkLicenseAvailability(@RequestParam String licenseNumber) {
        try {
            boolean exists = providerService.licenseExists(licenseNumber);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("available", !exists);
            response.put("licenseNumber", licenseNumber);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error checking license availability", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to check license availability");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Get all providers", 
               description = "Retrieve list of all active and verified providers for selection")
    @ApiResponse(responseCode = "200", description = "Providers retrieved successfully")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProviders() {
        try {
            List<Map<String, Object>> providers = providerService.getAllActiveProviders();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Providers retrieved successfully");
            response.put("data", providers);
            response.put("count", providers.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving providers", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve providers");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get client IP address from request
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