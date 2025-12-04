package com.asre.asre.domain.ingestion;

/**
 * Port for collecting API-level metrics.
 * Implementation will be in infrastructure layer.
 */
public interface ApiMetricsCollectorPort {
    void recordApiRequest();
    void recordRateLimitExceeded();
    void recordInvalidApiKey();
}

