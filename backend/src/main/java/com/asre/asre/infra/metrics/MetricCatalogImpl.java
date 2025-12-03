package com.asre.asre.infra.metrics;

import com.asre.asre.domain.alerts.MetricCatalog;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Simple implementation of MetricCatalog.
 * Checks if a metric exists by querying TimescaleDB for recent data.
 * For MVP, we consider a metric to exist if there's any data in the last 30 days.
 */
@Component
public class MetricCatalogImpl implements MetricCatalog {

    private final JdbcTemplate timescaleJdbcTemplate;

    public MetricCatalogImpl(@Qualifier("timescaledbJdbcTemplate") JdbcTemplate timescaleJdbcTemplate) {
        this.timescaleJdbcTemplate = timescaleJdbcTemplate;
    }

    @Override
    public boolean metricExists(UUID projectId, String metricName) {
        try {
            // Check if metric has any data in the last 30 days
            String sql = "SELECT COUNT(*) FROM metrics " +
                    "WHERE project_id = ? AND metric_name = ? " +
                    "AND time > NOW() - INTERVAL '30 days' " +
                    "LIMIT 1";
            
            Integer count = timescaleJdbcTemplate.queryForObject(sql, Integer.class, projectId, metricName);
            return count != null && count > 0;
        } catch (Exception e) {
            // If query fails, assume metric doesn't exist
            return false;
        }
    }
}

