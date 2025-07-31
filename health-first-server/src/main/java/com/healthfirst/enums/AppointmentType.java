package com.healthfirst.enums;

public enum AppointmentType {
    CONSULTATION("Consultation"),
    FOLLOW_UP("Follow-up"),
    ROUTINE_CHECKUP("Routine Checkup"),
    SPECIALIST_CONSULTATION("Specialist Consultation"),
    EMERGENCY("Emergency"),
    TELEMEDICINE("Telemedicine"),
    SURGICAL_CONSULTATION("Surgical Consultation"),
    DIAGNOSTIC("Diagnostic"),
    PREVENTIVE_CARE("Preventive Care"),
    THERAPY_SESSION("Therapy Session");

    private final String displayName;

    AppointmentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
} 