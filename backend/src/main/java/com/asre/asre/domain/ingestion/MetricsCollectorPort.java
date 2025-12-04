package com.asre.asre.domain.ingestion;

/**
 * Port for collecting metrics about ingestion operations.
 * Implementation will be in infrastructure layer (Micrometer/Prometheus).
 */
public interface MetricsCollectorPort {
    void recordMetricsIngested(long count);

    void recordMetricsIngestionError();

    void recordMetricsIngestionDuration(long durationMs);

    void recordLogsIngested(long count);

    void recordLogsIngestionError();

    void recordLogsIngestionDuration(long durationMs);
}
