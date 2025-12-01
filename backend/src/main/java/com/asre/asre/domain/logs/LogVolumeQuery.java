package com.asre.asre.domain.logs;

import com.asre.asre.domain.metrics.RollupPeriod;
import com.asre.asre.domain.metrics.TimeRange;
import lombok.Value;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain value object representing a log volume aggregation query.
 */
@Value
public class LogVolumeQuery {
    UUID projectId;
    TimeRange timeRange;
    RollupPeriod rollupPeriod;
    Optional<ServiceFilter> serviceFilter;
    Optional<LogLevelFilter> levelFilter;

    public LogVolumeQuery(
            UUID projectId,
            TimeRange timeRange,
            RollupPeriod rollupPeriod,
            ServiceFilter serviceFilter,
            LogLevelFilter levelFilter) {
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
        this.levelFilter = Optional.ofNullable(levelFilter);
    }
}


