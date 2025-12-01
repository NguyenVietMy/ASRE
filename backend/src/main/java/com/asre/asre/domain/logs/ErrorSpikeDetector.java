package com.asre.asre.domain.logs;

import java.util.List;

/**
 * Domain service for detecting error spikes from raw volume data.
 * Defines domain rules for spike detection, severity calculation, and thresholds.
 */
public class ErrorSpikeDetector {
    
    private static final long DEFAULT_SPIKE_THRESHOLD = 10L;
    private static final double MAX_SEVERITY = 10.0;

    /**
     * Detect error spikes from raw volume data points.
     * Domain interprets spikes based on threshold and calculates severity.
     */
    public ErrorSpikeResult detectSpikes(
            List<LogVolumePoint> dataPoints,
            Long threshold) {
        
        long effectiveThreshold = threshold != null ? threshold : DEFAULT_SPIKE_THRESHOLD;
        
        List<ErrorSpikeResult.ErrorSpikePoint> spikes = dataPoints.stream()
                .filter(point -> point.getCount() > effectiveThreshold)
                .map(point -> {
                    double severity = calculateSeverity(point.getCount(), effectiveThreshold);
                    String description = generateDescription(point.getCount(), severity);
                    return new ErrorSpikeResult.ErrorSpikePoint(
                            point.getTimestamp(),
                            point.getCount(),
                            severity,
                            java.util.Optional.of(description)
                    );
                })
                .toList();

        return new ErrorSpikeResult(spikes, dataPoints);
    }

    /**
     * Calculate spike severity based on domain rules.
     */
    private double calculateSeverity(long count, long threshold) {
        if (count <= threshold) {
            return 0.0;
        }
        // Ratio above threshold, capped at MAX_SEVERITY
        double ratio = (double) count / threshold;
        return Math.min(ratio, MAX_SEVERITY);
    }

    /**
     * Generate domain interpretation description for a spike.
     */
    private String generateDescription(long count, double severity) {
        if (severity >= 5.0) {
            return String.format("Critical error spike: %d errors detected", count);
        } else if (severity >= 2.0) {
            return String.format("Significant error spike: %d errors detected", count);
        } else {
            return String.format("Error spike: %d errors detected", count);
        }
    }
}

