package com.healthfirst.service;

import com.healthfirst.dto.ProviderRegistrationRequest;
import com.healthfirst.dto.ProviderRegistrationResponse;
import com.healthfirst.entity.Provider;
import com.healthfirst.entity.embedded.ClinicAddress;
import com.healthfirst.repository.ProviderRepository;
import com.healthfirst.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderServiceTest {

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private ValidationService validationService;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordUtil passwordUtil;

    @InjectMocks
    private ProviderService providerService;

    private ProviderRegistrationRequest validRequest;
    private Provider savedProvider;

    @BeforeEach
    void setUp() {
        // Setup valid request
        validRequest = new ProviderRegistrationRequest();
        validRequest.setFirstName("John");
        validRequest.setLastName("Doe");
        validRequest.setEmail("john.doe@clinic.com");
        validRequest.setPhoneNumber("+1234567890");
        validRequest.setPassword("SecurePassword123!");
        validRequest.setConfirmPassword("SecurePassword123!");
        validRequest.setSpecialization("Cardiology");
        validRequest.setLicenseNumber("MD123456789");
        validRequest.setYearsOfExperience(10);
        
        ClinicAddress address = new ClinicAddress();
        address.setStreet("123 Medical Center Dr");
        address.setCity("New York");
        address.setState("NY");
        address.setZip("10001");
        validRequest.setClinicAddress(address);

        // Setup saved provider
        savedProvider = new Provider();
        savedProvider.setId(UUID.randomUUID());
        savedProvider.setEmail("john.doe@clinic.com");
        savedProvider.setFirstName("John");
    }

    @Test
    void testRegisterProvider_Success() {
        // Arrange
        when(validationService.validateProviderRegistration(any())).thenReturn(Arrays.asList());
        when(passwordUtil.hashPassword(anyString())).thenReturn("hashedPassword");
        when(providerRepository.save(any(Provider.class))).thenReturn(savedProvider);
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString(), any(UUID.class));

        // Act
        ProviderRegistrationResponse response = providerService.registerProvider(validRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(savedProvider.getEmail(), response.getData().getEmail());
        
        verify(providerRepository).save(any(Provider.class));
        verify(emailService).sendVerificationEmail(anyString(), anyString(), any(UUID.class));
    }

    @Test
    void testRegisterProvider_ValidationErrors() {
        // Arrange
        when(validationService.validateProviderRegistration(any()))
            .thenReturn(Arrays.asList("Email is invalid", "Password is weak"));

        // Act
        ProviderRegistrationResponse response = providerService.registerProvider(validRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Validation failed"));
        
        verify(providerRepository, never()).save(any(Provider.class));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString(), any(UUID.class));
    }

    @Test
    void testRegisterProvider_EmailServiceFailure() {
        // Arrange
        when(validationService.validateProviderRegistration(any())).thenReturn(Arrays.asList());
        when(passwordUtil.hashPassword(anyString())).thenReturn("hashedPassword");
        when(providerRepository.save(any(Provider.class))).thenReturn(savedProvider);
        doThrow(new RuntimeException("Email service down")).when(emailService)
            .sendVerificationEmail(anyString(), anyString(), any(UUID.class));

        // Act
        ProviderRegistrationResponse response = providerService.registerProvider(validRequest);

        // Assert
        assertTrue(response.isSuccess()); // Should still succeed even if email fails
        assertNotNull(response.getData());
        
        verify(providerRepository).save(any(Provider.class));
        verify(emailService).sendVerificationEmail(anyString(), anyString(), any(UUID.class));
    }

    @Test
    void testEmailExists() {
        // Arrange
        when(providerRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act
        boolean exists = providerService.emailExists("test@example.com");

        // Assert
        assertTrue(exists);
        verify(providerRepository).existsByEmail("test@example.com");
    }

    @Test
    void testPhoneExists() {
        // Arrange
        when(providerRepository.existsByPhoneNumber("+1234567890")).thenReturn(false);

        // Act
        boolean exists = providerService.phoneExists("+1234567890");

        // Assert
        assertFalse(exists);
        verify(providerRepository).existsByPhoneNumber("+1234567890");
    }

    @Test
    void testLicenseExists() {
        // Arrange
        when(providerRepository.existsByLicenseNumber("MD123456789")).thenReturn(true);

        // Act
        boolean exists = providerService.licenseExists("MD123456789");

        // Assert
        assertTrue(exists);
        verify(providerRepository).existsByLicenseNumber("MD123456789");
    }

    @Test
    void testGetValidSpecializations() {
        // Arrange
        when(validationService.getValidSpecializations())
            .thenReturn(Arrays.asList("Cardiology", "Dermatology", "Family Medicine"));

        // Act
        var specializations = providerService.getValidSpecializations();

        // Assert
        assertEquals(3, specializations.size());
        assertTrue(specializations.contains("Cardiology"));
        verify(validationService).getValidSpecializations();
    }
} 