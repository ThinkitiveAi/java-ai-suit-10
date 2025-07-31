package com.healthfirst.service;

import com.healthfirst.dto.ProviderLoginRequest;
import com.healthfirst.dto.ProviderLoginResponse;
import com.healthfirst.dto.RefreshTokenRequest;
import com.healthfirst.entity.Provider;
import com.healthfirst.entity.RefreshToken;
import com.healthfirst.entity.embedded.ClinicAddress;
import com.healthfirst.enums.VerificationStatus;
import com.healthfirst.repository.ProviderRepository;
import com.healthfirst.util.JwtUtil;
import com.healthfirst.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordUtil passwordUtil;

    @InjectMocks
    private AuthService authService;

    private Provider testProvider;
    private ProviderLoginRequest loginRequest;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        // Setup test provider
        testProvider = new Provider();
        testProvider.setId(UUID.randomUUID());
        testProvider.setFirstName("John");
        testProvider.setLastName("Doe");
        testProvider.setEmail("john.doe@clinic.com");
        testProvider.setPhoneNumber("+1234567890");
        testProvider.setPasswordHash("hashedPassword");
        testProvider.setSpecialization("Cardiology");
        testProvider.setLicenseNumber("MD123456789");
        testProvider.setYearsOfExperience(10);
        testProvider.setVerificationStatus(VerificationStatus.VERIFIED);
        testProvider.setIsActive(true);
        testProvider.setFailedLoginAttempts(0);
        testProvider.setAccountLockedUntil(null);
        
        ClinicAddress address = new ClinicAddress();
        address.setStreet("123 Medical Center Dr");
        address.setCity("New York");
        address.setState("NY");
        address.setZip("10001");
        testProvider.setClinicAddress(address);

        // Setup login request
        loginRequest = new ProviderLoginRequest();
        loginRequest.setIdentifier("john.doe@clinic.com");
        loginRequest.setPassword("SecurePassword123!");
        loginRequest.setRememberMe(false);

        // Setup refresh token
        refreshToken = new RefreshToken();
        refreshToken.setId(UUID.randomUUID());
        refreshToken.setProvider(testProvider);
        refreshToken.setTokenHash("hashedRefreshToken");
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshToken.setIsRevoked(false);
    }

    @Test
    void testLoginProvider_Success() {
        // Arrange
        when(providerRepository.findByEmail("john.doe@clinic.com")).thenReturn(Optional.of(testProvider));
        when(passwordUtil.verifyPassword("SecurePassword123!", "hashedPassword")).thenReturn(true);
        when(jwtUtil.generateAccessToken(testProvider, false)).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(testProvider, false)).thenReturn("refreshToken");
        when(jwtUtil.getTokenExpirationInSeconds(false)).thenReturn(3600L);
        when(tokenService.createRefreshToken(any(), any(), anyBoolean(), any(), any())).thenReturn(refreshToken);
        when(providerRepository.save(testProvider)).thenReturn(testProvider);

        // Act
        ProviderLoginResponse response = authService.loginProvider(loginRequest, "userAgent", "192.168.1.1");

        // Assert
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals("accessToken", response.getData().getAccessToken());
        assertEquals("refreshToken", response.getData().getRefreshToken());
        
        verify(providerRepository).save(testProvider);
        verify(tokenService).createRefreshToken(testProvider, "refreshToken", false, "userAgent", "192.168.1.1");
    }

    @Test
    void testLoginProvider_InvalidCredentials() {
        // Arrange
        when(providerRepository.findByEmail("john.doe@clinic.com")).thenReturn(Optional.of(testProvider));
        when(passwordUtil.verifyPassword("wrongPassword", "hashedPassword")).thenReturn(false);

        loginRequest.setPassword("wrongPassword");

        // Act
        ProviderLoginResponse response = authService.loginProvider(loginRequest, "userAgent", "192.168.1.1");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("INVALID_CREDENTIALS", response.getErrorCode());
        
        verify(providerRepository).save(testProvider); // Failed attempt should be recorded
        verify(tokenService, never()).createRefreshToken(any(), any(), anyBoolean(), any(), any());
    }

    @Test
    void testLoginProvider_AccountLocked() {
        // Arrange
        testProvider.setAccountLockedUntil(LocalDateTime.now().plusMinutes(30));
        when(providerRepository.findByEmail("john.doe@clinic.com")).thenReturn(Optional.of(testProvider));

        // Act
        ProviderLoginResponse response = authService.loginProvider(loginRequest, "userAgent", "192.168.1.1");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("ACCOUNT_LOCKED", response.getErrorCode());
        
        verify(passwordUtil, never()).verifyPassword(any(), any());
        verify(tokenService, never()).createRefreshToken(any(), any(), anyBoolean(), any(), any());
    }

    @Test
    void testLoginProvider_AccountNotVerified() {
        // Arrange
        testProvider.setVerificationStatus(VerificationStatus.PENDING);
        when(providerRepository.findByEmail("john.doe@clinic.com")).thenReturn(Optional.of(testProvider));

        // Act
        ProviderLoginResponse response = authService.loginProvider(loginRequest, "userAgent", "192.168.1.1");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("ACCOUNT_NOT_VERIFIED", response.getErrorCode());
        
        verify(passwordUtil, never()).verifyPassword(any(), any());
        verify(tokenService, never()).createRefreshToken(any(), any(), anyBoolean(), any(), any());
    }

    @Test
    void testLoginProvider_AccountInactive() {
        // Arrange
        testProvider.setIsActive(false);
        when(providerRepository.findByEmail("john.doe@clinic.com")).thenReturn(Optional.of(testProvider));

        // Act
        ProviderLoginResponse response = authService.loginProvider(loginRequest, "userAgent", "192.168.1.1");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("ACCOUNT_INACTIVE", response.getErrorCode());
        
        verify(passwordUtil, never()).verifyPassword(any(), any());
        verify(tokenService, never()).createRefreshToken(any(), any(), anyBoolean(), any(), any());
    }

    @Test
    void testLoginProvider_ProviderNotFound() {
        // Arrange
        when(providerRepository.findByEmail("john.doe@clinic.com")).thenReturn(Optional.empty());

        // Act
        ProviderLoginResponse response = authService.loginProvider(loginRequest, "userAgent", "192.168.1.1");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("INVALID_CREDENTIALS", response.getErrorCode());
        
        verify(passwordUtil, never()).verifyPassword(any(), any());
        verify(tokenService, never()).createRefreshToken(any(), any(), anyBoolean(), any(), any());
    }

    @Test
    void testRefreshToken_Success() {
        // Arrange
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest("refreshTokenValue");
        
        when(tokenService.validateRefreshToken("refreshTokenValue")).thenReturn(Optional.of(refreshToken));
        when(jwtUtil.getRefreshTokenExpirationInSeconds(false)).thenReturn(604800L);
        when(jwtUtil.generateAccessToken(testProvider, false)).thenReturn("newAccessToken");
        when(jwtUtil.generateRefreshToken(testProvider, false)).thenReturn("newRefreshToken");
        when(jwtUtil.getTokenExpirationInSeconds(false)).thenReturn(3600L);
        when(tokenService.rotateRefreshToken(any(), any(), anyBoolean(), any(), any())).thenReturn(refreshToken);

        // Act
        ProviderLoginResponse response = authService.refreshToken(refreshRequest, "userAgent", "192.168.1.1");

        // Assert
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals("newAccessToken", response.getData().getAccessToken());
        assertEquals("newRefreshToken", response.getData().getRefreshToken());
        
        verify(tokenService).rotateRefreshToken(refreshToken, "newRefreshToken", false, "userAgent", "192.168.1.1");
    }

    @Test
    void testRefreshToken_InvalidToken() {
        // Arrange
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest("invalidToken");
        
        when(tokenService.validateRefreshToken("invalidToken")).thenReturn(Optional.empty());

        // Act
        ProviderLoginResponse response = authService.refreshToken(refreshRequest, "userAgent", "192.168.1.1");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("INVALID_REFRESH_TOKEN", response.getErrorCode());
        
        verify(tokenService, never()).rotateRefreshToken(any(), any(), anyBoolean(), any(), any());
    }

    @Test
    void testLogoutProvider_Success() {
        // Arrange
        String refreshTokenValue = "refreshTokenValue";
        doNothing().when(tokenService).revokeRefreshToken(refreshTokenValue);

        // Act
        boolean result = authService.logoutProvider(refreshTokenValue);

        // Assert
        assertTrue(result);
        verify(tokenService).revokeRefreshToken(refreshTokenValue);
    }

    @Test
    void testLogoutAllSessions_Success() {
        // Arrange
        String refreshTokenValue = "refreshTokenValue";
        
        when(tokenService.validateRefreshToken(refreshTokenValue)).thenReturn(Optional.of(refreshToken));
        doNothing().when(tokenService).revokeAllTokensForProvider(testProvider);

        // Act
        boolean result = authService.logoutAllSessions(refreshTokenValue);

        // Assert
        assertTrue(result);
        verify(tokenService).revokeAllTokensForProvider(testProvider);
    }

    @Test
    void testIsTokenValid_ValidToken() {
        // Arrange
        String token = "validToken";
        when(jwtUtil.validateToken(token)).thenReturn(mock(com.auth0.jwt.interfaces.DecodedJWT.class));
        when(jwtUtil.isTokenExpired(token)).thenReturn(false);

        // Act
        boolean result = authService.isTokenValid(token);

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsTokenValid_InvalidToken() {
        // Arrange
        String token = "invalidToken";
        when(jwtUtil.validateToken(token)).thenThrow(new RuntimeException("Invalid token"));

        // Act
        boolean result = authService.isTokenValid(token);

        // Assert
        assertFalse(result);
    }

    @Test
    void testGetProviderFromToken_Success() {
        // Arrange
        String token = "validToken";
        UUID providerId = testProvider.getId();
        
        when(jwtUtil.getProviderIdFromToken(token)).thenReturn(providerId);
        when(providerRepository.findById(providerId)).thenReturn(Optional.of(testProvider));

        // Act
        Optional<Provider> result = authService.getProviderFromToken(token);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testProvider, result.get());
    }

    @Test
    void testGetProviderFromToken_ProviderNotFound() {
        // Arrange
        String token = "validToken";
        UUID providerId = UUID.randomUUID();
        
        when(jwtUtil.getProviderIdFromToken(token)).thenReturn(providerId);
        when(providerRepository.findById(providerId)).thenReturn(Optional.empty());

        // Act
        Optional<Provider> result = authService.getProviderFromToken(token);

        // Assert
        assertFalse(result.isPresent());
    }
} 