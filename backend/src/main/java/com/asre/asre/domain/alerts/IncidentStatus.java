package com.asre.asre.domain.alerts;

/**
 * Status of an incident.
 * Maps to database enum: open, investigating, resolved
 */
public enum IncidentStatus {
    OPEN,
    INVESTIGATING,
    RESOLVED;

    /**
     * Checks if a transition from current status to new status is allowed.
     */
    public boolean canTransitionTo(IncidentStatus newStatus) {
        if (this == newStatus) {
            return true; // No-op transition
        }
        return switch (this) {
            case OPEN -> newStatus == INVESTIGATING || newStatus == RESOLVED;
            case INVESTIGATING -> newStatus == RESOLVED;
            case RESOLVED -> false; // Cannot transition from resolved
        };
    }
}

