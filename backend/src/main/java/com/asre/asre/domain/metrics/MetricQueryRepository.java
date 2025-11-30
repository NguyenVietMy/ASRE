package com.asre.asre.domain.metrics;

/**
 * Repository interface for querying metrics.
 * Implementations in infrastructure layer use JDBC to query TimescaleDB.
 */
public interface MetricQueryRepository {
    /**
     * Execute a single metric query and return time series data.
     */
    MetricQueryResult executeQuery(MetricQuery query);

    /**
     * Execute a multi-metric query (overlay) with optional timestamp alignment.
     */
    MultiMetricQueryResult executeMultiQuery(MultiMetricQuery query);

    /**
     * Execute a histogram query.
     */
    HistogramResult executeHistogramQuery(HistogramQuery query);
}

