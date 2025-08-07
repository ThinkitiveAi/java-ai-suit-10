package com.healthfirst.controller;

import com.healthfirst.dto.CreateAvailabilityRequest;
import com.healthfirst.dto.AvailabilityResponse;
import com.healthfirst.service.ProviderService;
import com.healthfirst.service.ProviderAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/providers")
@Tag(name = "Provider Management", description = "APIs for provider selection and availability setup")
public class ProviderManagementController {

    private static final Logger logger = LoggerFactory.getLogger(ProviderManagementController.class);

    @Autowired
    private ProviderService providerService;

    @Autowired
    private ProviderAvailabilityService providerAvailabilityService;

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

    @Operation(summary = "Set provider availability", 
               description = "Set availability for a provider. If availability already exists, it will be updated (idempotent behavior).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Availability set successfully"),
        @ApiResponse(responseCode = "201", description = "Availability created successfully"),
        @ApiResponse(responseCode = "404", description = "Provider not found"),
        @ApiResponse(responseCode = "400", description = "Bad request - invalid availability request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{providerId}/availability")
    public ResponseEntity<?> setProviderAvailability(
            @PathVariable UUID providerId,
            @Valid @RequestBody CreateAvailabilityRequest request,
            BindingResult bindingResult) {

        try {
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
                
                logger.warn("Validation errors in availability request: {}", errors);
                return ResponseEntity.badRequest().body(
                    AvailabilityResponse.error("Validation failed: " + String.join(", ", errors), "VALIDATION_ERROR")
                );
            }

            // Use the existing availability service to create/update availability
            AvailabilityResponse response = providerAvailabilityService.createAvailability(providerId, request);
            
            if (response.isSuccess()) {
                logger.info("Provider availability set successfully for provider ID: {}", providerId);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Provider availability set failed for provider ID: {}, reason: {}", 
                           providerId, response.getMessage());
                
                // Determine appropriate HTTP status code
                HttpStatus status = HttpStatus.BAD_REQUEST;
                if (response.getErrorCode() != null && response.getErrorCode().contains("NOT_FOUND")) {
                    status = HttpStatus.NOT_FOUND;
                }
                
                return ResponseEntity.status(status).body(response);
            }

        } catch (Exception e) {
            logger.error("Unexpected error during provider availability set", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AvailabilityResponse.error("Availability update failed. Please try again later.", "INTERNAL_ERROR"));
        }
    }
} 