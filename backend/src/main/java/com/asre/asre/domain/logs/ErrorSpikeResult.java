package com.asre.asre.domain.logs;

import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Domain object representing error spike detection results.
 * Contains both raw data buckets and domain-interpreted spike information.
 */
@Value
public class ErrorSpikeResult {
    List<ErrorSpikePoint> spikes;
    List<LogVolumePoint> allDataPoints; // Raw buckets for UI presentation

    @Value
    public static class ErrorSpikePoint {
        Instant timestamp;
        long errorCount;
        double spikeSeverity; // Domain-calculated severity score
        Optional<String> description; // Domain interpretation
    }

    public ErrorSpikeResult(List<ErrorSpikePoint> spikes, List<LogVolumePoint> allDataPoints) {
        if (spikes == null) {
            throw new IllegalArgumentException("Spikes list cannot be null");
        }
        if (allDataPoints == null) {
            throw new IllegalArgumentException("Data points cannot be null");
        }
        this.spikes = List.copyOf(spikes);
        this.allDataPoints = List.copyOf(allDataPoints);
    }
}

