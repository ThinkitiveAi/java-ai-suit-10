package com.healthfirst.middleware;

import com.healthfirst.entity.Patient;
import com.healthfirst.entity.Provider;
import com.healthfirst.service.AuthService;
import com.healthfirst.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class AuthenticationMiddleware extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationMiddleware.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = extractTokenFromRequest(request);
            
            if (token != null && authService.isTokenValid(token)) {
                
                // Check if it's a patient token
                if (jwtUtil.isPatientToken(token)) {
                    handlePatientAuthentication(token, request, response);
                }
                // Check if it's a provider token  
                else if (jwtUtil.isProviderToken(token)) {
                    handleProviderAuthentication(token, request, response);
                }
                else {
                    logger.warn("Unknown token type for endpoint: {}", requestPath);
                    sendUnauthorizedResponse(response, "Invalid token type");
                    return;
                }
            } else {
                logger.warn("Invalid or missing token for protected endpoint: {}", requestPath);
                sendUnauthorizedResponse(response, "Invalid or missing token");
                return;
            }

        } catch (Exception e) {
            logger.error("Error during authentication for request: {}", requestPath, e);
            sendUnauthorizedResponse(response, "Authentication error");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }
        
        return null;
    }

    /**
     * Check if the endpoint is public (doesn't require authentication)
     */
    private boolean isPublicEndpoint(String path) {
        // Remove context path if present
        if (path.startsWith("/api/v1")) {
            path = path.substring(7);
        }
        
        return path.equals("/provider/login") ||
               path.equals("/provider/register") ||
               path.equals("/provider/refresh") ||
               path.equals("/provider/specializations") ||
               path.startsWith("/provider/check-") ||
               path.equals("/patient/register") ||
               path.equals("/patient/login") ||
               path.startsWith("/patient/check-") ||
               path.equals("/actuator/health") ||
               path.startsWith("/h2-console") ||
               path.startsWith("/swagger-ui") ||
               path.equals("/swagger-ui.html") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/api-docs") ||
               path.equals("/error");
    }

    /**
     * Handle patient authentication
     */
    private void handlePatientAuthentication(String token, HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        Optional<Patient> patientOpt = authService.getPatientFromToken(token);
        
        if (patientOpt.isPresent()) {
            Patient patient = patientOpt.get();
            
            // Check if patient is still active and not locked
            if (patient.getIsActive() && !patient.isAccountLocked()) {
                
                // Create authentication object
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        patient, 
                        null, 
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_PATIENT"))
                    );
                
                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // Add patient to request attributes for easy access in controllers
                request.setAttribute("currentPatient", patient);
                
                logger.debug("Authentication successful for patient: {}", patient.getId());
            } else {
                logger.warn("Patient account status invalid for patient: {}", patient.getId());
                sendUnauthorizedResponse(response, "Account status invalid");
            }
        } else {
            logger.warn("Patient not found for valid token");
            sendUnauthorizedResponse(response, "Patient not found");
        }
    }

    /**
     * Handle provider authentication (existing logic)
     */
    private void handleProviderAuthentication(String token, HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        Optional<Provider> providerOpt = authService.getProviderFromToken(token);
        
        if (providerOpt.isPresent()) {
            Provider provider = providerOpt.get();
            
            // Check if provider is still active and verified
            if (provider.getIsActive() && 
                provider.getVerificationStatus() == com.healthfirst.enums.VerificationStatus.VERIFIED &&
                !provider.isAccountLocked()) {
                
                // Create authentication object
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        provider, 
                        null, 
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROVIDER"))
                    );
                
                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // Add provider to request attributes for easy access in controllers
                request.setAttribute("currentProvider", provider);
                
                logger.debug("Authentication successful for provider: {}", provider.getId());
            } else {
                logger.warn("Provider account status invalid for provider: {}", provider.getId());
                sendUnauthorizedResponse(response, "Account status invalid");
            }
        } else {
            logger.warn("Provider not found for valid token");
            sendUnauthorizedResponse(response, "Provider not found");
        }
    }

    /**
     * Send unauthorized response
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = String.format(
            "{\"success\": false, \"message\": \"%s\", \"error_code\": \"UNAUTHORIZED\"}", 
            message
        );
        
        response.getWriter().write(jsonResponse);
    }
} 