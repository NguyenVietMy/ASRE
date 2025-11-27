package com.asre.asre.application.ratelimit;

/**
 * Interface for rate limiting implementation.
 * Uses sliding window algorithm with Redis.
 */
public interface RateLimiter {
    /**
     * Checks if request is allowed for the given key and rate limit.
     * @param key Unique identifier (e.g., project_id)
     * @param limitPerMinute Rate limit per minute
     * @return true if allowed, false if rate limit exceeded
     */
    boolean isAllowed(String key, int limitPerMinute);
}


