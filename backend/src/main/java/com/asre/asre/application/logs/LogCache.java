package com.asre.asre.application.logs;

import com.asre.asre.domain.logs.*;

import java.util.Optional;

/**
 * Application-level port for caching log query results.
 * Hides cache key generation and TTL calculation from use cases.
 * Only caches analytics-style queries (volume, error spikes), not full-text search.
 */
public interface LogCache {
    /**
     * Get a cached log volume result.
     * Cache key is derived from the query object internally.
     */
    Optional<LogVolumeResult> getVolume(LogVolumeQuery query);

    /**
     * Put a log volume result in cache.
     * TTL is calculated based on query characteristics internally.
     */
    void putVolume(LogVolumeQuery query, LogVolumeResult result);

    /**
     * Get a cached error spike result.
     */
    Optional<ErrorSpikeResult> getErrorSpikes(ErrorSpikeQuery query);

    /**
     * Put an error spike result in cache.
     */
    void putErrorSpikes(ErrorSpikeQuery query, ErrorSpikeResult result);
}


