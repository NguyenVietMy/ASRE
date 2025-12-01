package com.asre.asre.domain.logs;

import java.util.List;

/**
 * Domain service interface for log pattern clustering.
 * Similar to AnomalyDetector - domain defines meaning, infra implements.
 * Clustering is typically asynchronous (background jobs).
 */
public interface LogPatternClusterer {
    /**
     * Cluster logs by pattern for a given query/segment.
     * This is the domain definition - "cluster logs by pattern".
     * 
     * @param query The log query to cluster
     * @return List of log clusters
     */
    List<LogCluster> clusterLogs(LogQuery query);

    /**
     * Domain entity representing a log cluster.
     */
    record LogCluster(
            LogClusterId clusterId,
            String pattern, // Template, e.g., "GET /users/{id} failed with 500"
            List<com.asre.asre.domain.ingestion.LogEntry> exampleLogs,
            long count, // Frequency
            LogLevel representativeLevel, // Most common level in cluster
            java.time.Instant firstSeen,
            java.time.Instant lastSeen
    ) {
        public LogCluster {
            if (clusterId == null) {
                throw new IllegalArgumentException("Cluster ID cannot be null");
            }
            if (pattern == null || pattern.isBlank()) {
                throw new IllegalArgumentException("Pattern cannot be null or blank");
            }
            if (exampleLogs == null) {
                throw new IllegalArgumentException("Example logs cannot be null");
            }
            if (count < 0) {
                throw new IllegalArgumentException("Count cannot be negative");
            }
        }
    }

    /**
     * Domain identifier for a log cluster.
     */
    record LogClusterId(String value) {
        public LogClusterId {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Cluster ID cannot be null or blank");
            }
        }
    }
}


