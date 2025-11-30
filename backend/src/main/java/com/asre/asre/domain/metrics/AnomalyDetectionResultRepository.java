package com.asre.asre.domain.metrics;

import java.util.List;

/**
 * Repository interface for AnomalyDetectionResult.
 * Implementations in infrastructure layer.
 */
public interface AnomalyDetectionResultRepository {
    /**
     * Save an anomaly detection result.
     */
    AnomalyDetectionResult save(AnomalyDetectionResult result);

    /**
     * Find anomaly detection results for a query.
     */
    List<AnomalyDetectionResult> findByQuery(AnomalyDetectionQuery query);
}

