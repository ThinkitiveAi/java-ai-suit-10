package com.healthfirst.controller;

import com.healthfirst.dto.AppointmentBookingRequest;
import com.healthfirst.dto.AppointmentBookingResponse;
import com.healthfirst.dto.AppointmentListRequest;
import com.healthfirst.dto.AppointmentListResponse;
import com.healthfirst.entity.AppointmentSlot;
import com.healthfirst.entity.Patient;
import com.healthfirst.entity.Provider;
import com.healthfirst.repository.AppointmentSlotRepository;
import com.healthfirst.repository.PatientRepository;
import com.healthfirst.repository.ProviderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/appointments")
@Tag(name = "Appointment Management", description = "APIs for booking and managing appointments between patients and providers")
public class AppointmentController {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);

    @Autowired
    private AppointmentSlotRepository appointmentSlotRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Operation(summary = "Book an appointment", 
               description = "Book an appointment for a patient with a provider based on available slots")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Appointment booked successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - invalid booking data"),
        @ApiResponse(responseCode = "404", description = "Slot, patient, or provider not found"),
        @ApiResponse(responseCode = "409", description = "Conflict - slot already booked or not available"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    @Transactional
    public ResponseEntity<AppointmentBookingResponse> bookAppointment(
            @Valid @RequestBody AppointmentBookingRequest request) {
        
        try {
            logger.info("Booking appointment request: {}", request);

            // Validate and fetch appointment slot
            Optional<AppointmentSlot> optionalSlot = appointmentSlotRepository.findById(request.getSlotId());
            if (optionalSlot.isEmpty()) {
                logger.warn("Appointment slot not found: {}", request.getSlotId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(AppointmentBookingResponse.error("Appointment slot not found", "SLOT_NOT_FOUND"));
            }

            AppointmentSlot slot = optionalSlot.get();

            // Check if slot is available
            if (!slot.isAvailable()) {
                logger.warn("Appointment slot not available: {}", request.getSlotId());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(AppointmentBookingResponse.error("Appointment slot is not available", "SLOT_NOT_AVAILABLE"));
            }

            // Validate and fetch patient
            Optional<Patient> optionalPatient = patientRepository.findById(request.getPatientId());
            if (optionalPatient.isEmpty()) {
                logger.warn("Patient not found: {}", request.getPatientId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(AppointmentBookingResponse.error("Patient not found", "PATIENT_NOT_FOUND"));
            }

            Patient patient = optionalPatient.get();

            // Check if patient is active
            if (!patient.getIsActive()) {
                logger.warn("Patient is not active: {}", request.getPatientId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AppointmentBookingResponse.error("Patient account is not active", "PATIENT_NOT_ACTIVE"));
            }

            // Validate provider if specified (optional check since slot already has provider)
            if (request.getProviderId() != null && !slot.getProvider().getId().equals(request.getProviderId())) {
                logger.warn("Provider ID mismatch. Slot provider: {}, Requested provider: {}", 
                    slot.getProvider().getId(), request.getProviderId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AppointmentBookingResponse.error("Provider ID does not match slot provider", "PROVIDER_MISMATCH"));
            }

            // Check for conflicting appointments for the patient
            if (hasConflictingAppointment(patient.getId(), slot.getStartDateTime(), slot.getEndDateTime())) {
                logger.warn("Patient has conflicting appointment: {}", request.getPatientId());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(AppointmentBookingResponse.error("Patient has a conflicting appointment at this time", "TIME_CONFLICT"));
            }

            // Check provider availability settings
            if (slot.getProviderAvailability().getRequiresConfirmation() && 
                (request.getRequiresConfirmation() == null || !request.getRequiresConfirmation())) {
                logger.info("Appointment requires provider confirmation: {}", request.getSlotId());
            }

            // Book the appointment
            slot.bookSlot(patient, request.getBookingReason());
            slot.setPatientNotes(request.getPatientNotes());
            slot.setBookingConfirmed(!slot.getProviderAvailability().getRequiresConfirmation());

            // Save the appointment
            AppointmentSlot bookedSlot = appointmentSlotRepository.save(slot);

            // Create response
            AppointmentBookingResponse.AppointmentData appointmentData = 
                new AppointmentBookingResponse.AppointmentData(
                    bookedSlot.getId(),
                    bookedSlot.getProvider().getId(),
                    bookedSlot.getProvider().getFirstName() + " " + bookedSlot.getProvider().getLastName(),
                    bookedSlot.getPatient().getId(),
                    bookedSlot.getPatient().getFirstName() + " " + bookedSlot.getPatient().getLastName(),
                    bookedSlot.getStartDateTime(),
                    bookedSlot.getEndDateTime(),
                    bookedSlot.getAppointmentType(),
                    bookedSlot.getPrice(),
                    bookedSlot.getLocation(),
                    bookedSlot.getBookingReason(),
                    bookedSlot.getBookingConfirmed(),
                    bookedSlot.getBookedAt()
                );

            logger.info("Successfully booked appointment: {}", bookedSlot.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(AppointmentBookingResponse.success("Appointment booked successfully", appointmentData));

        } catch (Exception e) {
            logger.error("Error booking appointment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AppointmentBookingResponse.error("Failed to book appointment. Please try again.", "BOOKING_ERROR"));
        }
    }

    @Operation(summary = "List appointments", 
               description = "Get a paginated list of appointments with filtering options")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved appointments"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "404", description = "Patient or provider not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<AppointmentListResponse> listAppointments(@Valid AppointmentListRequest request) {
        try {
            logger.info("Listing appointments with filters: {}", request);

            // Validate date range if provided
            if (request.hasDateFilter() && !request.isValidDateRange()) {
                return ResponseEntity.badRequest()
                    .body(AppointmentListResponse.error("Invalid date range", "INVALID_DATE_RANGE"));
            }

            // Validate patient if specified
            if (request.isPatientSpecific() && !patientRepository.existsById(request.getPatientId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(AppointmentListResponse.error("Patient not found", "PATIENT_NOT_FOUND"));
            }

            // Validate provider if specified
            if (request.isProviderSpecific() && !providerRepository.existsById(request.getProviderId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(AppointmentListResponse.error("Provider not found", "PROVIDER_NOT_FOUND"));
            }

            // Create pageable request
            Sort sort = createSort(request.getSortBy(), request.isAscending());
            Pageable pageable = PageRequest.of(request.getPage() - 1, request.getPageSize(), sort);

            // Get appointments based on filters
            Page<AppointmentSlot> appointmentPage = findAppointments(request, pageable);

            // Convert to response DTOs
            List<AppointmentListResponse.AppointmentSummary> appointmentSummaries = 
                appointmentPage.getContent().stream()
                    .map(this::convertToSummary)
                    .collect(Collectors.toList());

            // Create pagination data
            AppointmentListResponse.PaginationData paginationData = new AppointmentListResponse.PaginationData(
                request.getPage(),
                request.getPageSize(),
                appointmentPage.getTotalElements(),
                appointmentPage.getTotalPages()
            );

            return ResponseEntity.ok(AppointmentListResponse.success(
                "Appointments retrieved successfully",
                appointmentSummaries,
                paginationData
            ));

        } catch (Exception e) {
            logger.error("Error listing appointments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AppointmentListResponse.error("Failed to retrieve appointments", "LIST_ERROR"));
        }
    }

    private Sort createSort(String sortBy, boolean ascending) {
        Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        // Map frontend sort fields to entity fields
        String sortField = switch (sortBy) {
            case "startTime" -> "startDateTime";
            case "endTime" -> "endDateTime";
            case "bookedAt" -> "bookedAt";
            case "price" -> "price";
            default -> "startDateTime";
        };

        return Sort.by(direction, sortField);
    }

    private Page<AppointmentSlot> findAppointments(AppointmentListRequest request, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        
        // Convert dates to LocalDateTime for filtering
        LocalDateTime startDateTime = request.getStartDate() != null ? 
            request.getStartDate().atStartOfDay() : null;
        LocalDateTime endDateTime = request.getEndDate() != null ? 
            request.getEndDate().plusDays(1).atStartOfDay() : null;

        // Apply filters based on request
        return switch (request.getFilterType().toLowerCase()) {
            case "upcoming" -> appointmentSlotRepository.findUpcomingAppointments(
                request.getPatientId(), request.getProviderId(), now, pageable);
            case "past" -> appointmentSlotRepository.findPastAppointments(
                request.getPatientId(), request.getProviderId(), now, pageable);
            case "cancelled" -> appointmentSlotRepository.findCancelledAppointments(
                request.getPatientId(), request.getProviderId(), startDateTime, endDateTime, pageable);
            default -> appointmentSlotRepository.findAllAppointments(
                request.getPatientId(), request.getProviderId(), startDateTime, endDateTime, pageable);
        };
    }

    private AppointmentListResponse.AppointmentSummary convertToSummary(AppointmentSlot slot) {
        AppointmentListResponse.AppointmentSummary summary = new AppointmentListResponse.AppointmentSummary(
            slot.getId(),
            slot.getProvider().getId(),
            slot.getProvider().getFirstName() + " " + slot.getProvider().getLastName(),
            slot.getPatient() != null ? slot.getPatient().getId() : null,
            slot.getPatient() != null ? slot.getPatient().getFirstName() + " " + slot.getPatient().getLastName() : null,
            slot.getStartDateTime(),
            slot.getEndDateTime(),
            slot.getAppointmentType(),
            determineAppointmentStatus(slot),
            slot.getBookingConfirmed()
        );

        // Set additional fields
        summary.setProviderSpecialization(slot.getProvider().getSpecialization());
        summary.setProviderImage(slot.getProvider().getProfileImage());
        if (slot.getPatient() != null) {
            summary.setPatientImage(slot.getPatient().getProfileImage());
        }
        summary.setPrice(slot.getPrice());
        summary.setLocation(slot.getLocation());
        summary.setBookingReason(slot.getBookingReason());
        summary.setBookedAt(slot.getBookedAt());
        summary.setConsultationType(slot.getProviderAvailability().getConsultationType());
        summary.setCancellationReason(slot.getCancellationReason());
        summary.setCancelledAt(slot.getCancelledAt());
        summary.setPatientNotes(slot.getPatientNotes());
        summary.setProviderNotes(slot.getProviderNotes());

        return summary;
    }

    private String determineAppointmentStatus(AppointmentSlot slot) {
        if (slot.getCancelledAt() != null) {
            return "CANCELLED";
        }
        if (!slot.getIsBooked()) {
            return "AVAILABLE";
        }
        if (!slot.getBookingConfirmed()) {
            return "PENDING_CONFIRMATION";
        }
        if (slot.getStartDateTime().isAfter(LocalDateTime.now())) {
            return "UPCOMING";
        }
        if (slot.getEndDateTime().isBefore(LocalDateTime.now())) {
            return "COMPLETED";
        }
        return "IN_PROGRESS";
    }

    /**
     * Helper method to check for conflicting appointments
     */
    private boolean hasConflictingAppointment(UUID patientId, LocalDateTime startTime, LocalDateTime endTime) {
        return appointmentSlotRepository.findBookedSlotsByPatient(patientId)
            .stream()
            .anyMatch(slot -> slot.hasTimeConflict(startTime, endTime));
    }
} 