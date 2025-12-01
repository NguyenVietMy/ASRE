package com.asre.asre.domain.logs;

/**
 * Domain exception thrown when a specific log entry is not found.
 */
public class LogNotFoundException extends RuntimeException {
    public LogNotFoundException(String message) {
        super(message);
    }

    public LogNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}


