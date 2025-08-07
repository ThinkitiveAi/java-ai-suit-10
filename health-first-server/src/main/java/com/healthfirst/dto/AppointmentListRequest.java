package com.healthfirst.dto;

import com.healthfirst.enums.AppointmentType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.UUID;

public class AppointmentListRequest {

    private UUID patientId;  // For patient-specific appointments
    private UUID providerId; // For provider-specific appointments
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    @Pattern(regexp = "^(upcoming|past|cancelled|all)$", message = "Invalid filter type. Must be one of: upcoming, past, cancelled, all")
    private String filterType = "all";
    
    private AppointmentType appointmentType;
    
    @Pattern(regexp = "^(confirmed|unconfirmed|all)$", message = "Invalid confirmation status. Must be one of: confirmed, unconfirmed, all")
    private String confirmationStatus = "all";
    
    @Pattern(regexp = "^(startTime|endTime|bookedAt|price)$", message = "Invalid sort field")
    private String sortBy = "startTime";
    
    private boolean ascending = true;
    
    @Min(value = 1, message = "Page number must be at least 1")
    private int page = 1;
    
    @Min(value = 10, message = "Page size must be at least 10")
    @Max(value = 100, message = "Page size must not exceed 100")
    private int pageSize = 20;

    // Default constructor
    public AppointmentListRequest() {}

    // Constructor with essential fields
    public AppointmentListRequest(LocalDate startDate, LocalDate endDate, String filterType) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.filterType = filterType;
    }

    // Getters and Setters
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public AppointmentType getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(AppointmentType appointmentType) {
        this.appointmentType = appointmentType;
    }

    public String getConfirmationStatus() {
        return confirmationStatus;
    }

    public void setConfirmationStatus(String confirmationStatus) {
        this.confirmationStatus = confirmationStatus;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    // Helper methods
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return true; // No date filtering
        }
        return !endDate.isBefore(startDate);
    }

    public boolean hasDateFilter() {
        return startDate != null && endDate != null;
    }

    public boolean isPatientSpecific() {
        return patientId != null;
    }

    public boolean isProviderSpecific() {
        return providerId != null;
    }
} 