package com.healthfirst.dto;

import com.healthfirst.enums.AppointmentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class AvailabilitySearchRequest {

    // Date and time filters
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime preferredStartTime;
    private LocalTime preferredEndTime;

    // Provider filters
    private List<UUID> providerIds;
    private String specialization;
    private String location;
    private String timezone;

    // Appointment filters
    private AppointmentType appointmentType;
    private Integer minSlotDurationMinutes;
    private Integer maxSlotDurationMinutes;

    // Price filters
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    // Availability filters
    private Boolean availableOnly = true; // Only show available slots by default
    private Integer maxResults = 50;
    private String sortBy = "startTime"; // startTime, price, distance
    private Boolean ascending = true;

    // Location/proximity filters (future enhancement)
    private String proximityLocation;
    private Integer maxDistanceKm;

    // Insurance filters (future enhancement)
    private List<String> acceptedInsurance;

    // Constructors
    public AvailabilitySearchRequest() {}

    public AvailabilitySearchRequest(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
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

    public LocalTime getPreferredStartTime() {
        return preferredStartTime;
    }

    public void setPreferredStartTime(LocalTime preferredStartTime) {
        this.preferredStartTime = preferredStartTime;
    }

    public LocalTime getPreferredEndTime() {
        return preferredEndTime;
    }

    public void setPreferredEndTime(LocalTime preferredEndTime) {
        this.preferredEndTime = preferredEndTime;
    }

    public List<UUID> getProviderIds() {
        return providerIds;
    }

    public void setProviderIds(List<UUID> providerIds) {
        this.providerIds = providerIds;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public AppointmentType getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(AppointmentType appointmentType) {
        this.appointmentType = appointmentType;
    }

    public Integer getMinSlotDurationMinutes() {
        return minSlotDurationMinutes;
    }

    public void setMinSlotDurationMinutes(Integer minSlotDurationMinutes) {
        this.minSlotDurationMinutes = minSlotDurationMinutes;
    }

    public Integer getMaxSlotDurationMinutes() {
        return maxSlotDurationMinutes;
    }

    public void setMaxSlotDurationMinutes(Integer maxSlotDurationMinutes) {
        this.maxSlotDurationMinutes = maxSlotDurationMinutes;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Boolean getAvailableOnly() {
        return availableOnly;
    }

    public void setAvailableOnly(Boolean availableOnly) {
        this.availableOnly = availableOnly;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public Boolean getAscending() {
        return ascending;
    }

    public void setAscending(Boolean ascending) {
        this.ascending = ascending;
    }

    public String getProximityLocation() {
        return proximityLocation;
    }

    public void setProximityLocation(String proximityLocation) {
        this.proximityLocation = proximityLocation;
    }

    public Integer getMaxDistanceKm() {
        return maxDistanceKm;
    }

    public void setMaxDistanceKm(Integer maxDistanceKm) {
        this.maxDistanceKm = maxDistanceKm;
    }

    public List<String> getAcceptedInsurance() {
        return acceptedInsurance;
    }

    public void setAcceptedInsurance(List<String> acceptedInsurance) {
        this.acceptedInsurance = acceptedInsurance;
    }

    // Validation helpers
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return false;
        }
        return !startDate.isAfter(endDate);
    }

    public boolean isValidTimeRange() {
        if (preferredStartTime == null || preferredEndTime == null) {
            return true; // Optional filters
        }
        return !preferredStartTime.isAfter(preferredEndTime);
    }

    public boolean isValidPriceRange() {
        if (minPrice == null || maxPrice == null) {
            return true; // Optional filters
        }
        return minPrice.compareTo(maxPrice) <= 0;
    }

    public boolean isValidSlotDurationRange() {
        if (minSlotDurationMinutes == null || maxSlotDurationMinutes == null) {
            return true; // Optional filters
        }
        return minSlotDurationMinutes <= maxSlotDurationMinutes;
    }

    @Override
    public String toString() {
        return "AvailabilitySearchRequest{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                ", specialization='" + specialization + '\'' +
                ", appointmentType=" + appointmentType +
                ", location='" + location + '\'' +
                ", availableOnly=" + availableOnly +
                ", maxResults=" + maxResults +
                '}';
    }
} 