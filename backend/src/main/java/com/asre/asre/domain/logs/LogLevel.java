package com.asre.asre.domain.logs;

/**
 * Domain enum representing log severity levels.
 * Prevents invalid level strings from leaking into the core.
 */
public enum LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    FATAL;

    /**
     * Parse a string to LogLevel (case-insensitive).
     * 
     * @throws IllegalArgumentException if string doesn't match any level
     */
    public static LogLevel fromString(String level) {
        if (level == null) {
            throw new IllegalArgumentException("Log level cannot be null");
        }
        try {
            return LogLevel.valueOf(level.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid log level: " + level, e);
        }
    }

    /**
     * Returns true if this level is an error level (ERROR or FATAL).
     */
    public boolean isError() {
        return this == ERROR || this == FATAL;
    }

    /**
     * Returns true if this level is at least WARN severity.
     */
    public boolean isAtLeastWarn() {
        return this == WARN || this == ERROR || this == FATAL;
    }
}
