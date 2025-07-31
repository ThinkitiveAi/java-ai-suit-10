package com.healthfirst.service;

import com.healthfirst.dto.PatientRegistrationRequest;
import com.healthfirst.dto.PatientRegistrationResponse;
import com.healthfirst.entity.Patient;
import com.healthfirst.entity.embedded.EmergencyContact;
import com.healthfirst.entity.embedded.InsuranceInfo;
import com.healthfirst.entity.embedded.PatientAddress;
import com.healthfirst.enums.Gender;
import com.healthfirst.repository.PatientRepository;
import com.healthfirst.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ValidationService validationService;

    @Mock
    private PasswordUtil passwordUtil;

    @InjectMocks
    private PatientService patientService;

    private PatientRegistrationRequest validRequest;
    private PatientAddress validAddress;
    private EmergencyContact validEmergencyContact;
    private InsuranceInfo validInsuranceInfo;

    @BeforeEach
    void setUp() {
        // Setup valid address
        validAddress = new PatientAddress();
        validAddress.setStreet("123 Main Street");
        validAddress.setCity("New York");
        validAddress.setState("NY");
        validAddress.setZip("10001");

        // Setup valid emergency contact
        validEmergencyContact = new EmergencyContact();
        validEmergencyContact.setName("John Emergency");
        validEmergencyContact.setPhone("+12345678901");
        validEmergencyContact.setRelationship("Father");

        // Setup valid insurance info
        validInsuranceInfo = new InsuranceInfo();
        validInsuranceInfo.setProvider("Blue Cross Blue Shield");
        validInsuranceInfo.setPolicyNumber("BC123456789");

        // Setup valid registration request
        validRequest = new PatientRegistrationRequest();
        validRequest.setFirstName("Jane");
        validRequest.setLastName("Doe");
        validRequest.setEmail("jane.doe@example.com");
        validRequest.setPhoneNumber("+11234567890");
        validRequest.setPassword("SecurePass123!");
        validRequest.setConfirmPassword("SecurePass123!");
        validRequest.setDateOfBirth(LocalDate.of(1990, 5, 15)); // 33 years old
        validRequest.setGender(Gender.FEMALE);
        validRequest.setAddress(validAddress);
        validRequest.setEmergencyContact(validEmergencyContact);
        validRequest.setInsuranceInfo(validInsuranceInfo);
        validRequest.setMedicalHistory(Arrays.asList("Diabetes", "Hypertension"));
    }

    @Test
    void testRegisterPatient_Success() {
        // Arrange
        when(validationService.validatePatientRegistration(validRequest)).thenReturn(List.of());
        when(validationService.sanitizeInput(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordUtil.hashPassword("SecurePass123!")).thenReturn("$2a$12$hashedPassword");
        
        Patient savedPatient = createPatientFromRequest(validRequest, "$2a$12$hashedPassword");
        savedPatient.setId(UUID.randomUUID());
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        // Act
        PatientRegistrationResponse response = patientService.registerPatient(validRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(savedPatient.getId(), response.getData().getId());
        assertEquals(savedPatient.getEmail(), response.getData().getEmail());
        assertFalse(response.getData().isEmailVerified());
        assertFalse(response.getData().isPhoneVerified());
        
        verify(patientRepository).save(any(Patient.class));
        verify(passwordUtil).hashPassword("SecurePass123!");
    }

    @Test
    void testRegisterPatient_ValidationFailed() {
        // Arrange
        List<String> validationErrors = Arrays.asList("Email is required", "Password is too weak");
        when(validationService.validatePatientRegistration(validRequest)).thenReturn(validationErrors);

        // Act
        PatientRegistrationResponse response = patientService.registerPatient(validRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("VALIDATION_ERROR", response.getErrorCode());
        assertTrue(response.getMessage().contains("Validation failed"));
        assertNull(response.getData());
        
        verify(patientRepository, never()).save(any(Patient.class));
        verify(passwordUtil, never()).hashPassword(anyString());
    }

    @Test
    void testRegisterPatient_DatabaseError() {
        // Arrange
        when(validationService.validatePatientRegistration(validRequest)).thenReturn(List.of());
        when(validationService.sanitizeInput(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordUtil.hashPassword("SecurePass123!")).thenReturn("$2a$12$hashedPassword");
        when(patientRepository.save(any(Patient.class))).thenThrow(new RuntimeException("Database error"));

        // Act
        PatientRegistrationResponse response = patientService.registerPatient(validRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("REGISTRATION_ERROR", response.getErrorCode());
        assertTrue(response.getMessage().contains("Registration failed"));
        assertNull(response.getData());
    }

    @Test
    void testRegisterPatient_AgeValidation_Under13() {
        // Arrange
        validRequest.setDateOfBirth(LocalDate.now().minusYears(12)); // 12 years old
        List<String> validationErrors = Arrays.asList("Patient must be at least 13 years old");
        when(validationService.validatePatientRegistration(validRequest)).thenReturn(validationErrors);

        // Act
        PatientRegistrationResponse response = patientService.registerPatient(validRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("VALIDATION_ERROR", response.getErrorCode());
        assertTrue(response.getMessage().contains("must be at least 13 years old"));
        
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void testRegisterPatient_PasswordHashing() {
        // Arrange
        when(validationService.validatePatientRegistration(validRequest)).thenReturn(List.of());
        when(validationService.sanitizeInput(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordUtil.hashPassword("SecurePass123!")).thenReturn("$2a$12$verySecureHashedPassword");
        
        Patient savedPatient = createPatientFromRequest(validRequest, "$2a$12$verySecureHashedPassword");
        savedPatient.setId(UUID.randomUUID());
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        // Act
        PatientRegistrationResponse response = patientService.registerPatient(validRequest);

        // Assert
        assertTrue(response.isSuccess());
        verify(passwordUtil).hashPassword("SecurePass123!");
        
        // Verify that the saved patient has the hashed password
        verify(patientRepository).save(argThat(patient -> 
            "$2a$12$verySecureHashedPassword".equals(patient.getPasswordHash())
        ));
    }

    @Test
    void testRegisterPatient_DataSanitization() {
        // Arrange
        validRequest.setFirstName("<script>alert('xss')</script>Jane");
        validRequest.setLastName("Doe</script>");
        validRequest.setMedicalHistory(Arrays.asList("<script>diabetes</script>", "normal condition"));
        
        when(validationService.validatePatientRegistration(validRequest)).thenReturn(List.of());
        when(validationService.sanitizeInput("<script>alert('xss')</script>Jane")).thenReturn("Jane");
        when(validationService.sanitizeInput("Doe</script>")).thenReturn("Doe");
        when(validationService.sanitizeInput("<script>diabetes</script>")).thenReturn("diabetes");
        when(validationService.sanitizeInput("normal condition")).thenReturn("normal condition");
        when(passwordUtil.hashPassword("SecurePass123!")).thenReturn("$2a$12$hashedPassword");
        
        Patient savedPatient = createPatientFromRequest(validRequest, "$2a$12$hashedPassword");
        savedPatient.setId(UUID.randomUUID());
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        // Act
        PatientRegistrationResponse response = patientService.registerPatient(validRequest);

        // Assert
        assertTrue(response.isSuccess());
        verify(validationService).sanitizeInput("<script>alert('xss')</script>Jane");
        verify(validationService).sanitizeInput("Doe</script>");
        verify(validationService).sanitizeInput("<script>diabetes</script>");
        verify(validationService).sanitizeInput("normal condition");
    }

    @Test
    void testRegisterPatient_OptionalFields() {
        // Arrange - Remove optional fields
        validRequest.setEmergencyContact(null);
        validRequest.setInsuranceInfo(null);
        validRequest.setMedicalHistory(null);
        
        when(validationService.validatePatientRegistration(validRequest)).thenReturn(List.of());
        when(validationService.sanitizeInput(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordUtil.hashPassword("SecurePass123!")).thenReturn("$2a$12$hashedPassword");
        
        Patient savedPatient = createPatientFromRequest(validRequest, "$2a$12$hashedPassword");
        savedPatient.setId(UUID.randomUUID());
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        // Act
        PatientRegistrationResponse response = patientService.registerPatient(validRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        
        // Verify that patient was saved without optional fields
        verify(patientRepository).save(argThat(patient -> 
            patient.getEmergencyContact() == null &&
            patient.getInsuranceInfo() == null &&
            (patient.getMedicalHistory() == null || patient.getMedicalHistory().isEmpty())
        ));
    }

    @Test
    void testIsEmailRegistered_True() {
        // Arrange
        String email = "jane.doe@example.com";
        when(patientRepository.existsByEmail(email.toLowerCase())).thenReturn(true);

        // Act
        boolean result = patientService.isEmailRegistered(email);

        // Assert
        assertTrue(result);
        verify(patientRepository).existsByEmail(email.toLowerCase());
    }

    @Test
    void testIsEmailRegistered_False() {
        // Arrange
        String email = "new.patient@example.com";
        when(patientRepository.existsByEmail(email.toLowerCase())).thenReturn(false);

        // Act
        boolean result = patientService.isEmailRegistered(email);

        // Assert
        assertFalse(result);
        verify(patientRepository).existsByEmail(email.toLowerCase());
    }

    @Test
    void testIsEmailRegistered_NullEmail() {
        // Act
        boolean result = patientService.isEmailRegistered(null);

        // Assert
        assertFalse(result);
        verify(patientRepository, never()).existsByEmail(anyString());
    }

    @Test
    void testIsPhoneRegistered_True() {
        // Arrange
        String phoneNumber = "+11234567890";
        when(patientRepository.existsByPhoneNumber(phoneNumber)).thenReturn(true);

        // Act
        boolean result = patientService.isPhoneRegistered(phoneNumber);

        // Assert
        assertTrue(result);
        verify(patientRepository).existsByPhoneNumber(phoneNumber);
    }

    @Test
    void testIsPhoneRegistered_False() {
        // Arrange
        String phoneNumber = "+19876543210";
        when(patientRepository.existsByPhoneNumber(phoneNumber)).thenReturn(false);

        // Act
        boolean result = patientService.isPhoneRegistered(phoneNumber);

        // Assert
        assertFalse(result);
        verify(patientRepository).existsByPhoneNumber(phoneNumber);
    }

    @Test
    void testIsPhoneRegistered_NullPhone() {
        // Act
        boolean result = patientService.isPhoneRegistered(null);

        // Assert
        assertFalse(result);
        verify(patientRepository, never()).existsByPhoneNumber(anyString());
    }

    @Test
    void testRegisterPatient_HIPAACompliance_DataMasking() {
        // Arrange
        when(validationService.validatePatientRegistration(validRequest)).thenReturn(List.of());
        when(validationService.sanitizeInput(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordUtil.hashPassword("SecurePass123!")).thenReturn("$2a$12$hashedPassword");
        
        Patient savedPatient = createPatientFromRequest(validRequest, "$2a$12$hashedPassword");
        savedPatient.setId(UUID.randomUUID());
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        // Act
        PatientRegistrationResponse response = patientService.registerPatient(validRequest);

        // Assert
        assertTrue(response.isSuccess());
        
        // Verify phone number is masked in response (HIPAA compliance)
        String maskedPhone = response.getData().getPhoneNumber();
        assertTrue(maskedPhone.contains("***"));
        assertFalse(maskedPhone.equals(validRequest.getPhoneNumber()));
    }

    @Test
    void testRegisterPatient_SecurityDefaults() {
        // Arrange
        when(validationService.validatePatientRegistration(validRequest)).thenReturn(List.of());
        when(validationService.sanitizeInput(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordUtil.hashPassword("SecurePass123!")).thenReturn("$2a$12$hashedPassword");
        
        Patient savedPatient = createPatientFromRequest(validRequest, "$2a$12$hashedPassword");
        savedPatient.setId(UUID.randomUUID());
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        // Act
        PatientRegistrationResponse response = patientService.registerPatient(validRequest);

        // Assert
        assertTrue(response.isSuccess());
        
        // Verify security defaults are set correctly
        verify(patientRepository).save(argThat(patient -> 
            !patient.getEmailVerified() &&
            !patient.getPhoneVerified() &&
            patient.getIsActive() &&
            patient.getFailedLoginAttempts() == 0 &&
            patient.getAccountLockedUntil() == null
        ));
    }

    // Helper method to create a patient from request (similar to service method)
    private Patient createPatientFromRequest(PatientRegistrationRequest request, String hashedPassword) {
        Patient patient = new Patient();
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setEmail(request.getEmail());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setPasswordHash(hashedPassword);
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setAddress(request.getAddress());
        
        if (request.getEmergencyContact() != null) {
            patient.setEmergencyContact(request.getEmergencyContact());
        }
        
        if (request.getMedicalHistory() != null && !request.getMedicalHistory().isEmpty()) {
            patient.setMedicalHistory(request.getMedicalHistory());
        }
        
        if (request.getInsuranceInfo() != null) {
            patient.setInsuranceInfo(request.getInsuranceInfo());
        }
        
        patient.setEmailVerified(false);
        patient.setPhoneVerified(false);
        patient.setIsActive(true);
        patient.setFailedLoginAttempts(0);
        
        return patient;
    }
} 