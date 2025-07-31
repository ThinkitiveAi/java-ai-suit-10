package com.healthfirst.service;

import com.healthfirst.entity.Provider;
import com.healthfirst.entity.RefreshToken;
import com.healthfirst.repository.RefreshTokenRepository;
import com.healthfirst.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Create and store refresh token
     */
    public RefreshToken createRefreshToken(Provider provider, String tokenValue, boolean rememberMe, 
                                         String userAgent, String ipAddress) {
        try {
            // Hash the token before storing
            String tokenHash = hashToken(tokenValue);
            
            // Calculate expiration
            LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtUtil.getRefreshTokenExpirationInSeconds(rememberMe));

            // Create refresh token entity
            RefreshToken refreshToken = new RefreshToken(
                provider, tokenHash, expiresAt, userAgent, ipAddress
            );

            // Save to database
            RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
            logger.info("Created refresh token for provider {} expires at {}", 
                       provider.getId(), expiresAt);
            
            return savedToken;

        } catch (Exception e) {
            logger.error("Error creating refresh token for provider {}", provider.getId(), e);
            throw new RuntimeException("Failed to create refresh token", e);
        }
    }

    /**
     * Validate refresh token
     */
    public Optional<RefreshToken> validateRefreshToken(String tokenValue) {
        try {
            // First validate JWT structure and expiration
            if (jwtUtil.isTokenExpired(tokenValue) || !jwtUtil.isRefreshToken(tokenValue)) {
                logger.warn("Invalid or expired refresh token structure");
                return Optional.empty();
            }

            // Hash the token to find in database
            String tokenHash = hashToken(tokenValue);
            
            Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByTokenHash(tokenHash);
            
            if (tokenOpt.isPresent()) {
                RefreshToken token = tokenOpt.get();
                
                if (token.isValid()) {
                    // Update last used timestamp
                    token.updateLastUsed();
                    refreshTokenRepository.save(token);
                    
                    logger.info("Validated refresh token for provider {}", token.getProvider().getId());
                    return Optional.of(token);
                } else {
                    logger.warn("Refresh token is revoked or expired for provider {}", 
                               token.getProvider().getId());
                }
            } else {
                logger.warn("Refresh token not found in database");
            }
            
            return Optional.empty();

        } catch (Exception e) {
            logger.error("Error validating refresh token", e);
            return Optional.empty();
        }
    }

    /**
     * Revoke refresh token
     */
    public void revokeRefreshToken(String tokenValue) {
        try {
            String tokenHash = hashToken(tokenValue);
            Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByTokenHash(tokenHash);
            
            if (tokenOpt.isPresent()) {
                RefreshToken token = tokenOpt.get();
                token.revoke();
                refreshTokenRepository.save(token);
                
                logger.info("Revoked refresh token for provider {}", token.getProvider().getId());
            } else {
                logger.warn("Attempted to revoke non-existent refresh token");
            }

        } catch (Exception e) {
            logger.error("Error revoking refresh token", e);
        }
    }

    /**
     * Revoke all tokens for a provider
     */
    public void revokeAllTokensForProvider(Provider provider) {
        try {
            refreshTokenRepository.revokeAllTokensForProvider(provider);
            logger.info("Revoked all refresh tokens for provider {}", provider.getId());

        } catch (Exception e) {
            logger.error("Error revoking all tokens for provider {}", provider.getId(), e);
        }
    }

    /**
     * Revoke all tokens for a provider by ID
     */
    public void revokeAllTokensForProvider(UUID providerId) {
        try {
            refreshTokenRepository.revokeAllTokensForProviderId(providerId);
            logger.info("Revoked all refresh tokens for provider {}", providerId);

        } catch (Exception e) {
            logger.error("Error revoking all tokens for provider {}", providerId, e);
        }
    }

    /**
     * Get active token count for provider
     */
    public long getActiveTokenCount(Provider provider) {
        return refreshTokenRepository.countActiveTokensForProvider(provider, LocalDateTime.now());
    }

    /**
     * Clean up expired tokens (scheduled task)
     */
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredTokens() {
        try {
            logger.info("Starting cleanup of expired refresh tokens");
            refreshTokenRepository.cleanupOldTokens(LocalDateTime.now());
            logger.info("Completed cleanup of expired refresh tokens");

        } catch (Exception e) {
            logger.error("Error during token cleanup", e);
        }
    }

    /**
     * Get all tokens for a provider
     */
    public List<RefreshToken> getProviderTokens(Provider provider) {
        return refreshTokenRepository.findByProvider(provider);
    }

    /**
     * Check if token exists and is valid
     */
    public boolean isTokenValid(String tokenValue) {
        try {
            String tokenHash = hashToken(tokenValue);
            return refreshTokenRepository.existsValidToken(tokenHash, LocalDateTime.now());
        } catch (Exception e) {
            logger.error("Error checking token validity", e);
            return false;
        }
    }

    /**
     * Rotate refresh token (create new, revoke old)
     */
    public RefreshToken rotateRefreshToken(RefreshToken oldToken, String newTokenValue, 
                                         boolean rememberMe, String userAgent, String ipAddress) {
        try {
            // Revoke old token
            oldToken.revoke();
            refreshTokenRepository.save(oldToken);
            
            // Create new token
            RefreshToken newToken = createRefreshToken(
                oldToken.getProvider(), newTokenValue, rememberMe, userAgent, ipAddress
            );
            
            logger.info("Rotated refresh token for provider {}", oldToken.getProvider().getId());
            return newToken;

        } catch (Exception e) {
            logger.error("Error rotating refresh token for provider {}", 
                        oldToken.getProvider().getId(), e);
            throw new RuntimeException("Failed to rotate refresh token", e);
        }
    }

    /**
     * Hash token for secure storage
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();

        } catch (Exception e) {
            logger.error("Error hashing token", e);
            throw new RuntimeException("Failed to hash token", e);
        }
    }
} 