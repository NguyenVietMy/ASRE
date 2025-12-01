package com.asre.asre.domain.logs;

/**
 * Domain exception thrown when a log query is invalid.
 */
public class InvalidLogQueryException extends RuntimeException {
    public InvalidLogQueryException(String message) {
        super(message);
    }

    public InvalidLogQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}


