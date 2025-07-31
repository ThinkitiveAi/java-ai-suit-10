package com.healthfirst.enums;

public enum RecurrencePattern {
    NONE("None"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    WEEKDAYS("Weekdays"),
    WEEKENDS("Weekends"),
    CUSTOM("Custom");

    private final String displayName;

    RecurrencePattern(String displayName) {
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