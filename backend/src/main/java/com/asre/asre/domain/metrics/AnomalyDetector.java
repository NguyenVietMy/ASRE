package com.asre.asre.domain.metrics;

import java.util.List;
import java.util.UUID;

/**
 * Domain service interface for anomaly detection.
 * Implementations in infrastructure layer (integrated, not microservice).
 */
public interface AnomalyDetector {
    /**
     * Detect anomalies in a time series using the specified method.
     * @param dataPoints The time series data points
     * @param method The detection method to use
     * @return List of anomaly detection results (one per data point)
     */
    List<AnomalyDetectionResult> detectAnomalies(
            List<TimeSeriesPoint> dataPoints,
            AnomalyDetectionQuery.AnomalyDetectionMethod method,
            UUID projectId,
            UUID serviceId,
            String metricName);
}

