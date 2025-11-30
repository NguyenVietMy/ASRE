package com.asre.asre.domain.metrics;

import lombok.Value;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain value object representing an anomaly detection query.
 */
@Value
public class AnomalyDetectionQuery {
    UUID projectId;
    String metricName;
    TimeRange timeRange;
    AnomalyDetectionMethod method;
    Optional<UUID> serviceId;

    public enum AnomalyDetectionMethod {
        ZSCORE("zscore"),
        ISOLATION_FOREST("isolation_forest");

        private final String value;

        AnomalyDetectionMethod(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static AnomalyDetectionMethod fromString(String value) {
            if (value == null) {
                throw new IllegalArgumentException("Anomaly detection method cannot be null");
            }
            for (AnomalyDetectionMethod method : values()) {
                if (method.value.equalsIgnoreCase(value)) {
                    return method;
                }
            }
            throw new IllegalArgumentException("Unknown anomaly detection method: " + value);
        }
    }

    public AnomalyDetectionQuery(
            UUID projectId,
            String metricName,
            TimeRange timeRange,
            AnomalyDetectionMethod method,
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
        if (method == null) {
            throw new IllegalArgumentException("Anomaly detection method cannot be null");
        }
        
        this.projectId = projectId;
        this.metricName = metricName.trim();
        this.timeRange = timeRange;
        this.method = method;
        this.serviceId = Optional.ofNullable(serviceId);
    }
}

