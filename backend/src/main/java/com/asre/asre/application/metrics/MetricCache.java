package com.asre.asre.application.metrics;

import com.asre.asre.domain.metrics.*;

import java.util.Optional;

/**
 * Application-level port for caching metric query results.
 * Hides cache key generation and TTL calculation from use cases.
 * Implementations in infrastructure layer.
 */
public interface MetricCache {
    /**
     * Get a cached metric query result.
     * Cache key is derived from the query object internally.
     */
    Optional<MetricQueryResult> get(MetricQuery query);

    /**
     * Put a metric query result in cache.
     * TTL is calculated based on query characteristics internally.
     */
    void put(MetricQuery query, MetricQueryResult result);

    /**
     * Get a cached multi-metric query result.
     */
    Optional<MultiMetricQueryResult> getMulti(MultiMetricQuery query);

    /**
     * Put a multi-metric query result in cache.
     */
    void putMulti(MultiMetricQuery query, MultiMetricQueryResult result);

    /**
     * Get a cached histogram result.
     */
    Optional<HistogramResult> getHistogram(HistogramQuery query);

    /**
     * Put a histogram result in cache.
     */
    void putHistogram(HistogramQuery query, HistogramResult result);
}

