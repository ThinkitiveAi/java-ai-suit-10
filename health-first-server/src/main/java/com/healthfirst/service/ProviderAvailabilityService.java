package com.healthfirst.service;

import com.healthfirst.dto.*;
import com.healthfirst.entity.AppointmentSlot;
import com.healthfirst.entity.Provider;
import com.healthfirst.entity.ProviderAvailability;
import com.healthfirst.enums.AppointmentType;
import com.healthfirst.enums.RecurrencePattern;
import com.healthfirst.repository.AppointmentSlotRepository;
import com.healthfirst.repository.ProviderAvailabilityRepository;
import com.healthfirst.repository.ProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProviderAvailabilityService {

    private static final Logger logger = LoggerFactory.getLogger(ProviderAvailabilityService.class);

    @Autowired
    private ProviderAvailabilityRepository availabilityRepository;

    @Autowired
    private AppointmentSlotRepository slotRepository;

    @Autowired
    private ProviderRepository providerRepository;

    /**
     * Create availability slots for a provider
     */
    public AvailabilityResponse createAvailability(UUID providerId, CreateAvailabilityRequest request) {
        try {
            logger.info("Creating availability for provider: {} on date: {}", providerId, request.getAvailabilityDate());

            // Validate provider exists and is active
            Provider provider = validateProvider(providerId);

            // Validate business rules
            List<String> validationErrors = validateCreateRequest(request, providerId);
            if (!validationErrors.isEmpty()) {
                logger.warn("Validation errors for create availability: {}", validationErrors);
                return AvailabilityResponse.error("Validation failed: " + String.join(", ", validationErrors), "VALIDATION_ERROR");
            }

            // Check for overlapping availability
            if (hasOverlappingAvailability(providerId, request.getAvailabilityDate(), request.getStartTime(), request.getEndTime())) {
                logger.warn("Overlapping availability detected for provider: {} on date: {}", providerId, request.getAvailabilityDate());
                return AvailabilityResponse.error("Overlapping availability slots are not allowed", "OVERLAP_ERROR");
            }

            // Create availability instances based on recurrence
            List<ProviderAvailability> availabilities = createAvailabilityInstances(provider, request);

            // Save all availability instances
            List<ProviderAvailability> savedAvailabilities = availabilityRepository.saveAll(availabilities);

            // Generate appointment slots for each availability
            for (ProviderAvailability availability : savedAvailabilities) {
                generateAppointmentSlots(availability);
            }

            // Return response with first availability instance
            AvailabilityResponse.AvailabilityData responseData = convertToAvailabilityData(savedAvailabilities.get(0));
            logger.info("Successfully created {} availability instance(s) for provider: {}", savedAvailabilities.size(), providerId);
            
            return AvailabilityResponse.success("Availability created successfully", responseData);

        } catch (Exception e) {
            logger.error("Error creating availability for provider: {}", providerId, e);
            return AvailabilityResponse.error("Failed to create availability. Please try again later.", "CREATE_ERROR");
        }
    }

    /**
     * Get provider availability within date range
     */
    public List<AvailabilityResponse.AvailabilityData> getProviderAvailability(UUID providerId, LocalDate startDate, LocalDate endDate, Boolean includeSlots) {
        try {
            logger.info("Fetching availability for provider: {} from {} to {}", providerId, startDate, endDate);

            // Validate provider exists
            if (!providerRepository.existsById(providerId)) {
                throw new IllegalArgumentException("Provider not found: " + providerId);
            }

            List<ProviderAvailability> availabilities = availabilityRepository
                    .findByProviderIdAndDateRangeAndIsActive(providerId, startDate, endDate);

            return availabilities.stream()
                    .map(availability -> convertToAvailabilityData(availability, includeSlots))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error fetching availability for provider: {}", providerId, e);
            throw new RuntimeException("Failed to fetch availability", e);
        }
    }

    /**
     * Update existing availability
     */
    public AvailabilityResponse updateAvailability(UUID providerId, UUID availabilityId, UpdateAvailabilityRequest request) {
        try {
            logger.info("Updating availability: {} for provider: {}", availabilityId, providerId);

            // Find and validate availability
            ProviderAvailability availability = availabilityRepository.findById(availabilityId)
                    .orElseThrow(() -> new IllegalArgumentException("Availability not found: " + availabilityId));

            if (!availability.getProvider().getId().equals(providerId)) {
                throw new IllegalArgumentException("Availability does not belong to provider: " + providerId);
            }

            // Check if there are booked slots that would be affected
            if (hasBookedSlots(availabilityId) && (request.getStartTime() != null || request.getEndTime() != null || request.getSlotDurationMinutes() != null)) {
                return AvailabilityResponse.error("Cannot modify time settings when there are booked appointments", "BOOKED_SLOTS_ERROR");
            }

            // Validate business rules for update
            List<String> validationErrors = validateUpdateRequest(request, availability);
            if (!validationErrors.isEmpty()) {
                logger.warn("Validation errors for update availability: {}", validationErrors);
                return AvailabilityResponse.error("Validation failed: " + String.join(", ", validationErrors), "VALIDATION_ERROR");
            }

            // Apply updates
            updateAvailabilityEntity(availability, request);

            // Save the updated availability
            ProviderAvailability savedAvailability = availabilityRepository.save(availability);

            // Regenerate slots if requested and no conflicts
            if (Boolean.TRUE.equals(request.getRegenerateSlots()) && !hasBookedSlots(availabilityId)) {
                regenerateAppointmentSlots(savedAvailability);
            }

            AvailabilityResponse.AvailabilityData responseData = convertToAvailabilityData(savedAvailability);
            logger.info("Successfully updated availability: {}", availabilityId);
            
            return AvailabilityResponse.success("Availability updated successfully", responseData);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for update availability: {}", e.getMessage());
            return AvailabilityResponse.error(e.getMessage(), "INVALID_REQUEST");
        } catch (Exception e) {
            logger.error("Error updating availability: {}", availabilityId, e);
            return AvailabilityResponse.error("Failed to update availability. Please try again later.", "UPDATE_ERROR");
        }
    }

    /**
     * Delete availability and associated slots
     */
    public AvailabilityResponse deleteAvailability(UUID providerId, UUID availabilityId, boolean deleteRecurring, String reason) {
        try {
            logger.info("Deleting availability: {} for provider: {}", availabilityId, providerId);

            // Find and validate availability
            ProviderAvailability availability = availabilityRepository.findById(availabilityId)
                    .orElseThrow(() -> new IllegalArgumentException("Availability not found: " + availabilityId));

            if (!availability.getProvider().getId().equals(providerId)) {
                throw new IllegalArgumentException("Availability does not belong to provider: " + providerId);
            }

            // Check if there are booked slots
            if (hasBookedSlots(availabilityId)) {
                return AvailabilityResponse.error("Cannot delete availability with booked appointments", "BOOKED_SLOTS_ERROR");
            }

            // Delete recurring instances if requested
            if (deleteRecurring && availability.isRecurring()) {
                deleteRecurringAvailability(availability);
            } else {
                // Delete just this instance
                deleteAvailabilityInstance(availability);
            }

            logger.info("Successfully deleted availability: {}", availabilityId);
            return AvailabilityResponse.success("Availability deleted successfully", null);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for delete availability: {}", e.getMessage());
            return AvailabilityResponse.error(e.getMessage(), "INVALID_REQUEST");
        } catch (Exception e) {
            logger.error("Error deleting availability: {}", availabilityId, e);
            return AvailabilityResponse.error("Failed to delete availability. Please try again later.", "DELETE_ERROR");
        }
    }

    /**
     * Search available slots for patients
     */
    public List<AvailabilityResponse.AppointmentSlotData> searchAvailableSlots(AvailabilitySearchRequest searchRequest) {
        try {
            logger.info("Searching available slots with criteria: {}", searchRequest);

            // Validate search request
            if (!searchRequest.isValidDateRange()) {
                throw new IllegalArgumentException("Invalid date range");
            }

            // Convert dates to UTC datetime range
            LocalDateTime startDateTime = searchRequest.getStartDate().atStartOfDay();
            LocalDateTime endDateTime = searchRequest.getEndDate().atTime(23, 59, 59);

            List<AppointmentSlot> availableSlots = new ArrayList<>();

            // Search by different criteria
            if (searchRequest.getProviderIds() != null && !searchRequest.getProviderIds().isEmpty()) {
                // Search specific providers
                availableSlots = slotRepository.findAvailableSlotsByProvidersAndTimeRange(
                        searchRequest.getProviderIds(), startDateTime, endDateTime);
            } else if (searchRequest.getAppointmentType() != null) {
                // Search by appointment type
                availableSlots = slotRepository.findAvailableSlotsByTypeAndTimeRange(
                        searchRequest.getAppointmentType(), startDateTime, endDateTime);
            } else if (searchRequest.getMinPrice() != null && searchRequest.getMaxPrice() != null) {
                // Search by price range
                availableSlots = slotRepository.findAvailableSlotsByPriceRange(
                        searchRequest.getMinPrice(), searchRequest.getMaxPrice(), startDateTime, endDateTime);
            } else {
                // General search - we'll implement this with provider filtering
                availableSlots = searchByGeneralCriteria(searchRequest, startDateTime, endDateTime);
            }

            // Apply additional filters
            availableSlots = applySearchFilters(availableSlots, searchRequest);

            // Sort and limit results
            availableSlots = sortAndLimitResults(availableSlots, searchRequest);

            // Convert to response DTOs
            return availableSlots.stream()
                    .map(this::convertToSlotData)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error searching available slots", e);
            throw new RuntimeException("Failed to search available slots", e);
        }
    }

    // ========== PRIVATE HELPER METHODS ==========

    private Provider validateProvider(UUID providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found: " + providerId));

        if (!provider.getIsActive()) {
            throw new IllegalArgumentException("Provider is not active: " + providerId);
        }

        return provider;
    }

    private List<String> validateCreateRequest(CreateAvailabilityRequest request, UUID providerId) {
        List<String> errors = new ArrayList<>();

        // Time range validation
        if (!request.isValidTimeRange()) {
            errors.add("Start time must be before end time");
        }

        // Date validation
        if (request.getAvailabilityDate().isBefore(LocalDate.now())) {
            errors.add("Availability date cannot be in the past");
        }

        // Recurrence validation
        if (!request.isValidRecurrence()) {
            errors.add("Invalid recurrence settings");
        }

        // Timezone validation
        try {
            ZoneId.of(request.getTimezone());
        } catch (Exception e) {
            errors.add("Invalid timezone: " + request.getTimezone());
        }

        return errors;
    }

    private List<String> validateUpdateRequest(UpdateAvailabilityRequest request, ProviderAvailability existing) {
        List<String> errors = new ArrayList<>();

        // Time range validation
        if (!request.isValidTimeRange()) {
            errors.add("Start time must be before end time");
        }

        // Recurrence validation
        if (!request.isValidRecurrence()) {
            errors.add("Invalid recurrence settings");
        }

        // Timezone validation
        if (request.getTimezone() != null) {
            try {
                ZoneId.of(request.getTimezone());
            } catch (Exception e) {
                errors.add("Invalid timezone: " + request.getTimezone());
            }
        }

        return errors;
    }

    private boolean hasOverlappingAvailability(UUID providerId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<ProviderAvailability> overlapping = availabilityRepository.findOverlappingSlots(
                providerId, date, startTime, endTime);
        return !overlapping.isEmpty();
    }

    private List<ProviderAvailability> createAvailabilityInstances(Provider provider, CreateAvailabilityRequest request) {
        List<ProviderAvailability> availabilities = new ArrayList<>();

        if (request.getRecurrencePattern() == RecurrencePattern.NONE) {
            // Single instance
            availabilities.add(createAvailabilityInstance(provider, request, request.getAvailabilityDate()));
        } else {
            // Generate recurring instances
            availabilities.addAll(generateRecurringInstances(provider, request));
        }

        return availabilities;
    }

    private ProviderAvailability createAvailabilityInstance(Provider provider, CreateAvailabilityRequest request, LocalDate date) {
        ProviderAvailability availability = new ProviderAvailability(
                provider, date, request.getStartTime(), request.getEndTime(),
                request.getSlotDurationMinutes(), request.getAppointmentType(), request.getTimezone());

        availability.setPrice(request.getPrice());
        availability.setLocation(request.getLocation());
        availability.setDescription(request.getDescription());
        availability.setRecurrencePattern(request.getRecurrencePattern());
        availability.setRecurrenceEndDate(request.getRecurrenceEndDate());
        availability.setMaxConsecutiveSlots(request.getMaxConsecutiveSlots());
        availability.setBufferTimeMinutes(request.getBufferTimeMinutes());

        // Set all new UI-focused availability fields
        availability.setIsBlocked(request.getBlocked());
        availability.setBlockReason(request.getBlockReason());
        availability.setConsultationType(request.getConsultationType());
        availability.setAllowWalkIns(request.getAllowWalkIns());
        availability.setAdvanceBookingDays(request.getAdvanceBookingDays());
        availability.setSameDayBooking(request.getSameDayBooking());
        availability.setConsultationDurationMinutes(request.getConsultationDurationMinutes());
        availability.setBreakBetweenAppointments(request.getBreakBetweenAppointments());
        availability.setMaxAppointmentsPerDay(request.getMaxAppointmentsPerDay());
        availability.setConsultationFee(request.getConsultationFee());
        availability.setEmergencyAvailable(request.getEmergencyAvailable());
        availability.setNotesForPatients(request.getNotesForPatients());
        availability.setRequiresConfirmation(request.getRequiresConfirmation());
        availability.setSendReminders(request.getSendReminders());
        availability.setReminderTimeHours(request.getReminderTimeHours());
        availability.setAllowCancellation(request.getAllowCancellation());
        availability.setCancellationHoursBefore(request.getCancellationHoursBefore());

        // Convert recurrence days to JSON string
        if (request.getRecurrenceDaysOfWeek() != null) {
            availability.setRecurrenceDaysOfWeek(request.getRecurrenceDaysOfWeek().toString());
        }

        return availability;
    }

    private List<ProviderAvailability> generateRecurringInstances(Provider provider, CreateAvailabilityRequest request) {
        List<ProviderAvailability> instances = new ArrayList<>();
        LocalDate currentDate = request.getAvailabilityDate();
        LocalDate endDate = request.getRecurrenceEndDate() != null ? 
                request.getRecurrenceEndDate() : currentDate.plusMonths(6); // Default 6 months

        while (!currentDate.isAfter(endDate)) {
            if (shouldCreateInstanceOnDate(currentDate, request)) {
                instances.add(createAvailabilityInstance(provider, request, currentDate));
            }
            currentDate = getNextRecurrenceDate(currentDate, request.getRecurrencePattern());
        }

        return instances;
    }

    private boolean shouldCreateInstanceOnDate(LocalDate date, CreateAvailabilityRequest request) {
        return switch (request.getRecurrencePattern()) {
            case DAILY -> true;
            case WEEKLY -> true;
            case WEEKDAYS -> date.getDayOfWeek().getValue() <= 5;
            case WEEKENDS -> date.getDayOfWeek().getValue() > 5;
            case CUSTOM -> request.getRecurrenceDaysOfWeek() != null && 
                          request.getRecurrenceDaysOfWeek().contains(date.getDayOfWeek().getValue());
            default -> false;
        };
    }

    private LocalDate getNextRecurrenceDate(LocalDate currentDate, RecurrencePattern pattern) {
        return switch (pattern) {
            case DAILY -> currentDate.plusDays(1);
            case WEEKLY, WEEKDAYS, WEEKENDS, CUSTOM -> currentDate.plusDays(1);
            case MONTHLY -> currentDate.plusMonths(1);
            default -> currentDate.plusDays(1);
        };
    }

    private void generateAppointmentSlots(ProviderAvailability availability) {
        List<AppointmentSlot> slots = new ArrayList<>();
        
        // Convert to provider's timezone for slot generation
        ZoneId timezone = ZoneId.of(availability.getTimezone());
        LocalDateTime startDateTime = LocalDateTime.of(availability.getAvailabilityDate(), availability.getStartTime());
        LocalDateTime endDateTime = LocalDateTime.of(availability.getAvailabilityDate(), availability.getEndTime());
        
        // Convert to UTC for storage
        ZonedDateTime startZoned = startDateTime.atZone(timezone);
        ZonedDateTime endZoned = endDateTime.atZone(timezone);
        LocalDateTime currentUtc = startZoned.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime endUtc = endZoned.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        while (currentUtc.plus(availability.getSlotDurationMinutes(), ChronoUnit.MINUTES).isBefore(endUtc) ||
               currentUtc.plus(availability.getSlotDurationMinutes(), ChronoUnit.MINUTES).equals(endUtc)) {
            
            LocalDateTime slotEndUtc = currentUtc.plus(availability.getSlotDurationMinutes(), ChronoUnit.MINUTES);
            
            AppointmentSlot slot = new AppointmentSlot(
                    availability, availability.getProvider(), currentUtc, slotEndUtc, availability.getAppointmentType());
            
            slot.setPrice(availability.getPrice());
            slot.setLocation(availability.getLocation());
            
            slots.add(slot);
            
            // Move to next slot (plus buffer time)
            currentUtc = slotEndUtc.plus(availability.getBufferTimeMinutes(), ChronoUnit.MINUTES);
        }

        slotRepository.saveAll(slots);
        logger.info("Generated {} appointment slots for availability: {}", slots.size(), availability.getId());
    }

    private boolean hasBookedSlots(UUID availabilityId) {
        List<AppointmentSlot> slots = slotRepository.findSlotsByProviderAvailability(availabilityId);
        return slots.stream().anyMatch(AppointmentSlot::getIsBooked);
    }

    private void updateAvailabilityEntity(ProviderAvailability availability, UpdateAvailabilityRequest request) {
        if (request.getAvailabilityDate() != null) {
            availability.setAvailabilityDate(request.getAvailabilityDate());
        }
        if (request.getStartTime() != null) {
            availability.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            availability.setEndTime(request.getEndTime());
        }
        if (request.getSlotDurationMinutes() != null) {
            availability.setSlotDurationMinutes(request.getSlotDurationMinutes());
        }
        if (request.getAppointmentType() != null) {
            availability.setAppointmentType(request.getAppointmentType());
        }
        if (request.getTimezone() != null) {
            availability.setTimezone(request.getTimezone());
        }
        if (request.getPrice() != null) {
            availability.setPrice(request.getPrice());
        }
        if (request.getLocation() != null) {
            availability.setLocation(request.getLocation());
        }
        if (request.getDescription() != null) {
            availability.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            availability.setIsActive(request.getIsActive());
        }
        if (request.getMaxConsecutiveSlots() != null) {
            availability.setMaxConsecutiveSlots(request.getMaxConsecutiveSlots());
        }
        if (request.getBufferTimeMinutes() != null) {
            availability.setBufferTimeMinutes(request.getBufferTimeMinutes());
        }
    }

    private void regenerateAppointmentSlots(ProviderAvailability availability) {
        // Delete existing slots
        List<AppointmentSlot> existingSlots = slotRepository.findSlotsByProviderAvailability(availability.getId());
        slotRepository.deleteAll(existingSlots);
        
        // Generate new slots
        generateAppointmentSlots(availability);
    }

    private void deleteRecurringAvailability(ProviderAvailability availability) {
        // Implementation would find and delete all recurring instances
        // For now, just delete this instance
        deleteAvailabilityInstance(availability);
    }

    private void deleteAvailabilityInstance(ProviderAvailability availability) {
        // Delete associated slots first
        List<AppointmentSlot> slots = slotRepository.findSlotsByProviderAvailability(availability.getId());
        slotRepository.deleteAll(slots);
        
        // Delete availability
        availabilityRepository.delete(availability);
    }

    private List<AppointmentSlot> searchByGeneralCriteria(AvailabilitySearchRequest request, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Implementation for general search criteria
        // This is a simplified version - in practice, you'd want more sophisticated filtering
        return slotRepository.findAvailableSlotsByProvidersAndTimeRange(
                Collections.emptyList(), startDateTime, endDateTime).stream()
                .limit(request.getMaxResults())
                .collect(Collectors.toList());
    }

    private List<AppointmentSlot> applySearchFilters(List<AppointmentSlot> slots, AvailabilitySearchRequest request) {
        return slots.stream()
                .filter(slot -> applySlotFilters(slot, request))
                .collect(Collectors.toList());
    }

    private boolean applySlotFilters(AppointmentSlot slot, AvailabilitySearchRequest request) {
        // Apply time filter
        if (request.getPreferredStartTime() != null && request.getPreferredEndTime() != null) {
            LocalTime slotTime = slot.getStartDateTime().toLocalTime();
            if (slotTime.isBefore(request.getPreferredStartTime()) || slotTime.isAfter(request.getPreferredEndTime())) {
                return false;
            }
        }

        // Apply specialization filter
        if (request.getSpecialization() != null) {
            String providerSpecialization = slot.getProvider().getSpecialization();
            if (providerSpecialization == null || !providerSpecialization.equalsIgnoreCase(request.getSpecialization())) {
                return false;
            }
        }

        // Apply location filter
        if (request.getLocation() != null && slot.getLocation() != null) {
            if (!slot.getLocation().toLowerCase().contains(request.getLocation().toLowerCase())) {
                return false;
            }
        }

        return true;
    }

    private List<AppointmentSlot> sortAndLimitResults(List<AppointmentSlot> slots, AvailabilitySearchRequest request) {
        // Sort by specified criteria
        Comparator<AppointmentSlot> comparator = getSlotComparator(request.getSortBy(), request.getAscending());
        
        return slots.stream()
                .sorted(comparator)
                .limit(request.getMaxResults())
                .collect(Collectors.toList());
    }

    private Comparator<AppointmentSlot> getSlotComparator(String sortBy, Boolean ascending) {
        Comparator<AppointmentSlot> comparator = switch (sortBy) {
            case "price" -> Comparator.comparing(AppointmentSlot::getPrice, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(AppointmentSlot::getStartDateTime);
        };
        
        return Boolean.TRUE.equals(ascending) ? comparator : comparator.reversed();
    }

    private AvailabilityResponse.AvailabilityData convertToAvailabilityData(ProviderAvailability availability) {
        return convertToAvailabilityData(availability, false);
    }

    private AvailabilityResponse.AvailabilityData convertToAvailabilityData(ProviderAvailability availability, Boolean includeSlots) {
        AvailabilityResponse.AvailabilityData data = new AvailabilityResponse.AvailabilityData(
                availability.getId(),
                availability.getProvider().getId(),
                availability.getProvider().getFirstName() + " " + availability.getProvider().getLastName(),
                availability.getProvider().getSpecialization(),
                availability.getAvailabilityDate(),
                availability.getStartTime(),
                availability.getEndTime(),
                availability.getSlotDurationMinutes(),
                availability.getAppointmentType(),
                availability.getTimezone()
        );

        data.setPrice(availability.getPrice());
        data.setLocation(availability.getLocation());
        data.setDescription(availability.getDescription());
        data.setRecurrencePattern(availability.getRecurrencePattern());
        data.setRecurrenceEndDate(availability.getRecurrenceEndDate());
        data.setIsActive(availability.getIsActive());
        data.setMaxConsecutiveSlots(availability.getMaxConsecutiveSlots());
        data.setBufferTimeMinutes(availability.getBufferTimeMinutes());
        data.setCreatedAt(availability.getCreatedAt());
        data.setUpdatedAt(availability.getUpdatedAt());

        // Parse recurrence days from JSON
        if (availability.getRecurrenceDaysOfWeek() != null) {
            try {
                List<Integer> days = Arrays.stream(availability.getRecurrenceDaysOfWeek()
                        .replaceAll("[\\[\\]\\s]", "").split(","))
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
                data.setRecurrenceDaysOfWeek(days);
            } catch (Exception e) {
                logger.warn("Error parsing recurrence days: {}", availability.getRecurrenceDaysOfWeek());
            }
        }

        // Include appointment slots if requested
        if (Boolean.TRUE.equals(includeSlots)) {
            List<AvailabilityResponse.AppointmentSlotData> slotData = availability.getAppointmentSlots().stream()
                    .map(this::convertToSlotData)
                    .collect(Collectors.toList());
            data.setAppointmentSlots(slotData);
        }

        return data;
    }

    private AvailabilityResponse.AppointmentSlotData convertToSlotData(AppointmentSlot slot) {
        AvailabilityResponse.AppointmentSlotData data = new AvailabilityResponse.AppointmentSlotData(
                slot.getId(),
                slot.getStartDateTime(),
                slot.getEndDateTime(),
                slot.getAppointmentType(),
                slot.getIsBooked(),
                slot.getIsActive()
        );

        data.setPrice(slot.getPrice());
        data.setLocation(slot.getLocation());
        data.setBookingConfirmed(slot.getBookingConfirmed());

        // Only include patient info for booked slots and if user has permission
        if (slot.getIsBooked() && slot.getPatient() != null) {
            data.setPatientId(slot.getPatient().getId());
            data.setPatientName(slot.getPatient().getFirstName() + " " + slot.getPatient().getLastName());
        }

        return data;
    }
} 