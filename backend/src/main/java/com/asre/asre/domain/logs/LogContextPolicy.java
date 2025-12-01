package com.asre.asre.domain.logs;

/**
 * Domain policy for log context retrieval.
 * Defines limits and validation rules.
 */
public class LogContextPolicy {
    
    public static final int MAX_BEFORE_AFTER = 50;
    public static final int DEFAULT_BEFORE = 10;
    public static final int DEFAULT_AFTER = 10;

    /**
     * Validate context window sizes and throw domain exception if invalid.
     */
    public static void validateContextWindow(int beforeCount, int afterCount) {
        if (beforeCount < 0 || beforeCount > MAX_BEFORE_AFTER) {
            throw new IllegalArgumentException(
                    String.format("Before count must be between 0 and %d", MAX_BEFORE_AFTER));
        }
        if (afterCount < 0 || afterCount > MAX_BEFORE_AFTER) {
            throw new IllegalArgumentException(
                    String.format("After count must be between 0 and %d", MAX_BEFORE_AFTER));
        }
    }
}

