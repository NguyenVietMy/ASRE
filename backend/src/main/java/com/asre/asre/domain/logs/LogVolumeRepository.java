package com.asre.asre.domain.logs;

/**
 * Repository interface for log volume aggregations.
 * Implementations in infrastructure layer use OpenSearch date_histogram.
 */
public interface LogVolumeRepository {
    /**
     * Execute a log volume aggregation query.
     */
    LogVolumeResult executeVolumeQuery(LogVolumeQuery query);

    /**
     * Execute an error spike detection query.
     * Returns raw data buckets - domain interprets spikes.
     */
    ErrorSpikeResult executeErrorSpikeQuery(ErrorSpikeQuery query);
}


