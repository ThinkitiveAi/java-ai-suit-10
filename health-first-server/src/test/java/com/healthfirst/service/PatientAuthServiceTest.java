package com.healthfirst.service;

import com.healthfirst.dto.PatientLoginRequest;
import com.healthfirst.dto.PatientLoginResponse;
import com.healthfirst.entity.Patient;
import com.healthfirst.entity.embedded.PatientAddress;
import com.healthfirst.enums.Gender;
import com.healthfirst.repository.PatientRepository;
import com.healthfirst.util.JwtUtil;
import com.healthfirst.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientAuthServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordUtil passwordUtil;

    @InjectMocks
    private AuthService authService;

    private PatientLoginRequest validLoginRequest;
    private Patient testPatient;
    private PatientAddress testAddress;

    @BeforeEach
    void setUp() {
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
        testPatient.setPhoneNumber("+1234567890");
        testPatient.setPasswordHash("$2a$12$hashedPassword");
        testPatient.setDateOfBirth(LocalDate.of(1990, 5, 15));
        testPatient.setGender(Gender.FEMALE);
        testPatient.setAddress(testAddress);
        testPatient.setEmailVerified(true);
        testPatient.setPhoneVerified(false);
        testPatient.setIsActive(true);
        testPatient.setFailedLoginAttempts(0);
        testPatient.setAccountLockedUntil(null);

        // Setup valid login request
        validLoginRequest = new PatientLoginRequest();
        validLoginRequest.setEmail("jane.doe@example.com");
        validLoginRequest.setPassword("SecurePassword123!");
    }

    @Test
    void testLoginPatient_Success() {
        // Arrange
        when(patientRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(testPatient));
        when(passwordUtil.verifyPassword("SecurePassword123!", "$2a$12$hashedPassword")).thenReturn(true);
        when(jwtUtil.generateAccessToken(testPatient)).thenReturn("jwt-access-token");
        when(jwtUtil.getPatientTokenExpirationInSeconds()).thenReturn(1800L);
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // Act
        PatientLoginResponse response = authService.loginPatient(validLoginRequest, "127.0.0.1");

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("jwt-access-token", response.getData().getAccessToken());
        assertEquals(1800L, response.getData().getExpiresIn());
        assertEquals("Bearer", response.getData().getTokenType());
        
        PatientLoginResponse.PatientData patientData = response.getData().getPatient();
        assertEquals(testPatient.getId(), patientData.getId());
        assertEquals("Jane", patientData.getFirstName());
        assertEquals("Doe", patientData.getLastName());
        assertEquals("jane.doe@example.com", patientData.getEmail());
        assertEquals(Gender.FEMALE, patientData.getGender());
        assertTrue(patientData.isEmailVerified());
        assertFalse(patientData.isPhoneVerified());
        assertTrue(patientData.isActive());

        verify(patientRepository).findByEmail("jane.doe@example.com");
        verify(passwordUtil).verifyPassword("SecurePassword123!", "$2a$12$hashedPassword");
        verify(jwtUtil).generateAccessToken(testPatient);
        verify(patientRepository).save(argThat(patient -> 
            patient.getLastSuccessfulLogin() != null &&
            patient.getFailedLoginAttempts() == 0 &&
            patient.getAccountLockedUntil() == null
        ));
    }

    @Test
    void testLoginPatient_InvalidEmail() {
        // Arrange
        when(patientRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        PatientLoginRequest request = new PatientLoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password");

        // Act
        PatientLoginResponse response = authService.loginPatient(request, "127.0.0.1");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("INVALID_CREDENTIALS", response.getErrorCode());
        assertEquals("Invalid credentials", response.getMessage());
        assertNull(response.getData());

        verify(patientRepository).findByEmail("nonexistent@example.com");
        verify(passwordUtil, never()).verifyPassword(anyString(), anyString());
        verify(jwtUtil, never()).generateAccessToken(any(Patient.class));
    }

    @Test
    void testLoginPatient_InvalidPassword() {
        // Arrange
        when(patientRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(testPatient));
        when(passwordUtil.verifyPassword("wrongPassword", "$2a$12$hashedPassword")).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        PatientLoginRequest request = new PatientLoginRequest();
        request.setEmail("jane.doe@example.com");
        request.setPassword("wrongPassword");

        // Act
        PatientLoginResponse response = authService.loginPatient(request, "127.0.0.1");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("INVALID_CREDENTIALS", response.getErrorCode());
        assertEquals("Invalid credentials", response.getMessage());
        assertNull(response.getData());

        verify(patientRepository).findByEmail("jane.doe@example.com");
        verify(passwordUtil).verifyPassword("wrongPassword", "$2a$12$hashedPassword");
        verify(jwtUtil, never()).generateAccessToken(any(Patient.class));
        
        // Verify failed attempt was recorded
        verify(patientRepository).save(argThat(patient -> 
            patient.getFailedLoginAttempts() == 1
        ));
    }

    @Test
    void testLoginPatient_AccountLocked() {
        // Arrange
        testPatient.setAccountLockedUntil(LocalDateTime.now().plusMinutes(30));
        when(patientRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(testPatient));

        // Act
        PatientLoginResponse response = authService.loginPatient(validLoginRequest, "127.0.0.1");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("ACCOUNT_LOCKED", response.getErrorCode());
        assertTrue(response.getMessage().contains("temporarily locked"));
        assertNull(response.getData());

        verify(patientRepository).findByEmail("jane.doe@example.com");
        verify(passwordUtil, never()).verifyPassword(anyString(), anyString());
        verify(jwtUtil, never()).generateAccessToken(any(Patient.class));
    }

    @Test
    void testLoginPatient_InactiveAccount() {
        // Arrange
        testPatient.setIsActive(false);
        when(patientRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(testPatient));

        // Act
        PatientLoginResponse response = authService.loginPatient(validLoginRequest, "127.0.0.1");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("ACCOUNT_INACTIVE", response.getErrorCode());
        assertEquals("Account is inactive", response.getMessage());
        assertNull(response.getData());

        verify(patientRepository).findByEmail("jane.doe@example.com");
        verify(passwordUtil, never()).verifyPassword(anyString(), anyString());
        verify(jwtUtil, never()).generateAccessToken(any(Patient.class));
    }

    @Test
    void testLoginPatient_AccountLockAfterMaxFailedAttempts() {
        // Arrange
        testPatient.setFailedLoginAttempts(4); // One less than max (5)
        when(patientRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(testPatient));
        when(passwordUtil.verifyPassword("wrongPassword", "$2a$12$hashedPassword")).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        PatientLoginRequest request = new PatientLoginRequest();
        request.setEmail("jane.doe@example.com");
        request.setPassword("wrongPassword");

        // Act
        PatientLoginResponse response = authService.loginPatient(request, "127.0.0.1");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("INVALID_CREDENTIALS", response.getErrorCode());

        // Verify account gets locked after reaching max failed attempts
        verify(patientRepository).save(argThat(patient -> 
            patient.getFailedLoginAttempts() == 5 &&
            patient.getAccountLockedUntil() != null
        ));
    }

    @Test
    void testLoginPatient_DatabaseError() {
        // Arrange
        when(patientRepository.findByEmail("jane.doe@example.com")).thenThrow(new RuntimeException("Database error"));

        // Act
        PatientLoginResponse response = authService.loginPatient(validLoginRequest, "127.0.0.1");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("LOGIN_ERROR", response.getErrorCode());
        assertTrue(response.getMessage().contains("Login failed"));
        assertNull(response.getData());
    }

    @Test
    void testIsPatientTokenValid_ValidToken() {
        // Arrange
        String token = "valid-jwt-token";
        when(jwtUtil.validateToken(token)).thenReturn(null); // No exception thrown
        when(jwtUtil.isPatientToken(token)).thenReturn(true);

        // Act
        boolean isValid = authService.isPatientTokenValid(token);

        // Assert
        assertTrue(isValid);
        verify(jwtUtil).validateToken(token);
        verify(jwtUtil).isPatientToken(token);
    }

    @Test
    void testIsPatientTokenValid_InvalidToken() {
        // Arrange
        String token = "invalid-jwt-token";
        when(jwtUtil.validateToken(token)).thenThrow(new RuntimeException("Invalid token"));

        // Act
        boolean isValid = authService.isPatientTokenValid(token);

        // Assert
        assertFalse(isValid);
        verify(jwtUtil).validateToken(token);
        verify(jwtUtil, never()).isPatientToken(token);
    }

    @Test
    void testIsPatientTokenValid_NullToken() {
        // Act
        boolean isValid = authService.isPatientTokenValid(null);

        // Assert
        assertFalse(isValid);
        verify(jwtUtil, never()).validateToken(anyString());
        verify(jwtUtil, never()).isPatientToken(anyString());
    }

    @Test
    void testGetPatientFromToken_Success() {
        // Arrange
        String token = "valid-jwt-token";
        UUID patientId = testPatient.getId();
        
        when(jwtUtil.validateToken(token)).thenReturn(null);
        when(jwtUtil.isPatientToken(token)).thenReturn(true);
        when(jwtUtil.getPatientIdFromToken(token)).thenReturn(patientId);
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient));

        // Act
        Optional<Patient> result = authService.getPatientFromToken(token);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testPatient, result.get());
        verify(jwtUtil).getPatientIdFromToken(token);
        verify(patientRepository).findById(patientId);
    }

    @Test
    void testGetPatientFromToken_InvalidToken() {
        // Arrange
        String token = "invalid-jwt-token";
        when(jwtUtil.validateToken(token)).thenThrow(new RuntimeException("Invalid token"));

        // Act
        Optional<Patient> result = authService.getPatientFromToken(token);

        // Assert
        assertFalse(result.isPresent());
        verify(jwtUtil, never()).getPatientIdFromToken(token);
        verify(patientRepository, never()).findById(any(UUID.class));
    }

    @Test
    void testGetPatientFromToken_PatientNotFound() {
        // Arrange
        String token = "valid-jwt-token";
        UUID patientId = UUID.randomUUID();
        
        when(jwtUtil.validateToken(token)).thenReturn(null);
        when(jwtUtil.isPatientToken(token)).thenReturn(true);
        when(jwtUtil.getPatientIdFromToken(token)).thenReturn(patientId);
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // Act
        Optional<Patient> result = authService.getPatientFromToken(token);

        // Assert
        assertFalse(result.isPresent());
        verify(jwtUtil).getPatientIdFromToken(token);
        verify(patientRepository).findById(patientId);
    }

    @Test
    void testEmailMasking() {
        // This is implicitly tested in the login methods, but we can verify
        // by checking that no exception is thrown and the method completes
        
        // Arrange
        when(patientRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        PatientLoginRequest request = new PatientLoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        // Act & Assert (should not throw exception)
        assertDoesNotThrow(() -> {
            PatientLoginResponse response = authService.loginPatient(request, "127.0.0.1");
            assertFalse(response.isSuccess());
        });
    }
} 