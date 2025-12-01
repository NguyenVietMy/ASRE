package com.asre.asre.domain.logs;

import com.asre.asre.domain.metrics.RollupPeriod;
import com.asre.asre.domain.metrics.TimeRange;
import lombok.Value;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain value object representing an error spike detection query.
 * Error spikes are a separate domain concept from general volume queries.
 */
@Value
public class ErrorSpikeQuery {
    UUID projectId;
    TimeRange timeRange;
    RollupPeriod rollupPeriod;
    Optional<ServiceFilter> serviceFilter;
    Optional<Long> spikeThreshold; // Minimum count to be considered a spike

    public ErrorSpikeQuery(
            UUID projectId,
            TimeRange timeRange,
            RollupPeriod rollupPeriod,
            ServiceFilter serviceFilter,
            Long spikeThreshold) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        if (timeRange == null) {
            throw new IllegalArgumentException("Time range cannot be null");
        }
        if (rollupPeriod == null) {
            throw new IllegalArgumentException("Rollup period cannot be null");
        }
        
        this.projectId = projectId;
        this.timeRange = timeRange;
        this.rollupPeriod = rollupPeriod;
        this.serviceFilter = Optional.ofNullable(serviceFilter);
        this.spikeThreshold = Optional.ofNullable(spikeThreshold);
    }
}


