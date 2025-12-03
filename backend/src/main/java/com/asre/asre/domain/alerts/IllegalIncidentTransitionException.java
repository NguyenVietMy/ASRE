package com.asre.asre.domain.alerts;

/**
 * Exception thrown when an invalid incident status transition is attempted.
 */
public class IllegalIncidentTransitionException extends RuntimeException {
    public IllegalIncidentTransitionException(String message) {
        super(message);
    }
}

