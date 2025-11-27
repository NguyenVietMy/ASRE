package com.asre.asre.infra.jdbc;

import com.asre.asre.domain.ingestion.Metric;
import com.asre.asre.domain.ingestion.MetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TimescaleMetricRepository implements MetricRepository {

    private static final String INSERT_SQL = """
            INSERT INTO metrics (time, project_id, service_id, metric_name, value, tags)
            VALUES (?, ?, ?, ?, ?, ?::jsonb)
            """;

    @Qualifier("timescaledbJdbcTemplate")
    private final JdbcTemplate timescaleJdbcTemplate;

    @Override
    public void saveBatch(List<Metric> metrics) {
        if (metrics.isEmpty()) {
            return;
        }

        List<Object[]> batchArgs = metrics.stream()
                .map(metric -> new Object[]{
                        Timestamp.from(metric.getTimestamp()),
                        metric.getProjectId(),
                        metric.getServiceId(),
                        metric.getMetricName(),
                        metric.getValue(),
                        convertTagsToJson(metric.getTags())
                })
                .collect(Collectors.toList());

        try {
            int[] results = timescaleJdbcTemplate.batchUpdate(INSERT_SQL, batchArgs);
            log.debug("Inserted {} metrics into TimescaleDB", results.length);
        } catch (Exception e) {
            log.error("Error inserting metrics batch", e);
            throw new RuntimeException("Failed to insert metrics", e);
        }
    }

    private String convertTagsToJson(Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "{}";
        }
        // Simple JSON conversion - in production, use Jackson ObjectMapper
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            first = false;
        }
        json.append("}");
        return json.toString();
    }
}

