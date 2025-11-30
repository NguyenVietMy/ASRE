package com.asre.asre.domain.metrics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Domain entity representing an anomaly detection result.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyDetectionResult {
    private UUID id;
    private UUID projectId;
    private UUID serviceId;
    private String metricName;
    private Instant timestamp;
    private Double zScore;
    private Boolean isAnomaly;
    private Map<String, Object> context;
    private Instant createdAt;

    /**
     * Creates a new anomaly detection result.
     * serviceId is optional to support project-wide anomaly detection.
     */
    public static AnomalyDetectionResult create(
            UUID projectId,
            UUID serviceId,
            String metricName,
            Instant timestamp,
            Double zScore,
            Boolean isAnomaly,
            Map<String, Object> context) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        if (metricName == null || metricName.isBlank()) {
            throw new IllegalArgumentException("Metric name cannot be null or blank");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
        if (zScore == null) {
            throw new IllegalArgumentException("Z-score cannot be null");
        }
        if (isAnomaly == null) {
            throw new IllegalArgumentException("Is anomaly flag cannot be null");
        }
        
        AnomalyDetectionResult result = new AnomalyDetectionResult();
        result.setProjectId(projectId);
        result.setServiceId(serviceId); // Can be null for project-wide queries
        result.setMetricName(metricName);
        result.setTimestamp(timestamp);
        result.setZScore(zScore);
        result.setIsAnomaly(isAnomaly);
        result.setContext(context);
        result.setCreatedAt(Instant.now());
        return result;
    }

    /**
     * Validates that this result belongs to the given project.
     */
    public void ensureBelongsToProject(UUID projectId) {
        if (!this.projectId.equals(projectId)) {
            throw new IllegalArgumentException("Anomaly detection result does not belong to the specified project");
        }
    }
}

