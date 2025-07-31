package com.healthfirst.entity.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Embeddable
public class EmergencyContact {

    @Size(max = 100, message = "Emergency contact name must not exceed 100 characters")
    @Column(name = "emergency_contact_name", length = 100)
    private String name;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Emergency contact phone must be in valid international format")
    @Column(name = "emergency_contact_phone", length = 20)
    private String phone;

    @Size(max = 50, message = "Emergency contact relationship must not exceed 50 characters")
    @Column(name = "emergency_contact_relationship", length = 50)
    private String relationship;

    // Default constructor
    public EmergencyContact() {}

    // Constructor
    public EmergencyContact(String name, String phone, String relationship) {
        this.name = name;
        this.phone = phone;
        this.relationship = relationship;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    @Override
    public String toString() {
        return "EmergencyContact{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", relationship='" + relationship + '\'' +
                '}';
    }
} 