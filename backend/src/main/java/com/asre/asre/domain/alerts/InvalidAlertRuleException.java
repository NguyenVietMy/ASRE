package com.asre.asre.domain.alerts;

/**
 * Exception thrown when an alert rule is invalid.
 */
public class InvalidAlertRuleException extends RuntimeException {
    public InvalidAlertRuleException(String message) {
        super(message);
    }

    public InvalidAlertRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}

