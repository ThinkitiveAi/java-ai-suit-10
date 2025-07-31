package com.healthfirst.entity.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;

@Embeddable
public class InsuranceInfo {

    @Size(max = 100, message = "Insurance provider must not exceed 100 characters")
    @Column(name = "insurance_provider", length = 100)
    private String provider;

    @Size(max = 50, message = "Policy number must not exceed 50 characters")
    @Column(name = "insurance_policy_number", length = 50)
    private String policyNumber;

    // Default constructor
    public InsuranceInfo() {}

    // Constructor
    public InsuranceInfo(String provider, String policyNumber) {
        this.provider = provider;
        this.policyNumber = policyNumber;
    }

    // Getters and Setters
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    @Override
    public String toString() {
        return "InsuranceInfo{" +
                "provider='" + provider + '\'' +
                ", policyNumber='" + policyNumber + '\'' +
                '}';
    }
} 