package com.healthfirst.dto;

import com.healthfirst.enums.AppointmentType;
import com.healthfirst.enums.RecurrencePattern;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class AvailabilityResponse {

    private boolean success;
    private String message;
    private String errorCode;
    private AvailabilityData data;

    // Constructors
    public AvailabilityResponse() {}

    public AvailabilityResponse(boolean success, String message, String errorCode, AvailabilityData data) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
        this.data = data;
    }

    // Static factory methods
    public static AvailabilityResponse success(AvailabilityData data) {
        return new AvailabilityResponse(true, "Operation successful", null, data);
    }

    public static AvailabilityResponse success(String message, AvailabilityData data) {
        return new AvailabilityResponse(true, message, null, data);
    }

    public static AvailabilityResponse error(String message, String errorCode) {
        return new AvailabilityResponse(false, message, errorCode, null);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public AvailabilityData getData() {
        return data;
    }

    public void setData(AvailabilityData data) {
        this.data = data;
    }

    // Inner class for availability data
    public static class AvailabilityData {
        private UUID id;
        private UUID providerId;
        private String providerName;
        private String specialization;
        private LocalDate availabilityDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer slotDurationMinutes;
        private AppointmentType appointmentType;
        private String timezone;
        private BigDecimal price;
        private String location;
        private String description;
        private RecurrencePattern recurrencePattern;
        private LocalDate recurrenceEndDate;
        private List<Integer> recurrenceDaysOfWeek;
        private Boolean isActive;
        private Integer maxConsecutiveSlots;
        private Integer bufferTimeMinutes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<AppointmentSlotData> appointmentSlots;

        public AvailabilityData() {}

        public AvailabilityData(UUID id, UUID providerId, String providerName, String specialization,
                               LocalDate availabilityDate, LocalTime startTime, LocalTime endTime,
                               Integer slotDurationMinutes, AppointmentType appointmentType, String timezone) {
            this.id = id;
            this.providerId = providerId;
            this.providerName = providerName;
            this.specialization = specialization;
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

        public UUID getProviderId() {
            return providerId;
        }

        public void setProviderId(UUID providerId) {
            this.providerId = providerId;
        }

        public String getProviderName() {
            return providerName;
        }

        public void setProviderName(String providerName) {
            this.providerName = providerName;
        }

        public String getSpecialization() {
            return specialization;
        }

        public void setSpecialization(String specialization) {
            this.specialization = specialization;
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

        public List<Integer> getRecurrenceDaysOfWeek() {
            return recurrenceDaysOfWeek;
        }

        public void setRecurrenceDaysOfWeek(List<Integer> recurrenceDaysOfWeek) {
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

        public List<AppointmentSlotData> getAppointmentSlots() {
            return appointmentSlots;
        }

        public void setAppointmentSlots(List<AppointmentSlotData> appointmentSlots) {
            this.appointmentSlots = appointmentSlots;
        }
    }

    // Inner class for appointment slot data
    public static class AppointmentSlotData {
        private UUID id;
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        private AppointmentType appointmentType;
        private Boolean isBooked;
        private Boolean isActive;
        private BigDecimal price;
        private String location;
        private UUID patientId; // Only if booked and user has permission to see
        private String patientName; // Only if booked and user has permission to see
        private Boolean bookingConfirmed;

        public AppointmentSlotData() {}

        public AppointmentSlotData(UUID id, LocalDateTime startDateTime, LocalDateTime endDateTime,
                                  AppointmentType appointmentType, Boolean isBooked, Boolean isActive) {
            this.id = id;
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
            this.appointmentType = appointmentType;
            this.isBooked = isBooked;
            this.isActive = isActive;
        }

        // Getters and Setters
        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
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

        public UUID getPatientId() {
            return patientId;
        }

        public void setPatientId(UUID patientId) {
            this.patientId = patientId;
        }

        public String getPatientName() {
            return patientName;
        }

        public void setPatientName(String patientName) {
            this.patientName = patientName;
        }

        public Boolean getBookingConfirmed() {
            return bookingConfirmed;
        }

        public void setBookingConfirmed(Boolean bookingConfirmed) {
            this.bookingConfirmed = bookingConfirmed;
        }
    }
} 