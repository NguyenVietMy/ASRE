package com.asre.asre.domain.logs;

import lombok.Value;

import java.util.List;

/**
 * Domain object representing log volume aggregation results.
 */
@Value
public class LogVolumeResult {
    List<LogVolumePoint> dataPoints;
    boolean isSampled;

    public LogVolumeResult(List<LogVolumePoint> dataPoints, boolean isSampled) {
        if (dataPoints == null) {
            throw new IllegalArgumentException("Data points cannot be null");
        }
        this.dataPoints = List.copyOf(dataPoints);
        this.isSampled = isSampled;
    }
}


