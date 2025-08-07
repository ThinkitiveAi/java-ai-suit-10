package com.healthfirst.dto;

import com.healthfirst.enums.AppointmentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class AppointmentBookingResponse {

    private boolean success;
    private String message;
    private String errorCode;
    private AppointmentData data;

    // Constructors
    public AppointmentBookingResponse() {}

    public AppointmentBookingResponse(boolean success, String message, AppointmentData data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public AppointmentBookingResponse(boolean success, String message, String errorCode) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
    }

    // Static factory methods
    public static AppointmentBookingResponse success(String message, AppointmentData data) {
        return new AppointmentBookingResponse(true, message, data);
    }

    public static AppointmentBookingResponse error(String message, String errorCode) {
        return new AppointmentBookingResponse(false, message, errorCode);
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

    public AppointmentData getData() {
        return data;
    }

    public void setData(AppointmentData data) {
        this.data = data;
    }

    // Inner class for appointment data
    public static class AppointmentData {
        private UUID appointmentId;
        private UUID providerId;
        private String providerName;
        private UUID patientId;
        private String patientName;
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        private AppointmentType appointmentType;
        private BigDecimal price;
        private String location;
        private String bookingReason;
        private Boolean confirmed;
        private LocalDateTime bookedAt;

        // Default constructor
        public AppointmentData() {}

        // Constructor
        public AppointmentData(UUID appointmentId, UUID providerId, String providerName,
                             UUID patientId, String patientName, LocalDateTime startDateTime,
                             LocalDateTime endDateTime, AppointmentType appointmentType,
                             BigDecimal price, String location, String bookingReason,
                             Boolean confirmed, LocalDateTime bookedAt) {
            this.appointmentId = appointmentId;
            this.providerId = providerId;
            this.providerName = providerName;
            this.patientId = patientId;
            this.patientName = patientName;
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
            this.appointmentType = appointmentType;
            this.price = price;
            this.location = location;
            this.bookingReason = bookingReason;
            this.confirmed = confirmed;
            this.bookedAt = bookedAt;
        }

        // Getters and Setters
        public UUID getAppointmentId() {
            return appointmentId;
        }

        public void setAppointmentId(UUID appointmentId) {
            this.appointmentId = appointmentId;
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

        public Boolean getConfirmed() {
            return confirmed;
        }

        public void setConfirmed(Boolean confirmed) {
            this.confirmed = confirmed;
        }

        public LocalDateTime getBookedAt() {
            return bookedAt;
        }

        public void setBookedAt(LocalDateTime bookedAt) {
            this.bookedAt = bookedAt;
        }
    }
} 