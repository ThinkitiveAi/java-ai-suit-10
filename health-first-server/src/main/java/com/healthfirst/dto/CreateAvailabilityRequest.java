package com.healthfirst.dto;

import com.healthfirst.enums.AppointmentType;
import com.healthfirst.enums.RecurrencePattern;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class CreateAvailabilityRequest {

    @NotNull(message = "Availability date is required")
    @Future(message = "Availability date must be in the future")
    private LocalDate availabilityDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @Positive(message = "Slot duration must be positive")
    @Min(value = 15, message = "Slot duration must be at least 15 minutes")
    @Max(value = 480, message = "Slot duration cannot exceed 8 hours")
    private Integer slotDurationMinutes = 30;

    @NotNull(message = "Appointment type is required")
    private AppointmentType appointmentType;

    @NotBlank(message = "Timezone is required")
    private String timezone = "UTC";

    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 2 decimal places")
    private BigDecimal price;

    @Size(max = 255, message = "Location must be at most 255 characters")
    private String location;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    // Recurrence settings
    private RecurrencePattern recurrencePattern = RecurrencePattern.NONE;

    private LocalDate recurrenceEndDate;

    private List<Integer> recurrenceDaysOfWeek; // 1=Monday, 7=Sunday

    @Min(value = 1, message = "Max consecutive slots must be at least 1")
    @Max(value = 20, message = "Max consecutive slots cannot exceed 20")
    private Integer maxConsecutiveSlots;

    @Min(value = 0, message = "Buffer time cannot be negative")
    @Max(value = 120, message = "Buffer time cannot exceed 2 hours")
    private Integer bufferTimeMinutes = 0;

    // UI-Specific availability fields matching healthcare provider screens
    private Boolean isBlocked = false; // Block days functionality
    
    @Size(max = 500, message = "Block reason must be at most 500 characters")
    private String blockReason; // Reason for blocking (vacation, conference, etc.)
    
    @Size(max = 100, message = "Consultation type must be at most 100 characters")
    private String consultationType = "In-person"; // In-person, Video call, Phone call
    
    private Boolean allowWalkIns = false;
    
    @Min(value = 1, message = "Advance booking days must be at least 1")
    @Max(value = 90, message = "Advance booking days cannot exceed 90")
    private Integer advanceBookingDays = 30; // How far in advance patients can book
    
    private Boolean sameDayBooking = false;
    
    @Min(value = 15, message = "Consultation duration must be at least 15 minutes")
    @Max(value = 240, message = "Consultation duration cannot exceed 4 hours")
    private Integer consultationDurationMinutes = 30; // Default consultation time
    
    @Min(value = 0, message = "Break between appointments cannot be negative")
    @Max(value = 60, message = "Break between appointments cannot exceed 1 hour")
    private Integer breakBetweenAppointments = 5; // Buffer time between consultations
    
    @Min(value = 1, message = "Max appointments per day must be at least 1")
    @Max(value = 50, message = "Max appointments per day cannot exceed 50")
    private Integer maxAppointmentsPerDay = 20;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Consultation fee must be non-negative")
    @Digits(integer = 6, fraction = 2, message = "Consultation fee must have at most 2 decimal places")
    private BigDecimal consultationFee;
    
    private Boolean emergencyAvailable = false; // Available for emergency consultations
    
    @Size(max = 1000, message = "Notes for patients must be at most 1000 characters")
    private String notesForPatients; // Special instructions visible to patients
    
    private Boolean requiresConfirmation = false; // Manual approval required
    
    private Boolean sendReminders = true;
    
    @Min(value = 1, message = "Reminder time must be at least 1 hour")
    @Max(value = 168, message = "Reminder time cannot exceed 1 week")
    private Integer reminderTimeHours = 24; // When to send reminder
    
    private Boolean allowCancellation = true;
    
    @Min(value = 1, message = "Cancellation notice must be at least 1 hour")
    @Max(value = 168, message = "Cancellation notice cannot exceed 1 week")
    private Integer cancellationHoursBefore = 24; // Minimum notice for cancellation

    // Constructors
    public CreateAvailabilityRequest() {}

    public CreateAvailabilityRequest(LocalDate availabilityDate, LocalTime startTime, LocalTime endTime, 
                                   Integer slotDurationMinutes, AppointmentType appointmentType, String timezone) {
        this.availabilityDate = availabilityDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotDurationMinutes = slotDurationMinutes;
        this.appointmentType = appointmentType;
        this.timezone = timezone;
    }

    // Getters and Setters
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

    public List<Integer> getRecurrenceDaysOfWeek() {
        return recurrenceDaysOfWeek;
    }

    public void setRecurrenceDaysOfWeek(List<Integer> recurrenceDaysOfWeek) {
        this.recurrenceDaysOfWeek = recurrenceDaysOfWeek;
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

    public Boolean getBlocked() {
        return isBlocked;
    }

    public void setBlocked(Boolean blocked) {
        isBlocked = blocked;
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

    // Validation helpers
    public boolean isValidTimeRange() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }

    public boolean isValidRecurrence() {
        if (recurrencePattern == RecurrencePattern.NONE) {
            return true;
        }
        
        if (recurrencePattern == RecurrencePattern.CUSTOM && 
            (recurrenceDaysOfWeek == null || recurrenceDaysOfWeek.isEmpty())) {
            return false;
        }
        
        return recurrenceEndDate == null || recurrenceEndDate.isAfter(availabilityDate);
    }

    @Override
    public String toString() {
        return "CreateAvailabilityRequest{" +
                "availabilityDate=" + availabilityDate +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", slotDurationMinutes=" + slotDurationMinutes +
                ", appointmentType=" + appointmentType +
                ", timezone='" + timezone + '\'' +
                ", recurrencePattern=" + recurrencePattern +
                '}';
    }
} 