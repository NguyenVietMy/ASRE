package com.asre.asre.infra.jpa.alerts;

import com.asre.asre.domain.alerts.AlertRule;
import com.asre.asre.domain.alerts.AlertRuleRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaAlertRuleRepository implements AlertRuleRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AlertRuleEntityMapper mapper;
    private final ObjectMapper objectMapper;

    public JpaAlertRuleRepository(@Qualifier("supabaseJdbcTemplate") JdbcTemplate jdbcTemplate,
                                  AlertRuleEntityMapper mapper,
                                  ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    private RowMapper<AlertRuleEntity> createRowMapper() {
        return (rs, rowNum) -> {
            AlertRuleEntity entity = new AlertRuleEntity();
            entity.setId((UUID) rs.getObject("id"));
            entity.setProjectId((UUID) rs.getObject("project_id"));
            entity.setServiceId((UUID) rs.getObject("service_id"));
            entity.setName(rs.getString("name"));
            entity.setMetricName(rs.getString("metric_name"));
            entity.setAggregationStat(rs.getString("aggregation_stat"));
            entity.setOperator(rs.getString("operator"));
            entity.setThreshold(rs.getDouble("threshold"));
            entity.setWindowMinutes(rs.getInt("window_minutes"));
            entity.setDurationMinutes(rs.getInt("duration_minutes"));
            entity.setSeverity(rs.getString("severity"));
            entity.setEnabled(rs.getBoolean("enabled"));
            
            // Parse notification_channels from JSONB array
            try {
                String channelsJson = rs.getString("notification_channels");
                if (channelsJson != null && !channelsJson.isEmpty()) {
                    entity.setNotificationChannels(objectMapper.readValue(channelsJson, 
                        new TypeReference<List<String>>() {}));
                } else {
                    entity.setNotificationChannels(List.of());
                }
            } catch (Exception e) {
                entity.setNotificationChannels(List.of());
            }
            
            Timestamp createdAt = rs.getTimestamp("created_at");
            entity.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
            return entity;
        };
    }

    @Override
    public AlertRule save(AlertRule alertRule) {
        AlertRuleEntity entity = mapper.toEntity(alertRule);
        
        if (entity.getId() == null) {
            // Insert new alert rule
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();
            
            try {
                String channelsJson = objectMapper.writeValueAsString(
                    entity.getNotificationChannels() != null ? entity.getNotificationChannels() : List.of());
                
                String sql = "INSERT INTO alert_rules (id, project_id, service_id, name, metric_name, " +
                        "operator, threshold, window_minutes, duration_minutes, enabled, created_at, " +
                        "aggregation_stat, severity, notification_channels) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb)";
                
                jdbcTemplate.update(sql,
                        id, entity.getProjectId(), entity.getServiceId(), entity.getName(),
                        entity.getMetricName(), entity.getOperator(), entity.getThreshold(),
                        entity.getWindowMinutes(), entity.getDurationMinutes(), entity.getEnabled(),
                        Timestamp.from(now), entity.getAggregationStat(), entity.getSeverity(), channelsJson);
                
                entity.setId(id);
                entity.setCreatedAt(now);
            } catch (Exception e) {
                throw new RuntimeException("Failed to save alert rule", e);
            }
        } else {
            // Update existing alert rule
            try {
                String channelsJson = objectMapper.writeValueAsString(
                    entity.getNotificationChannels() != null ? entity.getNotificationChannels() : List.of());
                
                String sql = "UPDATE alert_rules SET name = ?, metric_name = ?, operator = ?, " +
                        "threshold = ?, window_minutes = ?, duration_minutes = ?, enabled = ?, " +
                        "aggregation_stat = ?, severity = ?, notification_channels = ?::jsonb " +
                        "WHERE id = ?";
                
                jdbcTemplate.update(sql,
                        entity.getName(), entity.getMetricName(), entity.getOperator(),
                        entity.getThreshold(), entity.getWindowMinutes(), entity.getDurationMinutes(),
                        entity.getEnabled(), entity.getAggregationStat(), entity.getSeverity(),
                        channelsJson, entity.getId());
            } catch (Exception e) {
                throw new RuntimeException("Failed to update alert rule", e);
            }
        }
        
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<AlertRule> findById(UUID id) {
        String sql = "SELECT id, project_id, service_id, name, metric_name, operator, threshold, " +
                "window_minutes, duration_minutes, enabled, created_at, aggregation_stat, severity, " +
                "notification_channels FROM alert_rules WHERE id = ?";
        try {
            AlertRuleEntity entity = jdbcTemplate.queryForObject(sql, createRowMapper(), id);
            return Optional.ofNullable(mapper.toDomain(entity));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<AlertRule> findByProjectId(UUID projectId) {
        String sql = "SELECT id, project_id, service_id, name, metric_name, operator, threshold, " +
                "window_minutes, duration_minutes, enabled, created_at, aggregation_stat, severity, " +
                "notification_channels FROM alert_rules WHERE project_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, createRowMapper()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<AlertRule> findByServiceId(UUID serviceId) {
        String sql = "SELECT id, project_id, service_id, name, metric_name, operator, threshold, " +
                "window_minutes, duration_minutes, enabled, created_at, aggregation_stat, severity, " +
                "notification_channels FROM alert_rules WHERE service_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, createRowMapper()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<AlertRule> findActiveRules() {
        String sql = "SELECT id, project_id, service_id, name, metric_name, operator, threshold, " +
                "window_minutes, duration_minutes, enabled, created_at, aggregation_stat, severity, " +
                "notification_channels FROM alert_rules WHERE enabled = true ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, createRowMapper()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM alert_rules WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}

