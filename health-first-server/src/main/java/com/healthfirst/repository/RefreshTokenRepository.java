package com.healthfirst.repository;

import com.healthfirst.entity.Provider;
import com.healthfirst.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Find refresh token by token hash
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Find all valid (non-revoked and non-expired) tokens for a provider
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.provider = :provider AND rt.isRevoked = false AND rt.expiresAt > :now")
    List<RefreshToken> findValidTokensByProvider(@Param("provider") Provider provider, @Param("now") LocalDateTime now);

    /**
     * Find all tokens for a provider
     */
    List<RefreshToken> findByProvider(Provider provider);

    /**
     * Find all tokens for a provider ID
     */
    List<RefreshToken> findByProviderId(UUID providerId);

    /**
     * Delete all revoked tokens
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.isRevoked = true")
    void deleteRevokedTokens();

    /**
     * Delete all expired tokens
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Revoke all tokens for a provider
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.provider = :provider")
    void revokeAllTokensForProvider(@Param("provider") Provider provider);

    /**
     * Revoke all tokens for a provider ID
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.provider.id = :providerId")
    void revokeAllTokensForProviderId(@Param("providerId") UUID providerId);

    /**
     * Count active tokens for a provider
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.provider = :provider AND rt.isRevoked = false AND rt.expiresAt > :now")
    long countActiveTokensForProvider(@Param("provider") Provider provider, @Param("now") LocalDateTime now);

    /**
     * Find tokens by IP address
     */
    List<RefreshToken> findByIpAddress(String ipAddress);

    /**
     * Find tokens created after a specific date
     */
    List<RefreshToken> findByCreatedAtAfter(LocalDateTime dateTime);

    /**
     * Find tokens that expire before a specific date
     */
    List<RefreshToken> findByExpiresAtBefore(LocalDateTime dateTime);

    /**
     * Check if token exists and is valid
     */
    @Query("SELECT CASE WHEN COUNT(rt) > 0 THEN true ELSE false END FROM RefreshToken rt WHERE rt.tokenHash = :tokenHash AND rt.isRevoked = false AND rt.expiresAt > :now")
    boolean existsValidToken(@Param("tokenHash") String tokenHash, @Param("now") LocalDateTime now);

    /**
     * Clean up old tokens (both expired and revoked)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.isRevoked = true OR rt.expiresAt < :now")
    void cleanupOldTokens(@Param("now") LocalDateTime now);
} 