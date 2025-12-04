package com.asre.asre.infra.metrics;

import com.asre.asre.domain.metrics.*;
import com.asre.asre.domain.metrics.AnomalyDetectorPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Integrated anomaly detection implementation (not microservice).
 * Currently implements z-score method. Isolation Forest can be added later.
 */
@Component
@Slf4j
public class AnomalyDetectorImpl implements AnomalyDetectorPort {

    private static final double Z_SCORE_THRESHOLD = 2.5; // Standard threshold for anomalies

    @Override
    public List<AnomalyDetectionResult> detectAnomalies(
            List<TimeSeriesPoint> dataPoints,
            AnomalyDetectionQuery.AnomalyDetectionMethod method,
            UUID projectId,
            UUID serviceId,
            String metricName) {

        if (dataPoints == null || dataPoints.isEmpty()) {
            return List.of();
        }

        return switch (method) {
            case ZSCORE -> detectAnomaliesZScore(dataPoints, projectId, serviceId, metricName);
            case ISOLATION_FOREST -> detectAnomaliesIsolationForest(dataPoints, projectId, serviceId, metricName);
        };
    }

    private List<AnomalyDetectionResult> detectAnomaliesZScore(
            List<TimeSeriesPoint> dataPoints,
            UUID projectId,
            UUID serviceId,
            String metricName) {

        // Calculate mean and standard deviation
        double mean = dataPoints.stream()
                .mapToDouble(TimeSeriesPoint::getValue)
                .average()
                .orElse(0.0);

        double variance = dataPoints.stream()
                .mapToDouble(p -> Math.pow(p.getValue() - mean, 2))
                .average()
                .orElse(0.0);

        double stdDev = Math.sqrt(variance);

        if (stdDev == 0) {
            // No variance - all values are the same, no anomalies
            return List.of();
        }

        // Calculate z-scores and detect anomalies
        List<AnomalyDetectionResult> results = new ArrayList<>();
        for (TimeSeriesPoint point : dataPoints) {
            double zScore = (point.getValue() - mean) / stdDev;
            boolean isAnomaly = Math.abs(zScore) > Z_SCORE_THRESHOLD;

            AnomalyDetectionResult result = AnomalyDetectionResult.create(
                    projectId,
                    serviceId,
                    metricName,
                    point.getTimestamp(),
                    zScore,
                    isAnomaly,
                    java.util.Map.of(
                            "mean", mean,
                            "stdDev", stdDev,
                            "value", point.getValue()
                    )
            );
            results.add(result);
        }

        return results;
    }

    private List<AnomalyDetectionResult> detectAnomaliesIsolationForest(
            List<TimeSeriesPoint> dataPoints,
            UUID projectId,
            UUID serviceId,
            String metricName) {
        // TODO: Implement Isolation Forest algorithm
        // For now, fall back to z-score
        log.warn("Isolation Forest not yet implemented, falling back to z-score");
        return detectAnomaliesZScore(dataPoints, projectId, serviceId, metricName);
    }
}

