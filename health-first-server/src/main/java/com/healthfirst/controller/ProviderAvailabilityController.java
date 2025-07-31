package com.healthfirst.controller;

import com.healthfirst.dto.*;
import com.healthfirst.middleware.RateLimitingService;
import com.healthfirst.service.ProviderAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/provider")
@Tag(name = "Provider Availability", description = "APIs for provider availability management and patient slot search")
public class ProviderAvailabilityController {

    private static final Logger logger = LoggerFactory.getLogger(ProviderAvailabilityController.class);

    @Autowired
    private ProviderAvailabilityService availabilityService;

    @Autowired
    private RateLimitingService rateLimitingService;

    @Operation(summary = "Create provider availability", 
               description = "Create availability slots for a provider with optional recurrence patterns. Generates appointment slots automatically.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Availability created successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - provider authentication required"),
        @ApiResponse(responseCode = "409", description = "Conflict - overlapping availability slots"),
        @ApiResponse(responseCode = "422", description = "Unprocessable entity - business logic validation failed"),
        @ApiResponse(responseCode = "429", description = "Too many requests - rate limit exceeded"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/availability")
    public ResponseEntity<?> createAvailability(
            @Parameter(description = "Provider ID", required = true) @RequestParam UUID providerId,
            @Valid @RequestBody CreateAvailabilityRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {

        try {
            // Get client IP address for rate limiting
            String clientIp = getClientIpAddress(httpRequest);
            logger.info("Create availability request from provider: {} IP: {}", providerId, clientIp);

            // Check rate limiting
            if (!rateLimitingService.isRegistrationAllowed(clientIp)) {
                return createRateLimitResponse(clientIp);
            }

            // Check for binding errors (Bean Validation)
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
                
                logger.warn("Validation errors in create availability request: {}", errors);
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                    AvailabilityResponse.error("Validation failed: " + String.join(", ", errors), "VALIDATION_ERROR")
                );
            }

            // Process availability creation
            AvailabilityResponse response = availabilityService.createAvailability(providerId, request);
            
            if (response.isSuccess()) {
                logger.info("Availability created successfully for provider: {}", providerId);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                logger.warn("Availability creation failed for provider: {} - {}", providerId, response.getMessage());
                
                HttpStatus status = determineHttpStatusFromError(response.getErrorCode());
                return ResponseEntity.status(status).body(response);
            }

        } catch (Exception e) {
            logger.error("Unexpected error during availability creation for provider: {}", providerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AvailabilityResponse.error("Failed to create availability. Please try again later.", "CREATE_ERROR"));
        }
    }

    @Operation(summary = "Get provider availability", 
               description = "Retrieve all availability slots for a provider within a date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Availability retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - invalid parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
        @ApiResponse(responseCode = "404", description = "Provider not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{providerId}/availability")
    public ResponseEntity<?> getProviderAvailability(
            @Parameter(description = "Provider ID", required = true) @PathVariable UUID providerId,
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Include appointment slots in response") 
            @RequestParam(defaultValue = "false") Boolean includeSlots) {

        try {
            logger.info("Get availability request for provider: {} from {} to {}", providerId, startDate, endDate);

            // Validate date range
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Start date cannot be after end date", "error_code", "INVALID_DATE_RANGE")
                );
            }

            // Retrieve availability
            List<AvailabilityResponse.AvailabilityData> availabilityList = 
                availabilityService.getProviderAvailability(providerId, startDate, endDate, includeSlots);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Availability retrieved successfully");
            response.put("data", availabilityList);
            response.put("count", availabilityList.size());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for get availability: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("success", false, "message", e.getMessage(), "error_code", "PROVIDER_NOT_FOUND")
            );
        } catch (Exception e) {
            logger.error("Error retrieving availability for provider: {}", providerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Failed to retrieve availability", "error_code", "RETRIEVAL_ERROR")
            );
        }
    }

    @Operation(summary = "Update provider availability", 
               description = "Update an existing availability slot. Supports partial updates and slot regeneration.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Availability updated successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - validation errors"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - provider authentication required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - cannot modify availability with booked slots"),
        @ApiResponse(responseCode = "404", description = "Availability not found"),
        @ApiResponse(responseCode = "422", description = "Unprocessable entity - business logic validation failed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/availability/{slotId}")
    public ResponseEntity<?> updateAvailability(
            @Parameter(description = "Provider ID", required = true) @RequestParam UUID providerId,
            @Parameter(description = "Availability slot ID", required = true) @PathVariable UUID slotId,
            @Valid @RequestBody UpdateAvailabilityRequest request,
            BindingResult bindingResult) {

        try {
            logger.info("Update availability request for slot: {} provider: {}", slotId, providerId);

            // Check for binding errors
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
                
                logger.warn("Validation errors in update availability request: {}", errors);
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                    AvailabilityResponse.error("Validation failed: " + String.join(", ", errors), "VALIDATION_ERROR")
                );
            }

            // Process availability update
            AvailabilityResponse response = availabilityService.updateAvailability(providerId, slotId, request);
            
            if (response.isSuccess()) {
                logger.info("Availability updated successfully: {}", slotId);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Availability update failed for slot: {} - {}", slotId, response.getMessage());
                
                HttpStatus status = determineHttpStatusFromError(response.getErrorCode());
                return ResponseEntity.status(status).body(response);
            }

        } catch (Exception e) {
            logger.error("Unexpected error during availability update for slot: {}", slotId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AvailabilityResponse.error("Failed to update availability. Please try again later.", "UPDATE_ERROR"));
        }
    }

    @Operation(summary = "Delete provider availability", 
               description = "Delete an availability slot and associated appointment slots. Cannot delete if there are booked appointments.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Availability deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - invalid parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - provider authentication required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - cannot delete availability with booked slots"),
        @ApiResponse(responseCode = "404", description = "Availability not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/availability/{slotId}")
    public ResponseEntity<?> deleteAvailability(
            @Parameter(description = "Provider ID", required = true) @RequestParam UUID providerId,
            @Parameter(description = "Availability slot ID", required = true) @PathVariable UUID slotId,
            @Parameter(description = "Delete all recurring instances") 
            @RequestParam(defaultValue = "false") Boolean deleteRecurring,
            @Parameter(description = "Reason for deletion") 
            @RequestParam(required = false) String reason) {

        try {
            logger.info("Delete availability request for slot: {} provider: {}", slotId, providerId);

            // Process availability deletion
            AvailabilityResponse response = availabilityService.deleteAvailability(
                providerId, slotId, deleteRecurring, reason);
            
            if (response.isSuccess()) {
                logger.info("Availability deleted successfully: {}", slotId);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Availability deletion failed for slot: {} - {}", slotId, response.getMessage());
                
                HttpStatus status = determineHttpStatusFromError(response.getErrorCode());
                return ResponseEntity.status(status).body(response);
            }

        } catch (Exception e) {
            logger.error("Unexpected error during availability deletion for slot: {}", slotId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AvailabilityResponse.error("Failed to delete availability. Please try again later.", "DELETE_ERROR"));
        }
    }

    // ========== PATIENT SEARCH ENDPOINTS ==========

    @Operation(summary = "Search available appointment slots", 
               description = "Search for available appointment slots across providers with various filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - invalid search parameters"),
        @ApiResponse(responseCode = "429", description = "Too many requests - rate limit exceeded"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/availability/search")
    public ResponseEntity<?> searchAvailableSlots(
            @Valid @RequestBody AvailabilitySearchRequest searchRequest,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {

        try {
            String clientIp = getClientIpAddress(httpRequest);
            logger.info("Search available slots request from IP: {}", clientIp);

            // Check rate limiting
            if (!rateLimitingService.isRegistrationAllowed(clientIp)) {
                return createRateLimitResponse(clientIp);
            }

            // Check for validation errors
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
                
                logger.warn("Validation errors in search request: {}", errors);
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Validation failed: " + String.join(", ", errors), "error_code", "VALIDATION_ERROR")
                );
            }

            // Validate search criteria
            if (!searchRequest.isValidDateRange()) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Invalid date range", "error_code", "INVALID_DATE_RANGE")
                );
            }

            // Perform search
            List<AvailabilityResponse.AppointmentSlotData> availableSlots = 
                availabilityService.searchAvailableSlots(searchRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Search completed successfully");
            response.put("data", availableSlots);
            response.put("count", availableSlots.size());
            response.put("hasMore", availableSlots.size() >= searchRequest.getMaxResults());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid search request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", e.getMessage(), "error_code", "INVALID_REQUEST")
            );
        } catch (Exception e) {
            logger.error("Error during slot search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Search failed. Please try again later.", "error_code", "SEARCH_ERROR")
            );
        }
    }

    @Operation(summary = "Quick search available slots", 
               description = "Quick search for available slots using GET parameters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - invalid parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/availability/search")
    public ResponseEntity<?> quickSearchAvailableSlots(
            @Parameter(description = "Start date (YYYY-MM-DD)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Provider specialization") 
            @RequestParam(required = false) String specialization,
            @Parameter(description = "Appointment type") 
            @RequestParam(required = false) String appointmentType,
            @Parameter(description = "Location") 
            @RequestParam(required = false) String location,
            @Parameter(description = "Maximum number of results") 
            @RequestParam(defaultValue = "20") Integer maxResults) {

        try {
            logger.info("Quick search request: {} to {} specialization: {}", startDate, endDate, specialization);

            // Validate date range
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Start date cannot be after end date", "error_code", "INVALID_DATE_RANGE")
                );
            }

            // Build search request
            AvailabilitySearchRequest searchRequest = new AvailabilitySearchRequest(startDate, endDate);
            searchRequest.setSpecialization(specialization);
            searchRequest.setLocation(location);
            searchRequest.setMaxResults(Math.min(maxResults, 100)); // Cap at 100

            if (appointmentType != null) {
                try {
                    searchRequest.setAppointmentType(com.healthfirst.enums.AppointmentType.valueOf(appointmentType.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "Invalid appointment type: " + appointmentType, "error_code", "INVALID_APPOINTMENT_TYPE")
                    );
                }
            }

            // Perform search
            List<AvailabilityResponse.AppointmentSlotData> availableSlots = 
                availabilityService.searchAvailableSlots(searchRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Search completed successfully");
            response.put("data", availableSlots);
            response.put("count", availableSlots.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error during quick search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Search failed. Please try again later.", "error_code", "SEARCH_ERROR")
            );
        }
    }

    // ========== UTILITY METHODS ==========

    /**
     * Determine HTTP status code based on error code
     */
    private HttpStatus determineHttpStatusFromError(String errorCode) {
        return switch (errorCode) {
            case "VALIDATION_ERROR" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case "OVERLAP_ERROR" -> HttpStatus.CONFLICT;
            case "BOOKED_SLOTS_ERROR" -> HttpStatus.FORBIDDEN;
            case "INVALID_REQUEST" -> HttpStatus.BAD_REQUEST;
            case "PROVIDER_NOT_FOUND", "AVAILABILITY_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "RATE_LIMIT_EXCEEDED" -> HttpStatus.TOO_MANY_REQUESTS;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    /**
     * Create rate limit exceeded response
     */
    private ResponseEntity<?> createRateLimitResponse(String clientIp) {
        int remaining = rateLimitingService.getRemainingAttempts(clientIp);
        logger.warn("Rate limit exceeded for IP: {}, remaining attempts: {}", clientIp, remaining);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Too many requests. Please try again later.");
        errorResponse.put("error_code", "RATE_LIMIT_EXCEEDED");
        errorResponse.put("remainingAttempts", remaining);
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
    }

    /**
     * Get client IP address from request (reused pattern)
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