package com.asre.asre.domain.metrics;

import lombok.Value;

import java.util.List;
import java.util.UUID;

/**
 * Domain value object representing a multi-metric query (overlay).
 * Used for querying multiple metrics simultaneously with alignment.
 */
@Value
public class MultiMetricQuery {
    UUID projectId;
    List<SingleMetricQuery> queries;
    TimeRange timeRange;
    RollupPeriod rollupPeriod;
    boolean alignTimestamps;

    @Value
    public static class SingleMetricQuery {
        String metricName;
        AggregationType aggregationType;
        UUID serviceId;
    }

    public MultiMetricQuery(
            UUID projectId,
            List<SingleMetricQuery> queries,
            TimeRange timeRange,
            RollupPeriod rollupPeriod,
            boolean alignTimestamps) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        if (queries == null || queries.isEmpty()) {
            throw new IllegalArgumentException("Queries list cannot be null or empty");
        }
        if (timeRange == null) {
            throw new IllegalArgumentException("Time range cannot be null");
        }
        if (rollupPeriod == null) {
            throw new IllegalArgumentException("Rollup period cannot be null");
        }
        
        this.projectId = projectId;
        this.queries = List.copyOf(queries);
        this.timeRange = timeRange;
        this.rollupPeriod = rollupPeriod;
        this.alignTimestamps = alignTimestamps;
    }
}

