package com.healthfirst.entity.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Embeddable
public class PatientAddress {

    @NotBlank(message = "Street is required")
    @Size(max = 200, message = "Street must not exceed 200 characters")
    @Column(name = "street", nullable = false, length = 200)
    private String street;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 50, message = "State must not exceed 50 characters")
    @Column(name = "state", nullable = false, length = 50)
    private String state;

    @NotBlank(message = "ZIP code is required")
    @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "ZIP code must be valid format (12345 or 12345-6789)")
    @Column(name = "zip", nullable = false, length = 10)
    private String zip;

    // Default constructor
    public PatientAddress() {}

    // Constructor
    public PatientAddress(String street, String city, String state, String zip) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zip = zip;
    }

    // Getters and Setters
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    @Override
    public String toString() {
        return "PatientAddress{" +
                "street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", zip='" + zip + '\'' +
                '}';
    }
} 