package com.healthfirst.controller;

import com.healthfirst.dto.AvailabilityResponse;
import com.healthfirst.dto.AvailabilitySearchRequest;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/availability")
@Tag(name = "Availability Search", description = "Public APIs for patients to search available appointment slots")
public class AvailabilitySearchController {

    private static final Logger logger = LoggerFactory.getLogger(AvailabilitySearchController.class);

    @Autowired
    private ProviderAvailabilityService availabilityService;

    @Autowired
    private RateLimitingService rateLimitingService;

    @Operation(summary = "Search available appointment slots", 
               description = "Public endpoint for patients to search available appointment slots with comprehensive filtering options")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - invalid search parameters"),
        @ApiResponse(responseCode = "429", description = "Too many requests - rate limit exceeded"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public ResponseEntity<?> searchAvailableSlots(
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Provider specialization filter") 
            @RequestParam(required = false) String specialization,
            @Parameter(description = "Appointment type filter") 
            @RequestParam(required = false) String appointmentType,
            @Parameter(description = "Location filter") 
            @RequestParam(required = false) String location,
            @Parameter(description = "Minimum price filter") 
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price filter") 
            @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Insurance provider filter") 
            @RequestParam(required = false) String insurance,
            @Parameter(description = "Maximum number of results (max 100)") 
            @RequestParam(defaultValue = "50") Integer maxResults,
            @Parameter(description = "Sort by: startTime, price, distance") 
            @RequestParam(defaultValue = "startTime") String sortBy,
            @Parameter(description = "Sort ascending (true) or descending (false)") 
            @RequestParam(defaultValue = "true") Boolean ascending,
            HttpServletRequest httpRequest) {

        try {
            String clientIp = getClientIpAddress(httpRequest);
            logger.info("Availability search request from IP: {} for {} to {}", clientIp, startDate, endDate);

            // Check rate limiting
            if (!rateLimitingService.isRegistrationAllowed(clientIp)) {
                return createRateLimitResponse(clientIp);
            }

            // Validate date range
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Start date cannot be after end date", "error_code", "INVALID_DATE_RANGE")
                );
            }

            // Validate date range is not too far in the future (limit to 1 year)
            if (startDate.isAfter(LocalDate.now().plusYears(1))) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Start date cannot be more than 1 year in the future", "error_code", "DATE_TOO_FAR")
                );
            }

            // Validate price range
            if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Minimum price cannot be greater than maximum price", "error_code", "INVALID_PRICE_RANGE")
                );
            }

            // Build search request
            AvailabilitySearchRequest searchRequest = new AvailabilitySearchRequest(startDate, endDate);
            searchRequest.setSpecialization(specialization);
            searchRequest.setLocation(location);
            searchRequest.setMinPrice(minPrice);
            searchRequest.setMaxPrice(maxPrice);
            searchRequest.setMaxResults(Math.min(maxResults, 100)); // Cap at 100
            searchRequest.setSortBy(sortBy);
            searchRequest.setAscending(ascending);

            // Add insurance filter (future enhancement)
            if (insurance != null) {
                searchRequest.setAcceptedInsurance(List.of(insurance));
            }

            // Parse appointment type
            if (appointmentType != null) {
                try {
                    searchRequest.setAppointmentType(
                        com.healthfirst.enums.AppointmentType.valueOf(appointmentType.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "Invalid appointment type: " + appointmentType, 
                               "error_code", "INVALID_APPOINTMENT_TYPE",
                               "validTypes", Arrays.stream(com.healthfirst.enums.AppointmentType.values())
                                   .map(Enum::name).collect(Collectors.toList()))
                    );
                }
            }

            // Perform search
            List<AvailabilityResponse.AppointmentSlotData> availableSlots = 
                availabilityService.searchAvailableSlots(searchRequest);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Search completed successfully");
            response.put("data", availableSlots);
            response.put("count", availableSlots.size());
            response.put("hasMore", availableSlots.size() >= searchRequest.getMaxResults());
            response.put("searchCriteria", buildSearchCriteriaResponse(searchRequest));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid search request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", e.getMessage(), "error_code", "INVALID_REQUEST")
            );
        } catch (Exception e) {
            logger.error("Error during availability search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Search failed. Please try again later.", "error_code", "SEARCH_ERROR")
            );
        }
    }

    @Operation(summary = "Advanced search with POST body", 
               description = "Advanced search for available slots using POST request with detailed filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - invalid search parameters"),
        @ApiResponse(responseCode = "429", description = "Too many requests - rate limit exceeded"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/search")
    public ResponseEntity<?> advancedSearchAvailableSlots(
            @Valid @RequestBody AvailabilitySearchRequest searchRequest,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {

        try {
            String clientIp = getClientIpAddress(httpRequest);
            logger.info("Advanced availability search request from IP: {}", clientIp);

            // Check rate limiting
            if (!rateLimitingService.isRegistrationAllowed(clientIp)) {
                return createRateLimitResponse(clientIp);
            }

            // Check for validation errors
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
                
                logger.warn("Validation errors in advanced search request: {}", errors);
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Validation failed: " + String.join(", ", errors), 
                           "error_code", "VALIDATION_ERROR", "errors", errors)
                );
            }

            // Validate search criteria
            if (!searchRequest.isValidDateRange()) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Invalid date range", "error_code", "INVALID_DATE_RANGE")
                );
            }

            if (!searchRequest.isValidTimeRange()) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Invalid time range", "error_code", "INVALID_TIME_RANGE")
                );
            }

            if (!searchRequest.isValidPriceRange()) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Invalid price range", "error_code", "INVALID_PRICE_RANGE")
                );
            }

            // Cap max results
            if (searchRequest.getMaxResults() > 100) {
                searchRequest.setMaxResults(100);
            }

            // Perform search
            List<AvailabilityResponse.AppointmentSlotData> availableSlots = 
                availabilityService.searchAvailableSlots(searchRequest);

            // Build enhanced response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Advanced search completed successfully");
            response.put("data", availableSlots);
            response.put("count", availableSlots.size());
            response.put("hasMore", availableSlots.size() >= searchRequest.getMaxResults());
            response.put("searchCriteria", buildSearchCriteriaResponse(searchRequest));
            response.put("filters", buildAvailableFilters(availableSlots));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid advanced search request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", e.getMessage(), "error_code", "INVALID_REQUEST")
            );
        } catch (Exception e) {
            logger.error("Error during advanced availability search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Advanced search failed. Please try again later.", "error_code", "SEARCH_ERROR")
            );
        }
    }

    @Operation(summary = "Get available appointment types", 
               description = "Get list of available appointment types for filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Appointment types retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/appointment-types")
    public ResponseEntity<?> getAvailableAppointmentTypes() {
        try {
            List<Map<String, String>> appointmentTypes = Arrays.stream(com.healthfirst.enums.AppointmentType.values())
                .map(type -> Map.of(
                    "value", type.name(),
                    "label", type.getDisplayName()
                ))
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Appointment types retrieved successfully");
            response.put("data", appointmentTypes);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving appointment types", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Failed to retrieve appointment types", "error_code", "RETRIEVAL_ERROR")
            );
        }
    }

    // ========== UTILITY METHODS ==========

    private Map<String, Object> buildSearchCriteriaResponse(AvailabilitySearchRequest request) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("startDate", request.getStartDate());
        criteria.put("endDate", request.getEndDate());
        criteria.put("specialization", request.getSpecialization());
        criteria.put("appointmentType", request.getAppointmentType());
        criteria.put("location", request.getLocation());
        criteria.put("minPrice", request.getMinPrice());
        criteria.put("maxPrice", request.getMaxPrice());
        criteria.put("maxResults", request.getMaxResults());
        criteria.put("sortBy", request.getSortBy());
        criteria.put("ascending", request.getAscending());
        return criteria;
    }

    private Map<String, Object> buildAvailableFilters(List<AvailabilityResponse.AppointmentSlotData> slots) {
        Map<String, Object> filters = new HashMap<>();
        
        // Extract unique specializations
        Set<String> specializations = slots.stream()
            .map(slot -> "General") // This would need to be populated from provider data
            .collect(Collectors.toSet());
        
        // Extract unique appointment types
        Set<String> appointmentTypes = slots.stream()
            .map(slot -> slot.getAppointmentType().name())
            .collect(Collectors.toSet());
        
        // Extract unique locations
        Set<String> locations = slots.stream()
            .map(AvailabilityResponse.AppointmentSlotData::getLocation)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        // Price range
        OptionalDouble minPrice = slots.stream()
            .filter(slot -> slot.getPrice() != null)
            .mapToDouble(slot -> slot.getPrice().doubleValue())
            .min();
        
        OptionalDouble maxPrice = slots.stream()
            .filter(slot -> slot.getPrice() != null)
            .mapToDouble(slot -> slot.getPrice().doubleValue())
            .max();

        filters.put("specializations", specializations);
        filters.put("appointmentTypes", appointmentTypes);
        filters.put("locations", locations);
        filters.put("priceRange", Map.of(
            "min", minPrice.isPresent() ? minPrice.getAsDouble() : 0,
            "max", maxPrice.isPresent() ? maxPrice.getAsDouble() : 0
        ));
        
        return filters;
    }

    private ResponseEntity<?> createRateLimitResponse(String clientIp) {
        int remaining = rateLimitingService.getRemainingAttempts(clientIp);
        logger.warn("Rate limit exceeded for IP: {}, remaining attempts: {}", clientIp, remaining);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Too many search requests. Please try again later.");
        errorResponse.put("error_code", "RATE_LIMIT_EXCEEDED");
        errorResponse.put("remainingAttempts", remaining);
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
    }

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