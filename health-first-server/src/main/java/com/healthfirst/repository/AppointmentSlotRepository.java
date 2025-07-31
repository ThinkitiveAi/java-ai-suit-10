package com.healthfirst.repository;

import com.healthfirst.entity.AppointmentSlot;
import com.healthfirst.enums.AppointmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, UUID> {

    /**
     * Find all available slots for a provider within time range
     */
    @Query("SELECT s FROM AppointmentSlot s WHERE s.provider.id = :providerId " +
           "AND s.startDateTime BETWEEN :startTime AND :endTime " +
           "AND s.isBooked = false AND s.isActive = true " +
           "ORDER BY s.startDateTime")
    List<AppointmentSlot> findAvailableSlotsByProviderAndTimeRange(
            @Param("providerId") UUID providerId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Find all slots for a provider within time range (booked and available)
     */
    @Query("SELECT s FROM AppointmentSlot s WHERE s.provider.id = :providerId " +
           "AND s.startDateTime BETWEEN :startTime AND :endTime " +
           "AND s.isActive = true " +
           "ORDER BY s.startDateTime")
    List<AppointmentSlot> findSlotsByProviderAndTimeRange(
            @Param("providerId") UUID providerId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Find overlapping slots for a provider (for conflict detection)
     */
    @Query("SELECT s FROM AppointmentSlot s WHERE s.provider.id = :providerId " +
           "AND s.isActive = true " +
           "AND ((s.startDateTime < :endTime AND s.endDateTime > :startTime))")
    List<AppointmentSlot> findOverlappingSlotsByProvider(
            @Param("providerId") UUID providerId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Find available slots by appointment type and time range
     */
    @Query("SELECT s FROM AppointmentSlot s WHERE s.appointmentType = :appointmentType " +
           "AND s.startDateTime BETWEEN :startTime AND :endTime " +
           "AND s.isBooked = false AND s.isActive = true " +
           "ORDER BY s.startDateTime")
    List<AppointmentSlot> findAvailableSlotsByTypeAndTimeRange(
            @Param("appointmentType") AppointmentType appointmentType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Find all booked slots for a patient
     */
    @Query("SELECT s FROM AppointmentSlot s WHERE s.patient.id = :patientId " +
           "AND s.isBooked = true AND s.isActive = true " +
           "ORDER BY s.startDateTime")
    List<AppointmentSlot> findBookedSlotsByPatient(@Param("patientId") UUID patientId);

    /**
     * Find upcoming booked slots for a patient
     */
    @Query("SELECT s FROM AppointmentSlot s WHERE s.patient.id = :patientId " +
           "AND s.isBooked = true AND s.isActive = true " +
           "AND s.startDateTime > :currentTime " +
           "ORDER BY s.startDateTime")
    List<AppointmentSlot> findUpcomingBookedSlotsByPatient(
            @Param("patientId") UUID patientId,
            @Param("currentTime") LocalDateTime currentTime);

    /**
     * Find upcoming booked slots for a provider
     */
    @Query("SELECT s FROM AppointmentSlot s WHERE s.provider.id = :providerId " +
           "AND s.isBooked = true AND s.isActive = true " +
           "AND s.startDateTime > :currentTime " +
           "ORDER BY s.startDateTime")
    List<AppointmentSlot> findUpcomingBookedSlotsByProvider(
            @Param("providerId") UUID providerId,
            @Param("currentTime") LocalDateTime currentTime);

    /**
     * Find slots by provider availability
     */
    @Query("SELECT s FROM AppointmentSlot s WHERE s.providerAvailability.id = :availabilityId " +
           "AND s.isActive = true " +
           "ORDER BY s.startDateTime")
    List<AppointmentSlot> findSlotsByProviderAvailability(@Param("availabilityId") UUID availabilityId);

    /**
     * Find available slots by multiple providers
     */
    @Query("SELECT s FROM AppointmentSlot s WHERE s.provider.id IN :providerIds " +
           "AND s.startDateTime BETWEEN :startTime AND :endTime " +
           "AND s.isBooked = false AND s.isActive = true " +
           "ORDER BY s.startDateTime")
    List<AppointmentSlot> findAvailableSlotsByProvidersAndTimeRange(
            @Param("providerIds") List<UUID> providerIds,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Find available slots with price range filter
     */
    @Query("SELECT s FROM AppointmentSlot s WHERE s.price BETWEEN :minPrice AND :maxPrice " +
           "AND s.startDateTime BETWEEN :startTime AND :endTime " +
           "AND s.isBooked = false AND s.isActive = true " +
           "ORDER BY s.price, s.startDateTime")
    List<AppointmentSlot> findAvailableSlotsByPriceRange(
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Count available slots for a provider
     */
    @Query("SELECT COUNT(s) FROM AppointmentSlot s WHERE s.provider.id = :providerId " +
           "AND s.isBooked = false AND s.isActive = true " +
           "AND s.startDateTime > :currentTime")
    long countAvailableSlotsByProvider(
            @Param("providerId") UUID providerId,
            @Param("currentTime") LocalDateTime currentTime);

    /**
     * Count booked slots for a provider
     */
    @Query("SELECT COUNT(s) FROM AppointmentSlot s WHERE s.provider.id = :providerId " +
           "AND s.isBooked = true AND s.isActive = true")
    long countBookedSlotsByProvider(@Param("providerId") UUID providerId);

    /**
     * Find slots by location pattern
     */
    @Query("SELECT s FROM AppointmentSlot s WHERE s.location LIKE %:location% " +
           "AND s.startDateTime BETWEEN :startTime AND :endTime " +
           "AND s.isBooked = false AND s.isActive = true " +
           "ORDER BY s.startDateTime")
    List<AppointmentSlot> findAvailableSlotsByLocationPattern(
            @Param("location") String location,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Check if slot has time conflict with existing slots for provider
     */
    @Query("SELECT COUNT(s) > 0 FROM AppointmentSlot s WHERE s.provider.id = :providerId " +
           "AND s.id != :excludeSlotId AND s.isActive = true " +
           "AND ((s.startDateTime < :endTime AND s.endDateTime > :startTime))")
    boolean hasTimeConflictForProvider(
            @Param("providerId") UUID providerId,
            @Param("excludeSlotId") UUID excludeSlotId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Find slots that are expiring soon (for cleanup or reminders)
     */
    @Query("SELECT s FROM AppointmentSlot s WHERE s.startDateTime BETWEEN :currentTime AND :expiryTime " +
           "AND s.isBooked = false AND s.isActive = true")
    List<AppointmentSlot> findExpiringSoonSlots(
            @Param("currentTime") LocalDateTime currentTime,
            @Param("expiryTime") LocalDateTime expiryTime);
} 