package com.healthfirst.service;

import com.healthfirst.dto.*;
import com.healthfirst.entity.*;
import com.healthfirst.enums.*;
import com.healthfirst.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderAvailabilityServiceTest {

    @Mock
    private ProviderAvailabilityRepository availabilityRepository;

    @Mock
    private AppointmentSlotRepository slotRepository;

    @Mock
    private ProviderRepository providerRepository;

    @InjectMocks
    private ProviderAvailabilityService availabilityService;

    private Provider testProvider;
    private ProviderAvailability testAvailability;
    private CreateAvailabilityRequest testCreateRequest;
    private UpdateAvailabilityRequest testUpdateRequest;

    @BeforeEach
    void setUp() {
        // Setup test provider
        testProvider = new Provider();
        testProvider.setId(UUID.randomUUID());
        testProvider.setFirstName("Dr. Jane");
        testProvider.setLastName("Smith");
        testProvider.setEmail("jane.smith@healthcare.com");
        testProvider.setSpecialization("Cardiology");
        testProvider.setIsActive(true);

        // Setup test availability
        testAvailability = new ProviderAvailability(
                testProvider,
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                30,
                AppointmentType.CONSULTATION,
                "UTC"
        );
        testAvailability.setId(UUID.randomUUID());
        testAvailability.setPrice(new BigDecimal("100.00"));
        testAvailability.setLocation("Main Clinic");

        // Setup test create request
        testCreateRequest = new CreateAvailabilityRequest(
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                30,
                AppointmentType.CONSULTATION,
                "UTC"
        );
        testCreateRequest.setPrice(new BigDecimal("100.00"));
        testCreateRequest.setLocation("Main Clinic");

        // Setup test update request
        testUpdateRequest = new UpdateAvailabilityRequest();
        testUpdateRequest.setPrice(new BigDecimal("120.00"));
        testUpdateRequest.setDescription("Updated availability");
    }

    @Test
    void testCreateAvailability_Success() {
        // Arrange
        when(providerRepository.findById(testProvider.getId())).thenReturn(Optional.of(testProvider));
        when(availabilityRepository.findOverlappingSlots(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(availabilityRepository.saveAll(anyList())).thenReturn(List.of(testAvailability));
        when(slotRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // Act
        AvailabilityResponse response = availabilityService.createAvailability(testProvider.getId(), testCreateRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Availability created successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(testProvider.getId(), response.getData().getProviderId());

        verify(providerRepository).findById(testProvider.getId());
        verify(availabilityRepository).findOverlappingSlots(any(), any(), any(), any());
        verify(availabilityRepository).saveAll(anyList());
        verify(slotRepository).saveAll(anyList());
    }

    @Test
    void testCreateAvailability_ProviderNotFound() {
        // Arrange
        UUID nonExistentProviderId = UUID.randomUUID();
        when(providerRepository.findById(nonExistentProviderId)).thenReturn(Optional.empty());

        // Act
        AvailabilityResponse response = availabilityService.createAvailability(nonExistentProviderId, testCreateRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Provider not found"));
        assertEquals("CREATE_ERROR", response.getErrorCode());

        verify(providerRepository).findById(nonExistentProviderId);
        verifyNoInteractions(availabilityRepository);
    }

    @Test
    void testCreateAvailability_InactiveProvider() {
        // Arrange
        testProvider.setIsActive(false);
        when(providerRepository.findById(testProvider.getId())).thenReturn(Optional.of(testProvider));

        // Act
        AvailabilityResponse response = availabilityService.createAvailability(testProvider.getId(), testCreateRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Provider is not active"));
        assertEquals("CREATE_ERROR", response.getErrorCode());

        verify(providerRepository).findById(testProvider.getId());
        verifyNoInteractions(availabilityRepository);
    }

    @Test
    void testCreateAvailability_InvalidTimeRange() {
        // Arrange
        testCreateRequest.setStartTime(LocalTime.of(17, 0));
        testCreateRequest.setEndTime(LocalTime.of(9, 0)); // End before start
        when(providerRepository.findById(testProvider.getId())).thenReturn(Optional.of(testProvider));

        // Act
        AvailabilityResponse response = availabilityService.createAvailability(testProvider.getId(), testCreateRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Start time must be before end time"));
        assertEquals("VALIDATION_ERROR", response.getErrorCode());

        verify(providerRepository).findById(testProvider.getId());
        verifyNoInteractions(availabilityRepository);
    }

    @Test
    void testCreateAvailability_PastDate() {
        // Arrange
        testCreateRequest.setAvailabilityDate(LocalDate.now().minusDays(1)); // Past date
        when(providerRepository.findById(testProvider.getId())).thenReturn(Optional.of(testProvider));

        // Act
        AvailabilityResponse response = availabilityService.createAvailability(testProvider.getId(), testCreateRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Availability date cannot be in the past"));
        assertEquals("VALIDATION_ERROR", response.getErrorCode());

        verify(providerRepository).findById(testProvider.getId());
        verifyNoInteractions(availabilityRepository);
    }

    @Test
    void testCreateAvailability_OverlappingSlots() {
        // Arrange
        when(providerRepository.findById(testProvider.getId())).thenReturn(Optional.of(testProvider));
        when(availabilityRepository.findOverlappingSlots(any(), any(), any(), any()))
                .thenReturn(List.of(testAvailability)); // Overlapping availability exists

        // Act
        AvailabilityResponse response = availabilityService.createAvailability(testProvider.getId(), testCreateRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Overlapping availability slots are not allowed"));
        assertEquals("OVERLAP_ERROR", response.getErrorCode());

        verify(providerRepository).findById(testProvider.getId());
        verify(availabilityRepository).findOverlappingSlots(any(), any(), any(), any());
        verify(availabilityRepository, never()).saveAll(anyList());
    }

    @Test
    void testCreateAvailability_RecurringDaily() {
        // Arrange
        testCreateRequest.setRecurrencePattern(RecurrencePattern.DAILY);
        testCreateRequest.setRecurrenceEndDate(LocalDate.now().plusDays(7));
        
        when(providerRepository.findById(testProvider.getId())).thenReturn(Optional.of(testProvider));
        when(availabilityRepository.findOverlappingSlots(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(availabilityRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<ProviderAvailability> availabilities = invocation.getArgument(0);
            return availabilities;
        });
        when(slotRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // Act
        AvailabilityResponse response = availabilityService.createAvailability(testProvider.getId(), testCreateRequest);

        // Assert
        assertTrue(response.isSuccess());
        
        // Verify that multiple availability instances were saved (7 days)
        verify(availabilityRepository).saveAll(argThat(list -> 
            list instanceof List && ((List<?>) list).size() == 7));
    }

    @Test
    void testGetProviderAvailability_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        List<ProviderAvailability> availabilities = List.of(testAvailability);
        
        when(providerRepository.existsById(testProvider.getId())).thenReturn(true);
        when(availabilityRepository.findByProviderIdAndDateRangeAndIsActive(testProvider.getId(), startDate, endDate))
                .thenReturn(availabilities);

        // Act
        List<AvailabilityResponse.AvailabilityData> result = 
                availabilityService.getProviderAvailability(testProvider.getId(), startDate, endDate, false);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProvider.getId(), result.get(0).getProviderId());

        verify(providerRepository).existsById(testProvider.getId());
        verify(availabilityRepository).findByProviderIdAndDateRangeAndIsActive(testProvider.getId(), startDate, endDate);
    }

    @Test
    void testGetProviderAvailability_ProviderNotFound() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        UUID nonExistentProviderId = UUID.randomUUID();
        
        when(providerRepository.existsById(nonExistentProviderId)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
                availabilityService.getProviderAvailability(nonExistentProviderId, startDate, endDate, false));

        verify(providerRepository).existsById(nonExistentProviderId);
        verifyNoInteractions(availabilityRepository);
    }

    @Test
    void testUpdateAvailability_Success() {
        // Arrange
        when(availabilityRepository.findById(testAvailability.getId())).thenReturn(Optional.of(testAvailability));
        when(slotRepository.findSlotsByProviderAvailability(testAvailability.getId())).thenReturn(Collections.emptyList());
        when(availabilityRepository.save(testAvailability)).thenReturn(testAvailability);

        // Act
        AvailabilityResponse response = availabilityService.updateAvailability(
                testProvider.getId(), testAvailability.getId(), testUpdateRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Availability updated successfully", response.getMessage());
        assertEquals(new BigDecimal("120.00"), testAvailability.getPrice());
        assertEquals("Updated availability", testAvailability.getDescription());

        verify(availabilityRepository).findById(testAvailability.getId());
        verify(availabilityRepository).save(testAvailability);
    }

    @Test
    void testUpdateAvailability_NotFound() {
        // Arrange
        UUID nonExistentAvailabilityId = UUID.randomUUID();
        when(availabilityRepository.findById(nonExistentAvailabilityId)).thenReturn(Optional.empty());

        // Act
        AvailabilityResponse response = availabilityService.updateAvailability(
                testProvider.getId(), nonExistentAvailabilityId, testUpdateRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Availability not found"));
        assertEquals("INVALID_REQUEST", response.getErrorCode());

        verify(availabilityRepository).findById(nonExistentAvailabilityId);
        verify(availabilityRepository, never()).save(any());
    }

    @Test
    void testUpdateAvailability_WrongProvider() {
        // Arrange
        UUID wrongProviderId = UUID.randomUUID();
        when(availabilityRepository.findById(testAvailability.getId())).thenReturn(Optional.of(testAvailability));

        // Act
        AvailabilityResponse response = availabilityService.updateAvailability(
                wrongProviderId, testAvailability.getId(), testUpdateRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Availability does not belong to provider"));
        assertEquals("INVALID_REQUEST", response.getErrorCode());

        verify(availabilityRepository).findById(testAvailability.getId());
        verify(availabilityRepository, never()).save(any());
    }

    @Test
    void testUpdateAvailability_HasBookedSlots() {
        // Arrange
        testUpdateRequest.setStartTime(LocalTime.of(10, 0)); // Try to change time
        
        AppointmentSlot bookedSlot = new AppointmentSlot();
        bookedSlot.setIsBooked(true);
        
        when(availabilityRepository.findById(testAvailability.getId())).thenReturn(Optional.of(testAvailability));
        when(slotRepository.findSlotsByProviderAvailability(testAvailability.getId())).thenReturn(List.of(bookedSlot));

        // Act
        AvailabilityResponse response = availabilityService.updateAvailability(
                testProvider.getId(), testAvailability.getId(), testUpdateRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Cannot modify time settings when there are booked appointments"));
        assertEquals("BOOKED_SLOTS_ERROR", response.getErrorCode());

        verify(availabilityRepository).findById(testAvailability.getId());
        verify(slotRepository).findSlotsByProviderAvailability(testAvailability.getId());
        verify(availabilityRepository, never()).save(any());
    }

    @Test
    void testDeleteAvailability_Success() {
        // Arrange
        when(availabilityRepository.findById(testAvailability.getId())).thenReturn(Optional.of(testAvailability));
        when(slotRepository.findSlotsByProviderAvailability(testAvailability.getId())).thenReturn(Collections.emptyList());

        // Act
        AvailabilityResponse response = availabilityService.deleteAvailability(
                testProvider.getId(), testAvailability.getId(), false, "Provider request");

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Availability deleted successfully", response.getMessage());

        verify(availabilityRepository).findById(testAvailability.getId());
        verify(slotRepository).findSlotsByProviderAvailability(testAvailability.getId());
        verify(slotRepository).deleteAll(Collections.emptyList());
        verify(availabilityRepository).delete(testAvailability);
    }

    @Test
    void testDeleteAvailability_HasBookedSlots() {
        // Arrange
        AppointmentSlot bookedSlot = new AppointmentSlot();
        bookedSlot.setIsBooked(true);
        
        when(availabilityRepository.findById(testAvailability.getId())).thenReturn(Optional.of(testAvailability));
        when(slotRepository.findSlotsByProviderAvailability(testAvailability.getId())).thenReturn(List.of(bookedSlot));

        // Act
        AvailabilityResponse response = availabilityService.deleteAvailability(
                testProvider.getId(), testAvailability.getId(), false, "Provider request");

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Cannot delete availability with booked appointments"));
        assertEquals("BOOKED_SLOTS_ERROR", response.getErrorCode());

        verify(availabilityRepository).findById(testAvailability.getId());
        verify(slotRepository).findSlotsByProviderAvailability(testAvailability.getId());
        verify(availabilityRepository, never()).delete(any());
    }

    @Test
    void testSearchAvailableSlots_Success() {
        // Arrange
        AvailabilitySearchRequest searchRequest = new AvailabilitySearchRequest(
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(7));
        searchRequest.setSpecialization("Cardiology");
        searchRequest.setMaxResults(10);

        AppointmentSlot availableSlot = new AppointmentSlot();
        availableSlot.setId(UUID.randomUUID());
        availableSlot.setProvider(testProvider);
        availableSlot.setStartDateTime(LocalDateTime.now().plusDays(1).withHour(10));
        availableSlot.setEndDateTime(LocalDateTime.now().plusDays(1).withHour(11));
        availableSlot.setAppointmentType(AppointmentType.CONSULTATION);
        availableSlot.setIsBooked(false);
        availableSlot.setIsActive(true);

        when(slotRepository.findAvailableSlotsByProvidersAndTimeRange(any(), any(), any()))
                .thenReturn(List.of(availableSlot));

        // Act
        List<AvailabilityResponse.AppointmentSlotData> result = 
                availabilityService.searchAvailableSlots(searchRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(availableSlot.getId(), result.get(0).getId());
        assertFalse(result.get(0).getIsBooked());

        verify(slotRepository).findAvailableSlotsByProvidersAndTimeRange(any(), any(), any());
    }

    @Test
    void testSearchAvailableSlots_InvalidDateRange() {
        // Arrange
        AvailabilitySearchRequest searchRequest = new AvailabilitySearchRequest(
                LocalDate.now().plusDays(7), LocalDate.now().plusDays(1)); // End before start

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
                availabilityService.searchAvailableSlots(searchRequest));

        verifyNoInteractions(slotRepository);
    }

    @Test
    void testSearchAvailableSlots_ByAppointmentType() {
        // Arrange
        AvailabilitySearchRequest searchRequest = new AvailabilitySearchRequest(
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(7));
        searchRequest.setAppointmentType(AppointmentType.CONSULTATION);

        AppointmentSlot consultationSlot = new AppointmentSlot();
        consultationSlot.setId(UUID.randomUUID());
        consultationSlot.setProvider(testProvider);
        consultationSlot.setAppointmentType(AppointmentType.CONSULTATION);
        consultationSlot.setIsBooked(false);
        consultationSlot.setIsActive(true);

        when(slotRepository.findAvailableSlotsByTypeAndTimeRange(eq(AppointmentType.CONSULTATION), any(), any()))
                .thenReturn(List.of(consultationSlot));

        // Act
        List<AvailabilityResponse.AppointmentSlotData> result = 
                availabilityService.searchAvailableSlots(searchRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(AppointmentType.CONSULTATION, result.get(0).getAppointmentType());

        verify(slotRepository).findAvailableSlotsByTypeAndTimeRange(eq(AppointmentType.CONSULTATION), any(), any());
    }

    @Test
    void testSearchAvailableSlots_ByPriceRange() {
        // Arrange
        AvailabilitySearchRequest searchRequest = new AvailabilitySearchRequest(
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(7));
        searchRequest.setMinPrice(new BigDecimal("50.00"));
        searchRequest.setMaxPrice(new BigDecimal("150.00"));

        AppointmentSlot priceFilteredSlot = new AppointmentSlot();
        priceFilteredSlot.setId(UUID.randomUUID());
        priceFilteredSlot.setProvider(testProvider);
        priceFilteredSlot.setPrice(new BigDecimal("100.00"));
        priceFilteredSlot.setIsBooked(false);
        priceFilteredSlot.setIsActive(true);

        when(slotRepository.findAvailableSlotsByPriceRange(
                eq(new BigDecimal("50.00")), eq(new BigDecimal("150.00")), any(), any()))
                .thenReturn(List.of(priceFilteredSlot));

        // Act
        List<AvailabilityResponse.AppointmentSlotData> result = 
                availabilityService.searchAvailableSlots(searchRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(new BigDecimal("100.00"), result.get(0).getPrice());

        verify(slotRepository).findAvailableSlotsByPriceRange(
                eq(new BigDecimal("50.00")), eq(new BigDecimal("150.00")), any(), any());
    }

    @Test
    void testSlotGeneration_TimezoneHandling() {
        // This test would verify that slots are correctly generated in UTC
        // regardless of the provider's timezone setting
        
        // Arrange
        testCreateRequest.setTimezone("America/New_York");
        when(providerRepository.findById(testProvider.getId())).thenReturn(Optional.of(testProvider));
        when(availabilityRepository.findOverlappingSlots(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(availabilityRepository.saveAll(anyList())).thenReturn(List.of(testAvailability));
        when(slotRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // Act
        AvailabilityResponse response = availabilityService.createAvailability(testProvider.getId(), testCreateRequest);

        // Assert
        assertTrue(response.isSuccess());
        verify(slotRepository).saveAll(anyList()); // Verify slots were generated
    }

    @Test
    void testSlotGeneration_BufferTime() {
        // Test that buffer time is properly handled between slots
        
        // Arrange
        testCreateRequest.setBufferTimeMinutes(15);
        when(providerRepository.findById(testProvider.getId())).thenReturn(Optional.of(testProvider));
        when(availabilityRepository.findOverlappingSlots(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(availabilityRepository.saveAll(anyList())).thenReturn(List.of(testAvailability));
        when(slotRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // Act
        AvailabilityResponse response = availabilityService.createAvailability(testProvider.getId(), testCreateRequest);

        // Assert
        assertTrue(response.isSuccess());
        verify(slotRepository).saveAll(anyList());
    }

    @Test
    void testConcurrencyHandling_OverlapDetection() {
        // Test that concurrent creation of overlapping slots is properly handled
        
        // Arrange
        when(providerRepository.findById(testProvider.getId())).thenReturn(Optional.of(testProvider));
        when(availabilityRepository.findOverlappingSlots(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList()) // First check passes
                .thenReturn(List.of(testAvailability)); // Second check (simulating concurrent creation) fails

        // Act
        AvailabilityResponse response = availabilityService.createAvailability(testProvider.getId(), testCreateRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("OVERLAP_ERROR", response.getErrorCode());
    }
} 