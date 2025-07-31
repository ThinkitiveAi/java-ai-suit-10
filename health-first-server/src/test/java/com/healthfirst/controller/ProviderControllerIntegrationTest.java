package com.healthfirst.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfirst.dto.ProviderRegistrationRequest;
import com.healthfirst.entity.embedded.ClinicAddress;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ProviderControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testRegisterProvider_Success() throws Exception {
        setUp();
        
        // Arrange
        ProviderRegistrationRequest request = createValidRegistrationRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post("/provider/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("john.doe@clinic.com"))
                .andExpect(jsonPath("$.data.verificationStatus").value("PENDING"));
    }

    @Test
    void testRegisterProvider_ValidationError() throws Exception {
        setUp();
        
        // Arrange - invalid request (missing required fields)
        ProviderRegistrationRequest request = new ProviderRegistrationRequest();
        request.setEmail("invalid-email");
        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post("/provider/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGetValidSpecializations() throws Exception {
        setUp();
        
        // Act & Assert
        mockMvc.perform(get("/provider/specializations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.count").isNumber());
    }

    @Test
    void testCheckEmailAvailability() throws Exception {
        setUp();
        
        // Act & Assert
        mockMvc.perform(get("/provider/check-email")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.available").isBoolean())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testCheckPhoneAvailability() throws Exception {
        setUp();
        
        // Act & Assert
        mockMvc.perform(get("/provider/check-phone")
                .param("phoneNumber", "+1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.available").isBoolean())
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"));
    }

    @Test
    void testCheckLicenseAvailability() throws Exception {
        setUp();
        
        // Act & Assert
        mockMvc.perform(get("/provider/check-license")
                .param("licenseNumber", "MD123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.available").isBoolean())
                .andExpect(jsonPath("$.licenseNumber").value("MD123456789"));
    }

    private ProviderRegistrationRequest createValidRegistrationRequest() {
        ProviderRegistrationRequest request = new ProviderRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@clinic.com");
        request.setPhoneNumber("+1234567890");
        request.setPassword("SecurePassword123!");
        request.setConfirmPassword("SecurePassword123!");
        request.setSpecialization("Cardiology");
        request.setLicenseNumber("MD123456789");
        request.setYearsOfExperience(10);
        
        ClinicAddress address = new ClinicAddress();
        address.setStreet("123 Medical Center Dr");
        address.setCity("New York");
        address.setState("NY");
        address.setZip("10001");
        request.setClinicAddress(address);
        
        return request;
    }
} 