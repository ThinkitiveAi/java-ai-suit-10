package com.healthfirst.controller;

import com.healthfirst.dto.ProviderLoginRequest;
import com.healthfirst.dto.ProviderLoginResponse;
import com.healthfirst.dto.RefreshTokenRequest;
import com.healthfirst.middleware.RateLimitingService;
import com.healthfirst.service.AuthService;
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
@RequestMapping("/provider")
@Tag(name = "Provider Authentication", description = "APIs for provider login, logout, and token management")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private RateLimitingService rateLimitingService;

    @Operation(summary = "Provider login", 
               description = "Authenticate provider with email/phone and password, returns JWT tokens")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "403", description = "Account not verified or inactive"),
        @ApiResponse(responseCode = "423", description = "Account locked due to failed attempts"),
        @ApiResponse(responseCode = "429", description = "Too many requests - rate limit exceeded"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public ResponseEntity<?> loginProvider(
            @Valid @RequestBody ProviderLoginRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {

        try {
            // Get client IP address
            String clientIp = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            logger.info("Login attempt for identifier: {} from IP: {}", request.getIdentifier(), clientIp);

            // Check rate limiting for login attempts
            if (!rateLimitingService.isRegistrationAllowed(clientIp)) {
                int remaining = rateLimitingService.getRemainingAttempts(clientIp);
                logger.warn("Rate limit exceeded for login attempts from IP: {}", clientIp);
                
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Too many login attempts. Please try again later.");
                errorResponse.put("error_code", "RATE_LIMIT_EXCEEDED");
                errorResponse.put("remainingAttempts", remaining);
                
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
            }

            // Check for validation errors
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
                
                logger.warn("Validation errors in login request: {}", errors);
                return ResponseEntity.badRequest().body(
                    ProviderLoginResponse.error("Validation failed: " + String.join(", ", errors), "VALIDATION_ERROR")
                );
            }

            // Attempt login
            ProviderLoginResponse response = authService.loginProvider(request, userAgent, clientIp);
            
            if (response.isSuccess()) {
                logger.info("Login successful for identifier: {}", request.getIdentifier());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Login failed for identifier: {}, reason: {}", 
                           request.getIdentifier(), response.getMessage());
                
                // Determine appropriate HTTP status code based on error
                HttpStatus status = determineHttpStatusFromError(response.getErrorCode());
                return ResponseEntity.status(status).body(response);
            }

        } catch (Exception e) {
            logger.error("Unexpected error during login for identifier: {}", request.getIdentifier(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ProviderLoginResponse.error("Login failed. Please try again later.", "LOGIN_ERROR"));
        }
    }

    @Operation(summary = "Refresh access token", 
               description = "Generate new access token using refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
        @ApiResponse(responseCode = "401", description = "Invalid refresh token"),
        @ApiResponse(responseCode = "403", description = "Account status changed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {

        try {
            String clientIp = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            logger.info("Token refresh attempt from IP: {}", clientIp);

            // Check for validation errors
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
                
                logger.warn("Validation errors in refresh token request: {}", errors);
                return ResponseEntity.badRequest().body(
                    ProviderLoginResponse.error("Validation failed: " + String.join(", ", errors), "VALIDATION_ERROR")
                );
            }

            // Attempt token refresh
            ProviderLoginResponse response = authService.refreshToken(request, userAgent, clientIp);
            
            if (response.isSuccess()) {
                logger.info("Token refresh successful");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Token refresh failed: {}", response.getMessage());
                
                HttpStatus status = determineHttpStatusFromError(response.getErrorCode());
                return ResponseEntity.status(status).body(response);
            }

        } catch (Exception e) {
            logger.error("Unexpected error during token refresh", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ProviderLoginResponse.error("Token refresh failed", "REFRESH_ERROR"));
        }
    }

    @Operation(summary = "Logout provider", 
               description = "Revoke refresh token and logout provider")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logoutProvider(
            @Valid @RequestBody RefreshTokenRequest request,
            BindingResult bindingResult) {

        try {
            logger.info("Logout attempt");

            Map<String, Object> response = new HashMap<>();

            // Check for validation errors
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
                
                logger.warn("Validation errors in logout request: {}", errors);
                response.put("success", false);
                response.put("message", "Validation failed: " + String.join(", ", errors));
                response.put("error_code", "VALIDATION_ERROR");
                
                return ResponseEntity.badRequest().body(response);
            }

            // Attempt logout
            boolean success = authService.logoutProvider(request.getRefreshToken());
            
            if (success) {
                logger.info("Logout successful");
                response.put("success", true);
                response.put("message", "Logout successful");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Logout failed");
                response.put("success", false);
                response.put("message", "Logout failed");
                response.put("error_code", "LOGOUT_ERROR");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            logger.error("Unexpected error during logout", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Logout failed. Please try again later.");
            errorResponse.put("error_code", "LOGOUT_ERROR");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Logout all sessions", 
               description = "Revoke all refresh tokens for the provider")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "All sessions logged out successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
        @ApiResponse(responseCode = "401", description = "Invalid refresh token"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/logout-all")
    public ResponseEntity<Map<String, Object>> logoutAllSessions(
            @Valid @RequestBody RefreshTokenRequest request,
            BindingResult bindingResult) {

        try {
            logger.info("Logout all sessions attempt");

            Map<String, Object> response = new HashMap<>();

            // Check for validation errors
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
                
                logger.warn("Validation errors in logout all request: {}", errors);
                response.put("success", false);
                response.put("message", "Validation failed: " + String.join(", ", errors));
                response.put("error_code", "VALIDATION_ERROR");
                
                return ResponseEntity.badRequest().body(response);
            }

            // Attempt logout all
            boolean success = authService.logoutAllSessions(request.getRefreshToken());
            
            if (success) {
                logger.info("Logout all sessions successful");
                response.put("success", true);
                response.put("message", "All sessions logged out successfully");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Logout all sessions failed");
                response.put("success", false);
                response.put("message", "Invalid refresh token");
                response.put("error_code", "INVALID_REFRESH_TOKEN");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

        } catch (Exception e) {
            logger.error("Unexpected error during logout all sessions", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Logout all failed. Please try again later.");
            errorResponse.put("error_code", "LOGOUT_ALL_ERROR");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Determine HTTP status code based on error code
     */
    private HttpStatus determineHttpStatusFromError(String errorCode) {
        return switch (errorCode) {
            case "INVALID_CREDENTIALS", "INVALID_REFRESH_TOKEN" -> HttpStatus.UNAUTHORIZED;
            case "ACCOUNT_LOCKED" -> HttpStatus.LOCKED;
            case "ACCOUNT_INACTIVE", "ACCOUNT_NOT_VERIFIED", "ACCOUNT_STATUS_CHANGED" -> HttpStatus.FORBIDDEN;
            case "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
            case "RATE_LIMIT_EXCEEDED" -> HttpStatus.TOO_MANY_REQUESTS;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
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