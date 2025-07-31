package com.healthfirst.entity;

import com.healthfirst.enums.AppointmentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointment_slot", indexes = {
    @Index(name = "idx_provider_start_time", columnList = "provider_id, start_date_time"),
    @Index(name = "idx_start_time_booked", columnList = "start_date_time, is_booked"),
    @Index(name = "idx_provider_booked_active", columnList = "provider_id, is_booked, is_active"),
    @Index(name = "idx_appointment_type_slot", columnList = "appointment_type"),
    @Index(name = "idx_patient_slot", columnList = "patient_id"),
    @Index(name = "idx_availability_slot", columnList = "provider_availability_id")
})
public class AppointmentSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_availability_id", nullable = false)
    private ProviderAvailability providerAvailability;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @NotNull
    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime; // Stored in UTC

    @NotNull
    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime; // Stored in UTC

    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type", nullable = false)
    private AppointmentType appointmentType;

    @Column(name = "is_booked", nullable = false)
    private Boolean isBooked = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "location")
    private String location;

    @Column(name = "booking_reason")
    private String bookingReason;

    @Column(name = "provider_notes")
    private String providerNotes;

    @Column(name = "patient_notes")
    private String patientNotes;

    @Column(name = "booking_confirmed", nullable = false)
    private Boolean bookingConfirmed = false;

    @Column(name = "booked_at")
    private LocalDateTime bookedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public AppointmentSlot() {}

    public AppointmentSlot(ProviderAvailability providerAvailability, Provider provider, 
                          LocalDateTime startDateTime, LocalDateTime endDateTime, 
                          AppointmentType appointmentType) {
        this.providerAvailability = providerAvailability;
        this.provider = provider;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.appointmentType = appointmentType;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ProviderAvailability getProviderAvailability() {
        return providerAvailability;
    }

    public void setProviderAvailability(ProviderAvailability providerAvailability) {
        this.providerAvailability = providerAvailability;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public AppointmentType getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(AppointmentType appointmentType) {
        this.appointmentType = appointmentType;
    }

    public Boolean getIsBooked() {
        return isBooked;
    }

    public void setIsBooked(Boolean isBooked) {
        this.isBooked = isBooked;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBookingReason() {
        return bookingReason;
    }

    public void setBookingReason(String bookingReason) {
        this.bookingReason = bookingReason;
    }

    public String getProviderNotes() {
        return providerNotes;
    }

    public void setProviderNotes(String providerNotes) {
        this.providerNotes = providerNotes;
    }

    public String getPatientNotes() {
        return patientNotes;
    }

    public void setPatientNotes(String patientNotes) {
        this.patientNotes = patientNotes;
    }

    public Boolean getBookingConfirmed() {
        return bookingConfirmed;
    }

    public void setBookingConfirmed(Boolean bookingConfirmed) {
        this.bookingConfirmed = bookingConfirmed;
    }

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(LocalDateTime bookedAt) {
        this.bookedAt = bookedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public boolean isAvailable() {
        return isActive && !isBooked && startDateTime.isAfter(LocalDateTime.now());
    }

    public boolean hasTimeConflict(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return !(endDateTime.isBefore(otherStart) || startDateTime.isAfter(otherEnd));
    }

    public void bookSlot(Patient patient, String reason) {
        this.patient = patient;
        this.isBooked = true;
        this.bookingReason = reason;
        this.bookedAt = LocalDateTime.now();
    }

    public void cancelSlot(String reason) {
        this.isBooked = false;
        this.patient = null;
        this.cancellationReason = reason;
        this.cancelledAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "AppointmentSlot{" +
                "id=" + id +
                ", provider=" + (provider != null ? provider.getId() : null) +
                ", patient=" + (patient != null ? patient.getId() : null) +
                ", startDateTime=" + startDateTime +
                ", endDateTime=" + endDateTime +
                ", appointmentType=" + appointmentType +
                ", isBooked=" + isBooked +
                ", isActive=" + isActive +
                '}';
    }
} 