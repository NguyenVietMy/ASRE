package com.asre.asre.domain.metrics;

import lombok.Value;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain value object representing a metric query.
 * Encapsulates all parameters needed to query metrics.
 */
@Value
public class MetricQuery {
    UUID projectId;
    String metricName;
    AggregationType aggregationType;
    TimeRange timeRange;
    RollupPeriod rollupPeriod;
    Optional<UUID> serviceId;
    Optional<Map<String, String>> tags;

    public MetricQuery(
            UUID projectId,
            String metricName,
            AggregationType aggregationType,
            TimeRange timeRange,
            RollupPeriod rollupPeriod,
            UUID serviceId,
            Map<String, String> tags) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        if (metricName == null || metricName.isBlank()) {
            throw new IllegalArgumentException("Metric name cannot be null or blank");
        }
        if (aggregationType == null) {
            throw new IllegalArgumentException("Aggregation type cannot be null");
        }
        if (timeRange == null) {
            throw new IllegalArgumentException("Time range cannot be null");
        }
        if (rollupPeriod == null) {
            throw new IllegalArgumentException("Rollup period cannot be null");
        }
        
        this.projectId = projectId;
        this.metricName = metricName.trim();
        this.aggregationType = aggregationType;
        this.timeRange = timeRange;
        this.rollupPeriod = rollupPeriod;
        this.serviceId = Optional.ofNullable(serviceId);
        this.tags = Optional.ofNullable(tags);
    }

    public MetricQuery(
            UUID projectId,
            String metricName,
            AggregationType aggregationType,
            TimeRange timeRange,
            RollupPeriod rollupPeriod) {
        this(projectId, metricName, aggregationType, timeRange, rollupPeriod, null, null);
    }
}

