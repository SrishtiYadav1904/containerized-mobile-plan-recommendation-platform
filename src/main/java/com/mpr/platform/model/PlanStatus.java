package com.mpr.platform.model;

/**
 * Lifecycle statuses for a mobile plan tariff.
 * Valid transitions:
 * DRAFT -> IN_REVIEW (Analyst submits)
 * IN_REVIEW -> PUBLISHED (Administrator approves)
 * PUBLISHED -> ARCHIVED (Administrator retires)
 */
public enum PlanStatus {
    DRAFT("Draft"),
    IN_REVIEW("In Review"),
    PUBLISHED("Published"),
    ARCHIVED("Archived");

    private final String displayName;

    PlanStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
