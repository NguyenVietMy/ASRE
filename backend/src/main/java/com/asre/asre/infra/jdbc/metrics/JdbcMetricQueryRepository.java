package com.asre.asre.infra.jdbc.metrics;

import com.asre.asre.domain.metrics.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JDBC-based implementation of MetricQueryRepository.
 * Executes queries against TimescaleDB using time_bucket for aggregation.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class JdbcMetricQueryRepository implements MetricQueryRepository {

    @Qualifier("timescaledbJdbcTemplate")
    private final JdbcTemplate timescaleJdbcTemplate;

    @Override
    public MetricQueryResult executeQuery(MetricQuery query) {
        // Enforce project isolation at infrastructure level
        String sql = buildQuerySql(query);
        List<Object> params = buildQueryParams(query);

        List<TimeSeriesPoint> dataPoints = timescaleJdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Timestamp ts = rs.getTimestamp("bucket");
                    Double value = rs.getDouble("value");
                    return new TimeSeriesPoint(ts.toInstant(), value);
                },
                params.toArray()
        );

        return new MetricQueryResult(
                query.getMetricName(),
                query.getAggregationType(),
                dataPoints
        );
    }

    @Override
    public MultiMetricQueryResult executeMultiQuery(MultiMetricQuery query) {
        List<MetricQueryResult> results = new ArrayList<>();

        for (MultiMetricQuery.SingleMetricQuery singleQuery : query.getQueries()) {
            // Build individual query for each metric
            MetricQuery metricQuery = new MetricQuery(
                    query.getProjectId(),
                    singleQuery.getMetricName(),
                    singleQuery.getAggregationType(),
                    query.getTimeRange(),
                    query.getRollupPeriod(),
                    singleQuery.getServiceId(),
                    null
            );

            MetricQueryResult result = executeQuery(metricQuery);

            // Align timestamps if requested
            if (query.isAlignTimestamps()) {
                result = alignTimestamps(result, query.getRollupPeriod());
            }

            results.add(result);
        }

        return new MultiMetricQueryResult(results);
    }

    @Override
    public HistogramResult executeHistogramQuery(HistogramQuery query) {
        // Calculate min/max from data first
        String minMaxSql = """
                SELECT MIN(value) as min_val, MAX(value) as max_val
                FROM metrics
                WHERE project_id = ? AND metric_name = ?
                AND time >= ? AND time <= ?
                """;

        if (query.getServiceId().isPresent()) {
            minMaxSql += " AND service_id = ?";
        }

        List<Object> minMaxParams = new ArrayList<>();
        minMaxParams.add(query.getProjectId());
        minMaxParams.add(query.getMetricName());
        minMaxParams.add(Timestamp.from(query.getTimeRange().getStartTime()));
        minMaxParams.add(Timestamp.from(query.getTimeRange().getEndTime()));
        if (query.getServiceId().isPresent()) {
            minMaxParams.add(query.getServiceId().get());
        }

        Double[] minMax = timescaleJdbcTemplate.queryForObject(minMaxSql, (rs, rowNum) -> {
            return new Double[]{rs.getDouble("min_val"), rs.getDouble("max_val")};
        }, minMaxParams.toArray());

        if (minMax == null || minMax[0] == null || minMax[1] == null) {
            return new HistogramResult(query.getMetricName(), List.of());
        }

        double min = minMax[0];
        double max = minMax[1];
        double binWidth = (max - min) / query.getBins();

        // Build histogram query
        StringBuilder sql = new StringBuilder("""
                SELECT
                    CASE
                """);

        for (int i = 0; i < query.getBins(); i++) {
            double binMin = min + (i * binWidth);
            double binMax = (i == query.getBins() - 1) ? max + 0.0001 : min + ((i + 1) * binWidth);
            sql.append(String.format("""
                        WHEN value >= %f AND value < %f THEN %d
                    """, binMin, binMax, i));
        }

        sql.append("""
                        ELSE -1
                    END as bin,
                    COUNT(*) as count
                FROM metrics
                WHERE project_id = ? AND metric_name = ?
                AND time >= ? AND time <= ?
                """);

        if (query.getServiceId().isPresent()) {
            sql.append(" AND service_id = ?");
        }

        sql.append("""
                GROUP BY bin
                ORDER BY bin
                """);

        List<Object> params = new ArrayList<>();
        params.add(query.getProjectId());
        params.add(query.getMetricName());
        params.add(Timestamp.from(query.getTimeRange().getStartTime()));
        params.add(Timestamp.from(query.getTimeRange().getEndTime()));
        if (query.getServiceId().isPresent()) {
            params.add(query.getServiceId().get());
        }

        List<HistogramBin> bins = timescaleJdbcTemplate.query(
                sql.toString(),
                (rs, rowNum) -> {
                    int binIndex = rs.getInt("bin");
                    long count = rs.getLong("count");
                    double binMin = min + (binIndex * binWidth);
                    double binMax = (binIndex == query.getBins() - 1) ? max : min + ((binIndex + 1) * binWidth);
                    return new HistogramBin(binMin, binMax, count);
                },
                params.toArray()
        );

        return new HistogramResult(query.getMetricName(), bins);
    }

    private String buildQuerySql(MetricQuery query) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append("time_bucket(?, time) as bucket, ");

        // Add aggregation based on type
        if (query.getAggregationType().isPercentile()) {
            double percentile = switch (query.getAggregationType()) {
                case P50 -> 0.50;
                case P95 -> 0.95;
                case P99 -> 0.99;
                default -> 0.50;
            };
            sql.append(String.format("percentile_cont(%.2f) WITHIN GROUP (ORDER BY value) as value", percentile));
        } else {
            switch (query.getAggregationType()) {
                case AVG -> sql.append("AVG(value) as value");
                case MIN -> sql.append("MIN(value) as value");
                case MAX -> sql.append("MAX(value) as value");
                case SUM -> sql.append("SUM(value) as value");
                case COUNT -> sql.append("COUNT(*) as value");
                default -> sql.append("AVG(value) as value");
            }
        }

        sql.append(" FROM metrics WHERE project_id = ? AND metric_name = ?");
        sql.append(" AND time >= ? AND time <= ?");

        if (query.getServiceId().isPresent()) {
            sql.append(" AND service_id = ?");
        }

        // TODO: Add tag filtering if tags are provided

        sql.append(" GROUP BY bucket ORDER BY bucket");

        return sql.toString();
    }

    private List<Object> buildQueryParams(MetricQuery query) {
        List<Object> params = new ArrayList<>();
        // Rollup period as interval string (e.g., '1 minute', '5 minutes')
        String interval = formatInterval(query.getRollupPeriod());
        params.add(interval);
        params.add(query.getProjectId());
        params.add(query.getMetricName());
        params.add(Timestamp.from(query.getTimeRange().getStartTime()));
        params.add(Timestamp.from(query.getTimeRange().getEndTime()));
        if (query.getServiceId().isPresent()) {
            params.add(query.getServiceId().get());
        }
        return params;
    }

    private String formatInterval(RollupPeriod rollupPeriod) {
        long minutes = rollupPeriod.getDuration().toMinutes();
        if (minutes < 60) {
            return minutes + " minute" + (minutes != 1 ? "s" : "");
        }
        long hours = rollupPeriod.getDuration().toHours();
        if (hours < 24) {
            return hours + " hour" + (hours != 1 ? "s" : "");
        }
        long days = rollupPeriod.getDuration().toDays();
        return days + " day" + (days != 1 ? "s" : "");
    }

    private MetricQueryResult alignTimestamps(MetricQueryResult result, RollupPeriod rollupPeriod) {
        // Simple alignment - in production, this would align all series to common timestamps
        // For now, just return as-is
        return result;
    }
}

