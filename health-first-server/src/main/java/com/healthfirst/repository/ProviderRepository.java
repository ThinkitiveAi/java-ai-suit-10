package com.healthfirst.repository;

import com.healthfirst.entity.Provider;
import com.healthfirst.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, UUID> {

    /**
     * Find provider by email
     */
    Optional<Provider> findByEmail(String email);

    /**
     * Find provider by phone number
     */
    Optional<Provider> findByPhoneNumber(String phoneNumber);

    /**
     * Find provider by license number
     */
    Optional<Provider> findByLicenseNumber(String licenseNumber);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if phone number exists
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Check if license number exists
     */
    boolean existsByLicenseNumber(String licenseNumber);

    /**
     * Find all providers by verification status
     */
    List<Provider> findByVerificationStatus(VerificationStatus verificationStatus);

    /**
     * Find all active providers
     */
    List<Provider> findByIsActiveTrue();

    /**
     * Find providers by specialization
     */
    List<Provider> findBySpecializationContainingIgnoreCase(String specialization);

    /**
     * Find verified and active providers
     */
    @Query("SELECT p FROM Provider p WHERE p.verificationStatus = :status AND p.isActive = true")
    List<Provider> findVerifiedActiveProviders(@Param("status") VerificationStatus status);

    /**
     * Count providers by verification status
     */
    long countByVerificationStatus(VerificationStatus verificationStatus);
} 