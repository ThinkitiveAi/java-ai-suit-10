package com.healthfirst.controller;

import com.healthfirst.dto.ProviderLoginRequest;
import com.healthfirst.dto.ProviderLoginResponse;
import com.healthfirst.dto.RefreshTokenRequest;
import com.healthfirst.dto.UnifiedLoginRequest;
import com.healthfirst.dto.UnifiedLoginResponse;
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
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Unified APIs for user authentication and token management")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private RateLimitingService rateLimitingService;

    @Operation(summary = "Unified login", 
               description = "Authenticate users (patients and providers) with auto-detection of user type based on identifier")
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
    public ResponseEntity<?> unifiedLogin(
            @Valid @RequestBody UnifiedLoginRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {

        try {
            // Get client IP address
            String clientIp = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            logger.info("Unified login attempt from IP: {}", clientIp);

            // Check rate limiting
            if (!rateLimitingService.isRegistrationAllowed(clientIp)) {
                int remaining = rateLimitingService.getRemainingAttempts(clientIp);
                logger.warn("Rate limit exceeded for IP: {}, remaining attempts: {}", clientIp, remaining);
                
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Too many login attempts. Please try again later.");
                errorResponse.put("error_code", "RATE_LIMIT_EXCEEDED");
                errorResponse.put("remainingAttempts", remaining);
                
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
            }

            // Check for binding errors (Bean Validation)
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
                
                logger.warn("Validation errors in unified login request: {}", errors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    UnifiedLoginResponse.error("Validation failed: " + String.join(", ", errors), "VALIDATION_ERROR")
                );
            }

            // Process unified login
            UnifiedLoginResponse response = authService.unifiedLogin(request, userAgent, clientIp);
            
            if (response.isSuccess()) {
                logger.info("Unified login successful for user type: {}", 
                           response.getData() != null ? response.getData().getUserType() : "unknown");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Unified login failed: {}", response.getMessage());
                
                // Determine appropriate HTTP status code based on error
                HttpStatus status = determineHttpStatusFromError(response.getErrorCode());
                return ResponseEntity.status(status).body(response);
            }

        } catch (Exception e) {
            logger.error("Unexpected error during unified login from IP: {}", 
                        getClientIpAddress(httpRequest), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(UnifiedLoginResponse.error("Login failed. Please try again later.", "LOGIN_ERROR"));
        }
    }

    @Operation(summary = "Provider login (backward compatible)", 
               description = "Legacy provider login endpoint for backward compatibility. Use /auth/login for new implementations.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "403", description = "Account not verified or inactive"),
        @ApiResponse(responseCode = "423", description = "Account locked due to failed attempts"),
        @ApiResponse(responseCode = "429", description = "Too many requests - rate limit exceeded"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/provider/login")
    public ResponseEntity<?> providerLoginLegacy(
            @Valid @RequestBody ProviderLoginRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {

        // Convert provider request to unified request
        UnifiedLoginRequest unifiedRequest = new UnifiedLoginRequest(
            request.getIdentifier(),
            request.getPassword(),
            request.getRememberMe(),
            "provider" // Explicitly set user type for provider login
        );

        // Delegate to unified login
        ResponseEntity<?> unifiedResponse = unifiedLogin(unifiedRequest, bindingResult, httpRequest);
        
        // Convert unified response back to provider response for backward compatibility
        if (unifiedResponse.getBody() instanceof UnifiedLoginResponse) {
            UnifiedLoginResponse unified = (UnifiedLoginResponse) unifiedResponse.getBody();
            return ResponseEntity.status(unifiedResponse.getStatusCode())
                .body(convertToProviderResponse(unified));
        }

        return unifiedResponse;
    }

    /**
     * Convert unified response to provider response for backward compatibility
     */
    private ProviderLoginResponse convertToProviderResponse(UnifiedLoginResponse unified) {
        if (unified.isSuccess() && unified.getData() != null) {
            UnifiedLoginResponse.LoginData loginData = unified.getData();
            
            if (loginData.getUser() instanceof UnifiedLoginResponse.ProviderData) {
                UnifiedLoginResponse.ProviderData providerData = (UnifiedLoginResponse.ProviderData) loginData.getUser();
                
                ProviderLoginResponse.ProviderData legacyProviderData = new ProviderLoginResponse.ProviderData(
                    providerData.getId(),
                    providerData.getFirstName(),
                    providerData.getLastName(),
                    providerData.getEmail(),
                    providerData.getSpecialization(),
                    providerData.getVerificationStatus(),
                    providerData.isActive()
                );

                ProviderLoginResponse.LoginData legacyLoginData = new ProviderLoginResponse.LoginData(
                    loginData.getAccessToken(),
                    loginData.getRefreshToken(),
                    loginData.getExpiresIn(),
                    legacyProviderData
                );

                return ProviderLoginResponse.success(legacyLoginData);
            }
        }
        
        return ProviderLoginResponse.error(unified.getMessage(), unified.getErrorCode());
    }

    @Operation(summary = "Refresh access token", 
               description = "Generate new access token using valid refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
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
            // Get client information
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
                
                HttpStatus status = switch (response.getErrorCode()) {
                    case "INVALID_REFRESH_TOKEN" -> HttpStatus.UNAUTHORIZED;
                    case "ACCOUNT_STATUS_CHANGED" -> HttpStatus.FORBIDDEN;
                    default -> HttpStatus.INTERNAL_SERVER_ERROR;
                };
                
                return ResponseEntity.status(status).body(response);
            }

        } catch (Exception e) {
            logger.error("Unexpected error during token refresh", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ProviderLoginResponse.error("Token refresh failed", "REFRESH_ERROR"));
        }
    }

    @Operation(summary = "Logout provider", 
               description = "Revoke refresh token and logout current session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Refresh token is required")
                );
            }

            boolean success = authService.logoutProvider(refreshToken);
            
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Logout successful"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("success", false, "message", "Logout failed")
                );
            }

        } catch (Exception e) {
            logger.error("Error during logout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Logout failed")
            );
        }
    }

    @Operation(summary = "Logout all sessions", 
               description = "Revoke all refresh tokens for the provider")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "All sessions logged out"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Refresh token is required")
                );
            }

            boolean success = authService.logoutAllSessions(refreshToken);
            
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "All sessions logged out"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("success", false, "message", "Logout all failed")
                );
            }

        } catch (Exception e) {
            logger.error("Error during logout all", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Logout all failed")
            );
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