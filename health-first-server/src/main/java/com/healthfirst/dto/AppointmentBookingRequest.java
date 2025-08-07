package com.healthfirst.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class AppointmentBookingRequest {

    @NotNull(message = "Slot ID is required")
    private UUID slotId;

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    private UUID providerId; // Optional - for validation

    @Size(max = 500, message = "Booking reason must be at most 500 characters")
    private String bookingReason;

    @Size(max = 1000, message = "Patient notes must be at most 1000 characters")
    private String patientNotes;

    private Boolean requiresConfirmation; // For appointments that need provider approval

    // Default constructor
    public AppointmentBookingRequest() {}

    // Constructor
    public AppointmentBookingRequest(UUID slotId, UUID patientId, String bookingReason) {
        this.slotId = slotId;
        this.patientId = patientId;
        this.bookingReason = bookingReason;
    }

    // Getters and Setters
    public UUID getSlotId() {
        return slotId;
    }

    public void setSlotId(UUID slotId) {
        this.slotId = slotId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
    }

    public String getBookingReason() {
        return bookingReason;
    }

    public void setBookingReason(String bookingReason) {
        this.bookingReason = bookingReason;
    }

    public String getPatientNotes() {
        return patientNotes;
    }

    public void setPatientNotes(String patientNotes) {
        this.patientNotes = patientNotes;
    }

    public Boolean getRequiresConfirmation() {
        return requiresConfirmation;
    }

    public void setRequiresConfirmation(Boolean requiresConfirmation) {
        this.requiresConfirmation = requiresConfirmation;
    }

    @Override
    public String toString() {
        return "AppointmentBookingRequest{" +
                "slotId=" + slotId +
                ", patientId=" + patientId +
                ", providerId=" + providerId +
                ", bookingReason='" + bookingReason + '\'' +
                ", requiresConfirmation=" + requiresConfirmation +
                '}';
    }
} 