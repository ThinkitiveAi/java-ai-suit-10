package com.healthfirst.entity;

import com.healthfirst.enums.AppointmentType;
import com.healthfirst.enums.RecurrencePattern;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "provider_availability", indexes = {
    @Index(name = "idx_provider_date", columnList = "provider_id, availability_date"),
    @Index(name = "idx_provider_date_active", columnList = "provider_id, availability_date, is_active"),
    @Index(name = "idx_appointment_type", columnList = "appointment_type"),
    @Index(name = "idx_active_availability", columnList = "is_active")
})
public class ProviderAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @NotNull
    @Column(name = "availability_date", nullable = false)
    private LocalDate availabilityDate;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Positive
    @Column(name = "slot_duration_minutes", nullable = false)
    private Integer slotDurationMinutes = 30; // Default 30 minutes

    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type", nullable = false)
    private AppointmentType appointmentType = AppointmentType.CONSULTATION;

    @Column(name = "timezone", nullable = false)
    private String timezone = "UTC";

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "location")
    private String location;

    @Column(name = "description")
    private String description;

    // Recurrence settings
    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_pattern", nullable = false)
    private RecurrencePattern recurrencePattern = RecurrencePattern.NONE;

    @Column(name = "recurrence_end_date")
    private LocalDate recurrenceEndDate;

    @Column(name = "recurrence_days_of_week")
    private String recurrenceDaysOfWeek; // JSON array like "[1,2,3,4,5]" for weekdays

    // Status and metadata
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "max_consecutive_slots")
    private Integer maxConsecutiveSlots;

    @Column(name = "buffer_time_minutes")
    private Integer bufferTimeMinutes = 0;

    // UI-Specific availability fields based on healthcare provider screens
    @Column(name = "is_blocked")
    private Boolean isBlocked = false; // Block days functionality
    
    @Column(name = "block_reason", length = 500)
    private String blockReason; // Reason for blocking (vacation, conference, etc.)
    
    @Column(name = "consultation_type", length = 100)
    private String consultationType; // In-person, Video call, Phone call
    
    @Column(name = "allow_walk_ins")
    private Boolean allowWalkIns = false;
    
    @Column(name = "advance_booking_days")
    private Integer advanceBookingDays = 30; // How far in advance patients can book
    
    @Column(name = "same_day_booking")
    private Boolean sameDayBooking = false;
    
    @Column(name = "consultation_duration_minutes")
    private Integer consultationDurationMinutes = 30; // Default consultation time
    
    @Column(name = "break_between_appointments")
    private Integer breakBetweenAppointments = 5; // Buffer time between consultations
    
    @Column(name = "max_appointments_per_day")
    private Integer maxAppointmentsPerDay = 20;
    
    @Column(name = "consultation_fee")
    private BigDecimal consultationFee;
    
    @Column(name = "emergency_available")
    private Boolean emergencyAvailable = false; // Available for emergency consultations
    
    @Column(name = "notes_for_patients", length = 1000)
    private String notesForPatients; // Special instructions visible to patients
    
    @Column(name = "requires_confirmation")
    private Boolean requiresConfirmation = false; // Manual approval required
    
    @Column(name = "send_reminders")
    private Boolean sendReminders = true;
    
    @Column(name = "reminder_time_hours")
    private Integer reminderTimeHours = 24; // When to send reminder
    
    @Column(name = "allow_cancellation")
    private Boolean allowCancellation = true;
    
    @Column(name = "cancellation_hours_before")
    private Integer cancellationHoursBefore = 24; // Minimum notice for cancellation

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "providerAvailability", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AppointmentSlot> appointmentSlots = new ArrayList<>();

    // Constructors
    public ProviderAvailability() {}

    public ProviderAvailability(Provider provider, LocalDate availabilityDate, LocalTime startTime, 
                               LocalTime endTime, Integer slotDurationMinutes, AppointmentType appointmentType, 
                               String timezone) {
        this.provider = provider;
        this.availabilityDate = availabilityDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotDurationMinutes = slotDurationMinutes;
        this.appointmentType = appointmentType;
        this.timezone = timezone;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public LocalDate getAvailabilityDate() {
        return availabilityDate;
    }

    public void setAvailabilityDate(LocalDate availabilityDate) {
        this.availabilityDate = availabilityDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getSlotDurationMinutes() {
        return slotDurationMinutes;
    }

    public void setSlotDurationMinutes(Integer slotDurationMinutes) {
        this.slotDurationMinutes = slotDurationMinutes;
    }

    public AppointmentType getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(AppointmentType appointmentType) {
        this.appointmentType = appointmentType;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RecurrencePattern getRecurrencePattern() {
        return recurrencePattern;
    }

    public void setRecurrencePattern(RecurrencePattern recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }

    public LocalDate getRecurrenceEndDate() {
        return recurrenceEndDate;
    }

    public void setRecurrenceEndDate(LocalDate recurrenceEndDate) {
        this.recurrenceEndDate = recurrenceEndDate;
    }

    public String getRecurrenceDaysOfWeek() {
        return recurrenceDaysOfWeek;
    }

    public void setRecurrenceDaysOfWeek(String recurrenceDaysOfWeek) {
        this.recurrenceDaysOfWeek = recurrenceDaysOfWeek;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getMaxConsecutiveSlots() {
        return maxConsecutiveSlots;
    }

    public void setMaxConsecutiveSlots(Integer maxConsecutiveSlots) {
        this.maxConsecutiveSlots = maxConsecutiveSlots;
    }

    public Integer getBufferTimeMinutes() {
        return bufferTimeMinutes;
    }

    public void setBufferTimeMinutes(Integer bufferTimeMinutes) {
        this.bufferTimeMinutes = bufferTimeMinutes;
    }

    public Boolean getIsBlocked() {
        return isBlocked;
    }

    public void setIsBlocked(Boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    public String getBlockReason() {
        return blockReason;
    }

    public void setBlockReason(String blockReason) {
        this.blockReason = blockReason;
    }

    public String getConsultationType() {
        return consultationType;
    }

    public void setConsultationType(String consultationType) {
        this.consultationType = consultationType;
    }

    public Boolean getAllowWalkIns() {
        return allowWalkIns;
    }

    public void setAllowWalkIns(Boolean allowWalkIns) {
        this.allowWalkIns = allowWalkIns;
    }

    public Integer getAdvanceBookingDays() {
        return advanceBookingDays;
    }

    public void setAdvanceBookingDays(Integer advanceBookingDays) {
        this.advanceBookingDays = advanceBookingDays;
    }

    public Boolean getSameDayBooking() {
        return sameDayBooking;
    }

    public void setSameDayBooking(Boolean sameDayBooking) {
        this.sameDayBooking = sameDayBooking;
    }

    public Integer getConsultationDurationMinutes() {
        return consultationDurationMinutes;
    }

    public void setConsultationDurationMinutes(Integer consultationDurationMinutes) {
        this.consultationDurationMinutes = consultationDurationMinutes;
    }

    public Integer getBreakBetweenAppointments() {
        return breakBetweenAppointments;
    }

    public void setBreakBetweenAppointments(Integer breakBetweenAppointments) {
        this.breakBetweenAppointments = breakBetweenAppointments;
    }

    public Integer getMaxAppointmentsPerDay() {
        return maxAppointmentsPerDay;
    }

    public void setMaxAppointmentsPerDay(Integer maxAppointmentsPerDay) {
        this.maxAppointmentsPerDay = maxAppointmentsPerDay;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public Boolean getEmergencyAvailable() {
        return emergencyAvailable;
    }

    public void setEmergencyAvailable(Boolean emergencyAvailable) {
        this.emergencyAvailable = emergencyAvailable;
    }

    public String getNotesForPatients() {
        return notesForPatients;
    }

    public void setNotesForPatients(String notesForPatients) {
        this.notesForPatients = notesForPatients;
    }

    public Boolean getRequiresConfirmation() {
        return requiresConfirmation;
    }

    public void setRequiresConfirmation(Boolean requiresConfirmation) {
        this.requiresConfirmation = requiresConfirmation;
    }

    public Boolean getSendReminders() {
        return sendReminders;
    }

    public void setSendReminders(Boolean sendReminders) {
        this.sendReminders = sendReminders;
    }

    public Integer getReminderTimeHours() {
        return reminderTimeHours;
    }

    public void setReminderTimeHours(Integer reminderTimeHours) {
        this.reminderTimeHours = reminderTimeHours;
    }

    public Boolean getAllowCancellation() {
        return allowCancellation;
    }

    public void setAllowCancellation(Boolean allowCancellation) {
        this.allowCancellation = allowCancellation;
    }

    public Integer getCancellationHoursBefore() {
        return cancellationHoursBefore;
    }

    public void setCancellationHoursBefore(Integer cancellationHoursBefore) {
        this.cancellationHoursBefore = cancellationHoursBefore;
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

    public List<AppointmentSlot> getAppointmentSlots() {
        return appointmentSlots;
    }

    public void setAppointmentSlots(List<AppointmentSlot> appointmentSlots) {
        this.appointmentSlots = appointmentSlots;
    }

    // Helper methods
    public boolean isValidTimeRange() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }

    public boolean isRecurring() {
        return recurrencePattern != null && recurrencePattern != RecurrencePattern.NONE;
    }

    @Override
    public String toString() {
        return "ProviderAvailability{" +
                "id=" + id +
                ", provider=" + (provider != null ? provider.getId() : null) +
                ", availabilityDate=" + availabilityDate +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", appointmentType=" + appointmentType +
                ", isActive=" + isActive +
                '}';
    }
} 