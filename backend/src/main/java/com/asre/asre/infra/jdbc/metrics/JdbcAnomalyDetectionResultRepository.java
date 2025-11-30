package com.asre.asre.infra.jdbc.metrics;

import com.asre.asre.domain.metrics.AnomalyDetectionQuery;
import com.asre.asre.domain.metrics.AnomalyDetectionResult;
import com.asre.asre.domain.metrics.AnomalyDetectionResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * JDBC implementation for AnomalyDetectionResultRepository.
 * Stores results in TimescaleDB anomaly_detection_results table.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class JdbcAnomalyDetectionResultRepository implements AnomalyDetectionResultRepository {

    @Qualifier("timescaledbJdbcTemplate")
    private final JdbcTemplate timescaleJdbcTemplate;
    private final ObjectMapper objectMapper;

    private static final String INSERT_SQL = """
            INSERT INTO anomaly_detection_results 
            (id, project_id, service_id, metric_name, timestamp, z_score, is_anomaly, context, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?)
            """;

    private static final String SELECT_SQL = """
            SELECT id, project_id, service_id, metric_name, timestamp, z_score, is_anomaly, context, created_at
            FROM anomaly_detection_results
            WHERE project_id = ? AND metric_name = ?
            AND timestamp >= ? AND timestamp <= ?
            """;

    private RowMapper<AnomalyDetectionResult> createRowMapper() {
        return (rs, rowNum) -> {
            AnomalyDetectionResult result = new AnomalyDetectionResult();
            result.setId((UUID) rs.getObject("id"));
            result.setProjectId((UUID) rs.getObject("project_id"));
            result.setServiceId((UUID) rs.getObject("service_id"));
            result.setMetricName(rs.getString("metric_name"));
            Timestamp ts = rs.getTimestamp("timestamp");
            result.setTimestamp(ts != null ? ts.toInstant() : null);
            result.setZScore(rs.getDouble("z_score"));
            result.setIsAnomaly(rs.getBoolean("is_anomaly"));
            
            // Parse JSONB context
            String contextJson = rs.getString("context");
            try {
                if (contextJson != null) {
                    Map<String, Object> context = objectMapper.readValue(contextJson, 
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                    result.setContext(context);
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
            
            Timestamp createdAt = rs.getTimestamp("created_at");
            result.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
            return result;
        };
    }

    @Override
    public AnomalyDetectionResult save(AnomalyDetectionResult result) {
        UUID id = result.getId() != null ? result.getId() : UUID.randomUUID();
        Instant now = result.getCreatedAt() != null ? result.getCreatedAt() : Instant.now();

        try {
            String contextJson = result.getContext() != null 
                    ? objectMapper.writeValueAsString(result.getContext()) 
                    : "{}";

            timescaleJdbcTemplate.update(INSERT_SQL,
                    id,
                    result.getProjectId(),
                    result.getServiceId(),
                    result.getMetricName(),
                    Timestamp.from(result.getTimestamp()),
                    result.getZScore(),
                    result.getIsAnomaly(),
                    contextJson,
                    Timestamp.from(now)
            );

            result.setId(id);
            result.setCreatedAt(now);
            return result;
        } catch (Exception e) {
            log.error("Error saving anomaly detection result", e);
            throw new RuntimeException("Failed to save anomaly detection result", e);
        }
    }

    @Override
    public List<AnomalyDetectionResult> findByQuery(AnomalyDetectionQuery query) {
        StringBuilder sql = new StringBuilder(SELECT_SQL);
        List<Object> params = new java.util.ArrayList<>();
        
        params.add(query.getProjectId());
        params.add(query.getMetricName());
        params.add(Timestamp.from(query.getTimeRange().getStartTime()));
        params.add(Timestamp.from(query.getTimeRange().getEndTime()));

        if (query.getServiceId().isPresent()) {
            sql.append(" AND service_id = ?");
            params.add(query.getServiceId().get());
        }

        sql.append(" ORDER BY timestamp DESC");

        return timescaleJdbcTemplate.query(sql.toString(), createRowMapper(), params.toArray());
    }
}

