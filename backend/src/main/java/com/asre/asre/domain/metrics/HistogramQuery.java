package com.asre.asre.domain.metrics;

import lombok.Value;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain value object representing a histogram query.
 */
@Value
public class HistogramQuery {
    UUID projectId;
    String metricName;
    TimeRange timeRange;
    int bins;
    Optional<UUID> serviceId;

    public HistogramQuery(
            UUID projectId,
            String metricName,
            TimeRange timeRange,
            int bins,
            UUID serviceId) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        if (metricName == null || metricName.isBlank()) {
            throw new IllegalArgumentException("Metric name cannot be null or blank");
        }
        if (timeRange == null) {
            throw new IllegalArgumentException("Time range cannot be null");
        }
        if (bins < 2 || bins > 100) {
            throw new IllegalArgumentException("Bins must be between 2 and 100");
        }
        
        this.projectId = projectId;
        this.metricName = metricName.trim();
        this.timeRange = timeRange;
        this.bins = bins;
        this.serviceId = Optional.ofNullable(serviceId);
    }
}

