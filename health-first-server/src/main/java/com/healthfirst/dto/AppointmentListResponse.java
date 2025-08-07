package com.healthfirst.dto;

import com.healthfirst.enums.AppointmentType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AppointmentListResponse {
    private boolean success;
    private String message;
    private String errorCode;
    private PaginationData pagination;
    private List<AppointmentSummary> appointments;

    // Constructors
    public AppointmentListResponse() {}

    public AppointmentListResponse(boolean success, String message, List<AppointmentSummary> appointments, PaginationData pagination) {
        this.success = success;
        this.message = message;
        this.appointments = appointments;
        this.pagination = pagination;
    }

    // Static factory methods
    public static AppointmentListResponse success(String message, List<AppointmentSummary> appointments, PaginationData pagination) {
        return new AppointmentListResponse(true, message, appointments, pagination);
    }

    public static AppointmentListResponse error(String message, String errorCode) {
        AppointmentListResponse response = new AppointmentListResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setErrorCode(errorCode);
        return response;
    }

    // Inner class for pagination data
    public static class PaginationData {
        private int currentPage;
        private int pageSize;
        private long totalItems;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;

        public PaginationData(int currentPage, int pageSize, long totalItems, int totalPages) {
            this.currentPage = currentPage;
            this.pageSize = pageSize;
            this.totalItems = totalItems;
            this.totalPages = totalPages;
            this.hasNext = currentPage < totalPages;
            this.hasPrevious = currentPage > 1;
        }

        // Getters and Setters
        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public long getTotalItems() {
            return totalItems;
        }

        public void setTotalItems(long totalItems) {
            this.totalItems = totalItems;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public boolean isHasNext() {
            return hasNext;
        }

        public void setHasNext(boolean hasNext) {
            this.hasNext = hasNext;
        }

        public boolean isHasPrevious() {
            return hasPrevious;
        }

        public void setHasPrevious(boolean hasPrevious) {
            this.hasPrevious = hasPrevious;
        }
    }

    // Inner class for appointment summary
    public static class AppointmentSummary {
        private UUID appointmentId;
        private UUID providerId;
        private String providerName;
        private String providerSpecialization;
        private String providerImage;
        private UUID patientId;
        private String patientName;
        private String patientImage;
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        private AppointmentType appointmentType;
        private String appointmentStatus;
        private BigDecimal price;
        private String location;
        private String bookingReason;
        private Boolean confirmed;
        private LocalDateTime bookedAt;
        private Boolean isUpcoming;
        private String consultationType;
        private String cancellationReason;
        private LocalDateTime cancelledAt;
        private String patientNotes;
        private String providerNotes;

        // Default constructor
        public AppointmentSummary() {}

        // Constructor with essential fields
        public AppointmentSummary(UUID appointmentId, UUID providerId, String providerName,
                                UUID patientId, String patientName, LocalDateTime startDateTime,
                                LocalDateTime endDateTime, AppointmentType appointmentType,
                                String appointmentStatus, Boolean confirmed) {
            this.appointmentId = appointmentId;
            this.providerId = providerId;
            this.providerName = providerName;
            this.patientId = patientId;
            this.patientName = patientName;
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
            this.appointmentType = appointmentType;
            this.appointmentStatus = appointmentStatus;
            this.confirmed = confirmed;
            this.isUpcoming = startDateTime.isAfter(LocalDateTime.now());
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

        public String getProviderSpecialization() {
            return providerSpecialization;
        }

        public void setProviderSpecialization(String providerSpecialization) {
            this.providerSpecialization = providerSpecialization;
        }

        public String getProviderImage() {
            return providerImage;
        }

        public void setProviderImage(String providerImage) {
            this.providerImage = providerImage;
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

        public String getPatientImage() {
            return patientImage;
        }

        public void setPatientImage(String patientImage) {
            this.patientImage = patientImage;
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

        public String getAppointmentStatus() {
            return appointmentStatus;
        }

        public void setAppointmentStatus(String appointmentStatus) {
            this.appointmentStatus = appointmentStatus;
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

        public Boolean getIsUpcoming() {
            return isUpcoming;
        }

        public void setIsUpcoming(Boolean upcoming) {
            isUpcoming = upcoming;
        }

        public String getConsultationType() {
            return consultationType;
        }

        public void setConsultationType(String consultationType) {
            this.consultationType = consultationType;
        }

        public String getCancellationReason() {
            return cancellationReason;
        }

        public void setCancellationReason(String cancellationReason) {
            this.cancellationReason = cancellationReason;
        }

        public LocalDateTime getCancelledAt() {
            return cancelledAt;
        }

        public void setCancelledAt(LocalDateTime cancelledAt) {
            this.cancelledAt = cancelledAt;
        }

        public String getPatientNotes() {
            return patientNotes;
        }

        public void setPatientNotes(String patientNotes) {
            this.patientNotes = patientNotes;
        }

        public String getProviderNotes() {
            return providerNotes;
        }

        public void setProviderNotes(String providerNotes) {
            this.providerNotes = providerNotes;
        }
    }

    // Getters and Setters for main class
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

    public PaginationData getPagination() {
        return pagination;
    }

    public void setPagination(PaginationData pagination) {
        this.pagination = pagination;
    }

    public List<AppointmentSummary> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<AppointmentSummary> appointments) {
        this.appointments = appointments;
    }
} 