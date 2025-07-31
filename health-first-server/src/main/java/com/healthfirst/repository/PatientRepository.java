package com.healthfirst.repository;

import com.healthfirst.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    /**
     * Find patient by email
     */
    Optional<Patient> findByEmail(String email);

    /**
     * Find patient by phone number
     */
    Optional<Patient> findByPhoneNumber(String phoneNumber);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if phone number exists
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Find active patients by email
     */
    @Query("SELECT p FROM Patient p WHERE p.email = :email AND p.isActive = true")
    Optional<Patient> findActiveByEmail(@Param("email") String email);

    /**
     * Find active patients by phone number
     */
    @Query("SELECT p FROM Patient p WHERE p.phoneNumber = :phoneNumber AND p.isActive = true")
    Optional<Patient> findActiveByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    /**
     * Count all active patients
     */
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.isActive = true")
    long countActivePatients();

    /**
     * Find verified patients by email
     */
    @Query("SELECT p FROM Patient p WHERE p.email = :email AND p.emailVerified = true AND p.isActive = true")
    Optional<Patient> findVerifiedByEmail(@Param("email") String email);
} 