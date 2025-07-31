package com.healthfirst.repository;

import com.healthfirst.entity.ProviderAvailability;
import com.healthfirst.enums.AppointmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProviderAvailabilityRepository extends JpaRepository<ProviderAvailability, UUID> {

    /**
     * Find all active availability slots for a provider on a specific date
     */
    @Query("SELECT pa FROM ProviderAvailability pa WHERE pa.provider.id = :providerId " +
           "AND pa.availabilityDate = :date AND pa.isActive = true")
    List<ProviderAvailability> findByProviderIdAndDateAndIsActive(
            @Param("providerId") UUID providerId, 
            @Param("date") LocalDate date);

    /**
     * Find all active availability slots for a provider within a date range
     */
    @Query("SELECT pa FROM ProviderAvailability pa WHERE pa.provider.id = :providerId " +
           "AND pa.availabilityDate BETWEEN :startDate AND :endDate AND pa.isActive = true " +
           "ORDER BY pa.availabilityDate, pa.startTime")
    List<ProviderAvailability> findByProviderIdAndDateRangeAndIsActive(
            @Param("providerId") UUID providerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find overlapping availability slots for a provider on a specific date
     */
    @Query("SELECT pa FROM ProviderAvailability pa WHERE pa.provider.id = :providerId " +
           "AND pa.availabilityDate = :date AND pa.isActive = true " +
           "AND ((pa.startTime < :endTime AND pa.endTime > :startTime))")
    List<ProviderAvailability> findOverlappingSlots(
            @Param("providerId") UUID providerId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    /**
     * Find availability by provider and appointment type
     */
    @Query("SELECT pa FROM ProviderAvailability pa WHERE pa.provider.id = :providerId " +
           "AND pa.appointmentType = :appointmentType AND pa.isActive = true " +
           "AND pa.availabilityDate >= :fromDate " +
           "ORDER BY pa.availabilityDate, pa.startTime")
    List<ProviderAvailability> findByProviderIdAndAppointmentTypeAndFromDate(
            @Param("providerId") UUID providerId,
            @Param("appointmentType") AppointmentType appointmentType,
            @Param("fromDate") LocalDate fromDate);

    /**
     * Find all active availability for multiple providers within date range
     */
    @Query("SELECT pa FROM ProviderAvailability pa WHERE pa.provider.id IN :providerIds " +
           "AND pa.availabilityDate BETWEEN :startDate AND :endDate AND pa.isActive = true " +
           "ORDER BY pa.availabilityDate, pa.startTime")
    List<ProviderAvailability> findByProviderIdsAndDateRange(
            @Param("providerIds") List<UUID> providerIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find availability by appointment type and date range for search
     */
    @Query("SELECT pa FROM ProviderAvailability pa " +
           "WHERE pa.appointmentType = :appointmentType " +
           "AND pa.availabilityDate BETWEEN :startDate AND :endDate " +
           "AND pa.isActive = true " +
           "ORDER BY pa.availabilityDate, pa.startTime")
    List<ProviderAvailability> findByAppointmentTypeAndDateRange(
            @Param("appointmentType") AppointmentType appointmentType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find upcoming availability for a provider (starting from today)
     */
    @Query("SELECT pa FROM ProviderAvailability pa WHERE pa.provider.id = :providerId " +
           "AND pa.availabilityDate >= :fromDate AND pa.isActive = true " +
           "ORDER BY pa.availabilityDate, pa.startTime")
    List<ProviderAvailability> findUpcomingByProviderId(
            @Param("providerId") UUID providerId,
            @Param("fromDate") LocalDate fromDate);

    /**
     * Count active availability slots for a provider
     */
    @Query("SELECT COUNT(pa) FROM ProviderAvailability pa WHERE pa.provider.id = :providerId " +
           "AND pa.isActive = true")
    long countActiveByProviderId(@Param("providerId") UUID providerId);

    /**
     * Find availability with price range filter
     */
    @Query("SELECT pa FROM ProviderAvailability pa " +
           "WHERE pa.price BETWEEN :minPrice AND :maxPrice " +
           "AND pa.availabilityDate BETWEEN :startDate AND :endDate " +
           "AND pa.isActive = true " +
           "ORDER BY pa.price, pa.availabilityDate")
    List<ProviderAvailability> findByPriceRangeAndDateRange(
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find by provider and location
     */
    @Query("SELECT pa FROM ProviderAvailability pa WHERE pa.provider.id = :providerId " +
           "AND pa.location LIKE %:location% AND pa.isActive = true " +
           "AND pa.availabilityDate >= :fromDate " +
           "ORDER BY pa.availabilityDate, pa.startTime")
    List<ProviderAvailability> findByProviderIdAndLocation(
            @Param("providerId") UUID providerId,
            @Param("location") String location,
            @Param("fromDate") LocalDate fromDate);

    /**
     * Check if provider exists and is active
     */
    @Query("SELECT COUNT(pa) > 0 FROM ProviderAvailability pa WHERE pa.provider.id = :providerId " +
           "AND pa.provider.isActive = true")
    boolean existsByActiveProviderId(@Param("providerId") UUID providerId);

    /**
     * Find availability for provider with timezone filter
     */
    @Query("SELECT pa FROM ProviderAvailability pa WHERE pa.provider.id = :providerId " +
           "AND pa.timezone = :timezone AND pa.isActive = true " +
           "AND pa.availabilityDate BETWEEN :startDate AND :endDate " +
           "ORDER BY pa.availabilityDate, pa.startTime")
    List<ProviderAvailability> findByProviderIdAndTimezoneAndDateRange(
            @Param("providerId") UUID providerId,
            @Param("timezone") String timezone,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
} 