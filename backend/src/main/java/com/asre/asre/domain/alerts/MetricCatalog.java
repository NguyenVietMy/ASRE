package com.asre.asre.domain.alerts;

import java.util.UUID;

/**
 * Domain service interface for validating that metrics exist.
 * Implementations in infrastructure layer can query metric metadata or
 * check against a registry of known metrics.
 */
public interface MetricCatalog {
    /**
     * Validates that a metric exists for the given project.
     * @param projectId The project ID
     * @param metricName The metric name to validate
     * @return true if the metric exists, false otherwise
     */
    boolean metricExists(UUID projectId, String metricName);
}

