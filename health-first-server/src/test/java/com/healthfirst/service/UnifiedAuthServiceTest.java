package com.healthfirst.service;

import com.healthfirst.dto.UnifiedLoginRequest;
import com.healthfirst.dto.UnifiedLoginResponse;
import com.healthfirst.entity.Patient;
import com.healthfirst.entity.Provider;
import com.healthfirst.entity.embedded.PatientAddress;
import com.healthfirst.enums.Gender;
import com.healthfirst.enums.VerificationStatus;
import com.healthfirst.repository.PatientRepository;
import com.healthfirst.repository.ProviderRepository;
import com.healthfirst.util.JwtUtil;
import com.healthfirst.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnifiedAuthServiceTest {

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordUtil passwordUtil;

    @InjectMocks
    private AuthService authService;

    private Provider testProvider;
    private Patient testPatient;
    private PatientAddress testAddress;

    @BeforeEach
    void setUp() {
        // Setup test provider
        testProvider = new Provider();
        testProvider.setId(UUID.randomUUID());
        testProvider.setFirstName("Dr. John");
        testProvider.setLastName("Smith");
        testProvider.setEmail("john.smith@example.com");
        testProvider.setPhoneNumber("+1234567890");
        testProvider.setPasswordHash("$2a$12$hashedPassword");
        testProvider.setSpecialization("Cardiology");
        testProvider.setVerificationStatus(VerificationStatus.VERIFIED);
        testProvider.setIsActive(true);
        testProvider.setFailedLoginAttempts(0);

        // Setup test address
        testAddress = new PatientAddress();
        testAddress.setStreet("123 Test Street");
        testAddress.setCity("Test City");
        testAddress.setState("TS");
        testAddress.setZip("12345");

        // Setup test patient
        testPatient = new Patient();
        testPatient.setId(UUID.randomUUID());
        testPatient.setFirstName("Jane");
        testPatient.setLastName("Doe");
        testPatient.setEmail("jane.doe@example.com");
        testPatient.setPhoneNumber("+1234567891");
        testPatient.setPasswordHash("$2a$12$hashedPassword");
        testPatient.setDateOfBirth(LocalDate.of(1990, 5, 15));
        testPatient.setGender(Gender.FEMALE);
        testPatient.setAddress(testAddress);
        testPatient.setEmailVerified(true);
        testPatient.setPhoneVerified(false);
        testPatient.setIsActive(true);
        testPatient.setFailedLoginAttempts(0);
    }

    @Test
    void testUnifiedLogin_ProviderAutoDetection_Success() {
        // Arrange
        UnifiedLoginRequest request = new UnifiedLoginRequest(
            "john.smith@example.com", "password123", true, null // No userType - should auto-detect
        );

        when(providerRepository.findByEmail("john.smith@example.com")).thenReturn(Optional.of(testProvider));
        when(passwordUtil.verifyPassword("password123", "$2a$12$hashedPassword")).thenReturn(true);
        when(jwtUtil.generateAccessToken(testProvider, true)).thenReturn("provider-access-token");
        when(jwtUtil.generateRefreshToken(testProvider, true)).thenReturn("provider-refresh-token");
        when(jwtUtil.getTokenExpirationInSeconds(true)).thenReturn(86400L);
        when(providerRepository.save(any(Provider.class))).thenReturn(testProvider);

        // Act
        UnifiedLoginResponse response = authService.unifiedLogin(request, "TestAgent", "127.0.0.1");

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("provider-access-token", response.getData().getAccessToken());
        assertEquals("provider-refresh-token", response.getData().getRefreshToken());
        assertEquals("provider", response.getData().getUserType());
        
        assertInstanceOf(UnifiedLoginResponse.ProviderData.class, response.getData().getUser());
        UnifiedLoginResponse.ProviderData providerData = (UnifiedLoginResponse.ProviderData) response.getData().getUser();
        assertEquals("Dr. John", providerData.getFirstName());
        assertEquals("Cardiology", providerData.getSpecialization());

        verify(providerRepository).findByEmail("john.smith@example.com");
        verify(tokenService).createRefreshToken(any(), anyString(), anyBoolean(), anyString(), anyString());
    }

    @Test
    void testUnifiedLogin_PatientAutoDetection_Success() {
        // Arrange
        UnifiedLoginRequest request = new UnifiedLoginRequest(
            "jane.doe@example.com", "password123", false, null // No userType - should auto-detect
        );

        when(providerRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.empty());
        when(patientRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(testPatient));
        when(passwordUtil.verifyPassword("password123", "$2a$12$hashedPassword")).thenReturn(true);
        when(jwtUtil.generateAccessToken(testPatient)).thenReturn("patient-access-token");
        when(jwtUtil.getPatientTokenExpirationInSeconds()).thenReturn(1800L);
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // Act
        UnifiedLoginResponse response = authService.unifiedLogin(request, "TestAgent", "127.0.0.1");

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("patient-access-token", response.getData().getAccessToken());
        assertNull(response.getData().getRefreshToken()); // Patients don't get refresh tokens
        assertEquals("patient", response.getData().getUserType());
        
        assertInstanceOf(UnifiedLoginResponse.PatientData.class, response.getData().getUser());
        UnifiedLoginResponse.PatientData patientData = (UnifiedLoginResponse.PatientData) response.getData().getUser();
        assertEquals("Jane", patientData.getFirstName());
        assertEquals(Gender.FEMALE, patientData.getGender());

        verify(providerRepository).findByEmail("jane.doe@example.com");
        verify(patientRepository).findByEmail("jane.doe@example.com");
    }

    @Test
    void testUnifiedLogin_ExplicitProviderType_Success() {
        // Arrange
        UnifiedLoginRequest request = new UnifiedLoginRequest(
            "john.smith@example.com", "password123", true, "provider" // Explicit userType
        );

        when(providerRepository.findByEmail("john.smith@example.com")).thenReturn(Optional.of(testProvider));
        when(passwordUtil.verifyPassword("password123", "$2a$12$hashedPassword")).thenReturn(true);
        when(jwtUtil.generateAccessToken(testProvider, true)).thenReturn("provider-access-token");
        when(jwtUtil.generateRefreshToken(testProvider, true)).thenReturn("provider-refresh-token");
        when(jwtUtil.getTokenExpirationInSeconds(true)).thenReturn(86400L);
        when(providerRepository.save(any(Provider.class))).thenReturn(testProvider);

        // Act
        UnifiedLoginResponse response = authService.unifiedLogin(request, "TestAgent", "127.0.0.1");

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("provider", response.getData().getUserType());
        
        // Should not check patient repository when userType is explicitly set
        verify(providerRepository).findByEmail("john.smith@example.com");
        verify(patientRepository, never()).findByEmail(anyString());
    }

    @Test
    void testUnifiedLogin_ExplicitPatientType_Success() {
        // Arrange
        UnifiedLoginRequest request = new UnifiedLoginRequest(
            "jane.doe@example.com", "password123", false, "patient" // Explicit userType
        );

        when(patientRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(testPatient));
        when(passwordUtil.verifyPassword("password123", "$2a$12$hashedPassword")).thenReturn(true);
        when(jwtUtil.generateAccessToken(testPatient)).thenReturn("patient-access-token");
        when(jwtUtil.getPatientTokenExpirationInSeconds()).thenReturn(1800L);
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // Act
        UnifiedLoginResponse response = authService.unifiedLogin(request, "TestAgent", "127.0.0.1");

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("patient", response.getData().getUserType());
        
        // Should directly call patient login without checking provider repository
        verify(patientRepository).findByEmail("jane.doe@example.com");
        verify(providerRepository, never()).findByEmail(anyString());
    }

    @Test
    void testUnifiedLogin_UserNotFound() {
        // Arrange
        UnifiedLoginRequest request = new UnifiedLoginRequest(
            "nonexistent@example.com", "password123", false, null
        );

        when(providerRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        when(patientRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        UnifiedLoginResponse response = authService.unifiedLogin(request, "TestAgent", "127.0.0.1");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("INVALID_CREDENTIALS", response.getErrorCode());
        assertEquals("Invalid credentials", response.getMessage());
        assertNull(response.getData());

        verify(providerRepository).findByEmail("nonexistent@example.com");
        verify(patientRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void testUnifiedLogin_InvalidUserType() {
        // Arrange
        UnifiedLoginRequest request = new UnifiedLoginRequest(
            "test@example.com", "password123", false, "invalid_type"
        );

        // Act
        UnifiedLoginResponse response = authService.unifiedLogin(request, "TestAgent", "127.0.0.1");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("INVALID_USER_TYPE", response.getErrorCode());
        assertEquals("Invalid user type", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testUnifiedLogin_ProviderAccountLocked() {
        // Arrange
        testProvider.setAccountLockedUntil(java.time.LocalDateTime.now().plusMinutes(30));
        
        UnifiedLoginRequest request = new UnifiedLoginRequest(
            "john.smith@example.com", "password123", true, "provider"
        );

        when(providerRepository.findByEmail("john.smith@example.com")).thenReturn(Optional.of(testProvider));

        // Act
        UnifiedLoginResponse response = authService.unifiedLogin(request, "TestAgent", "127.0.0.1");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("ACCOUNT_LOCKED", response.getErrorCode());
        assertTrue(response.getMessage().contains("temporarily locked"));
        assertNull(response.getData());
    }

    @Test
    void testUnifiedLogin_PatientInactiveAccount() {
        // Arrange
        testPatient.setIsActive(false);
        
        UnifiedLoginRequest request = new UnifiedLoginRequest(
            "jane.doe@example.com", "password123", false, "patient"
        );

        when(patientRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(testPatient));

        // Act
        UnifiedLoginResponse response = authService.unifiedLogin(request, "TestAgent", "127.0.0.1");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("ACCOUNT_INACTIVE", response.getErrorCode());
        assertEquals("Account is inactive", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testUnifiedLogin_InvalidPassword() {
        // Arrange
        UnifiedLoginRequest request = new UnifiedLoginRequest(
            "john.smith@example.com", "wrongpassword", true, "provider"
        );

        when(providerRepository.findByEmail("john.smith@example.com")).thenReturn(Optional.of(testProvider));
        when(passwordUtil.verifyPassword("wrongpassword", "$2a$12$hashedPassword")).thenReturn(false);
        when(providerRepository.save(any(Provider.class))).thenReturn(testProvider);

        // Act
        UnifiedLoginResponse response = authService.unifiedLogin(request, "TestAgent", "127.0.0.1");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("INVALID_CREDENTIALS", response.getErrorCode());
        assertEquals("Invalid credentials", response.getMessage());
        assertNull(response.getData());

        // Verify failed attempt was recorded
        verify(providerRepository).save(argThat(provider -> 
            provider.getFailedLoginAttempts() == 1
        ));
    }

    @Test
    void testUnifiedLogin_DatabaseError() {
        // Arrange
        UnifiedLoginRequest request = new UnifiedLoginRequest(
            "test@example.com", "password123", false, null
        );

        when(providerRepository.findByEmail("test@example.com")).thenThrow(new RuntimeException("Database error"));

        // Act
        UnifiedLoginResponse response = authService.unifiedLogin(request, "TestAgent", "127.0.0.1");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("LOGIN_ERROR", response.getErrorCode());
        assertTrue(response.getMessage().contains("Login failed"));
        assertNull(response.getData());
    }

    @Test
    void testUnifiedLogin_ProviderByPhoneNumber() {
        // Arrange
        UnifiedLoginRequest request = new UnifiedLoginRequest(
            "+1234567890", "password123", true, null // Phone number instead of email
        );

        when(providerRepository.findByEmail("+1234567890")).thenReturn(Optional.empty());
        when(providerRepository.findByPhoneNumber("+1234567890")).thenReturn(Optional.of(testProvider));
        when(passwordUtil.verifyPassword("password123", "$2a$12$hashedPassword")).thenReturn(true);
        when(jwtUtil.generateAccessToken(testProvider, true)).thenReturn("provider-access-token");
        when(jwtUtil.generateRefreshToken(testProvider, true)).thenReturn("provider-refresh-token");
        when(jwtUtil.getTokenExpirationInSeconds(true)).thenReturn(86400L);
        when(providerRepository.save(any(Provider.class))).thenReturn(testProvider);

        // Act
        UnifiedLoginResponse response = authService.unifiedLogin(request, "TestAgent", "127.0.0.1");

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("provider", response.getData().getUserType());
        
        verify(providerRepository).findByEmail("+1234567890");
        verify(providerRepository).findByPhoneNumber("+1234567890");
    }

    @Test
    void testAutoDetectUserType_ProviderFound() {
        // This test verifies the private detectUserType method indirectly
        
        // Arrange
        when(providerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testProvider));

        UnifiedLoginRequest request = new UnifiedLoginRequest(
            "test@example.com", "password123", true, null
        );

        when(passwordUtil.verifyPassword("password123", "$2a$12$hashedPassword")).thenReturn(true);
        when(jwtUtil.generateAccessToken(testProvider, true)).thenReturn("provider-access-token");
        when(jwtUtil.generateRefreshToken(testProvider, true)).thenReturn("provider-refresh-token");
        when(jwtUtil.getTokenExpirationInSeconds(true)).thenReturn(86400L);
        when(providerRepository.save(any(Provider.class))).thenReturn(testProvider);

        // Act
        UnifiedLoginResponse response = authService.unifiedLogin(request, "TestAgent", "127.0.0.1");

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("provider", response.getData().getUserType());
        
        // Should find provider first and not check patient repository
        verify(providerRepository).findByEmail("test@example.com");
        verify(patientRepository, never()).findByEmail(anyString());
    }

    @Test
    void testAutoDetectUserType_PatientFound() {
        // This test verifies the private detectUserType method indirectly
        
        // Arrange
        when(providerRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(patientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testPatient));

        UnifiedLoginRequest request = new UnifiedLoginRequest(
            "test@example.com", "password123", false, null
        );

        when(passwordUtil.verifyPassword("password123", "$2a$12$hashedPassword")).thenReturn(true);
        when(jwtUtil.generateAccessToken(testPatient)).thenReturn("patient-access-token");
        when(jwtUtil.getPatientTokenExpirationInSeconds()).thenReturn(1800L);
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // Act
        UnifiedLoginResponse response = authService.unifiedLogin(request, "TestAgent", "127.0.0.1");

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("patient", response.getData().getUserType());
        
        // Should check provider first, then patient
        verify(providerRepository).findByEmail("test@example.com");
        verify(patientRepository).findByEmail("test@example.com");
    }
} 