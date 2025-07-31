package com.healthfirst.dto;

import com.healthfirst.enums.AppointmentType;
import com.healthfirst.enums.RecurrencePattern;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class UpdateAvailabilityRequest {

    private LocalDate availabilityDate;

    private LocalTime startTime;

    private LocalTime endTime;

    @Positive(message = "Slot duration must be positive")
    @Min(value = 15, message = "Slot duration must be at least 15 minutes")
    @Max(value = 480, message = "Slot duration cannot exceed 8 hours")
    private Integer slotDurationMinutes;

    private AppointmentType appointmentType;

    private String timezone;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 2 decimal places")
    private BigDecimal price;

    @Size(max = 255, message = "Location must be at most 255 characters")
    private String location;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    // Recurrence settings
    private RecurrencePattern recurrencePattern;

    private LocalDate recurrenceEndDate;

    private List<Integer> recurrenceDaysOfWeek; // 1=Monday, 7=Sunday

    @Min(value = 1, message = "Max consecutive slots must be at least 1")
    @Max(value = 20, message = "Max consecutive slots cannot exceed 20")
    private Integer maxConsecutiveSlots;

    @Min(value = 0, message = "Buffer time cannot be negative")
    @Max(value = 120, message = "Buffer time cannot exceed 2 hours")
    private Integer bufferTimeMinutes;

    private Boolean isActive;

    // Update options
    private Boolean updateRecurring = false; // Whether to update all recurring instances
    private Boolean regenerateSlots = false; // Whether to regenerate appointment slots

    // Constructors
    public UpdateAvailabilityRequest() {}

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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getUpdateRecurring() {
        return updateRecurring;
    }

    public void setUpdateRecurring(Boolean updateRecurring) {
        this.updateRecurring = updateRecurring;
    }

    public Boolean getRegenerateSlots() {
        return regenerateSlots;
    }

    public void setRegenerateSlots(Boolean regenerateSlots) {
        this.regenerateSlots = regenerateSlots;
    }

    // Validation helpers
    public boolean isValidTimeRange() {
        if (startTime == null || endTime == null) {
            return true; // Allow partial updates
        }
        return startTime.isBefore(endTime);
    }

    public boolean isValidRecurrence() {
        if (recurrencePattern == null || recurrencePattern == RecurrencePattern.NONE) {
            return true;
        }
        
        if (recurrencePattern == RecurrencePattern.CUSTOM && 
            (recurrenceDaysOfWeek == null || recurrenceDaysOfWeek.isEmpty())) {
            return false;
        }
        
        return recurrenceEndDate == null || availabilityDate == null || 
               recurrenceEndDate.isAfter(availabilityDate);
    }

    @Override
    public String toString() {
        return "UpdateAvailabilityRequest{" +
                "availabilityDate=" + availabilityDate +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", slotDurationMinutes=" + slotDurationMinutes +
                ", appointmentType=" + appointmentType +
                ", isActive=" + isActive +
                ", updateRecurring=" + updateRecurring +
                ", regenerateSlots=" + regenerateSlots +
                '}';
    }
} 