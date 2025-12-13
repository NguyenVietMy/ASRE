package com.asre.asre.infra.jpa.alerts;

import com.asre.asre.domain.alerts.Incident;
import com.asre.asre.domain.alerts.IncidentRepository;
import com.asre.asre.domain.alerts.IncidentStatus;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class JpaIncidentRepository implements IncidentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final IncidentEntityMapper mapper;

    public JpaIncidentRepository(@Qualifier("supabaseJdbcTemplate") JdbcTemplate jdbcTemplate,
            IncidentEntityMapper mapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
    }

    private RowMapper<IncidentEntity> createRowMapper() {
        return (rs, rowNum) -> {
            IncidentEntity entity = new IncidentEntity();
            entity.setId((UUID) rs.getObject("id"));
            entity.setProjectId((UUID) rs.getObject("project_id"));
            entity.setServiceId((UUID) rs.getObject("service_id"));
            entity.setRuleId((UUID) rs.getObject("rule_id"));
            entity.setStatus(rs.getString("status"));
            entity.setSeverity(rs.getString("severity"));

            Timestamp startedAt = rs.getTimestamp("started_at");
            entity.setStartedAt(startedAt != null ? startedAt.toInstant() : null);

            Timestamp resolvedAt = rs.getTimestamp("resolved_at");
            entity.setResolvedAt(resolvedAt != null ? resolvedAt.toInstant() : null);

            entity.setSummary(rs.getString("summary"));
            entity.setAiSummary(rs.getString("ai_summary"));

            // Use started_at as created_at since incidents table doesn't have
            // created_at/updated_at
            entity.setCreatedAt(startedAt != null ? startedAt.toInstant() : Instant.now());
            entity.setUpdatedAt(resolvedAt != null ? resolvedAt.toInstant()
                    : (startedAt != null ? startedAt.toInstant() : Instant.now()));

            return entity;
        };
    }

    @Override
    public Incident save(Incident incident) {
        IncidentEntity entity = mapper.toEntity(incident);

        if (entity.getId() == null) {
            // Insert new incident
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();

            String sql = "INSERT INTO incidents (id, project_id, service_id, rule_id, status, severity, " +
                    "started_at, resolved_at, summary, ai_summary) " +
                    "VALUES (?, ?, ?, ?, ?::incident_status, ?::incident_severity, ?, ?, ?, ?)";

            jdbcTemplate.update(sql,
                    id, entity.getProjectId(), entity.getServiceId(), entity.getRuleId(),
                    entity.getStatus(), entity.getSeverity(),
                    entity.getStartedAt() != null ? Timestamp.from(entity.getStartedAt()) : Timestamp.from(now),
                    entity.getResolvedAt() != null ? Timestamp.from(entity.getResolvedAt()) : null,
                    entity.getSummary(), entity.getAiSummary());

            entity.setId(id);
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
        } else {
            // Update existing incident
            String sql = "UPDATE incidents SET status = ?::incident_status, severity = ?::incident_severity, " +
                    "resolved_at = ?, summary = ?, ai_summary = ? WHERE id = ?";

            jdbcTemplate.update(sql,
                    entity.getStatus(), entity.getSeverity(),
                    entity.getResolvedAt() != null ? Timestamp.from(entity.getResolvedAt()) : null,
                    entity.getSummary(), entity.getAiSummary(),
                    entity.getId());

            entity.setUpdatedAt(Instant.now());
        }

        return mapper.toDomain(entity);
    }

    @Override
    public Optional<Incident> findById(UUID id) {
        String sql = "SELECT id, project_id, service_id, rule_id, status, severity, started_at, " +
                "resolved_at, summary, ai_summary FROM incidents WHERE id = ?";
        try {
            IncidentEntity entity = jdbcTemplate.queryForObject(sql, createRowMapper(), id);
            return Optional.ofNullable(mapper.toDomain(entity));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Incident> findByProjectId(UUID projectId) {
        String sql = "SELECT id, project_id, service_id, rule_id, status, severity, started_at, " +
                "resolved_at, summary, ai_summary FROM incidents " +
                "WHERE project_id = ? ORDER BY started_at DESC";
        return jdbcTemplate.query(sql, createRowMapper()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Incident> findByServiceId(UUID serviceId) {
        String sql = "SELECT id, project_id, service_id, rule_id, status, severity, started_at, " +
                "resolved_at, summary, ai_summary FROM incidents " +
                "WHERE service_id = ? ORDER BY started_at DESC";
        try {
            return jdbcTemplate.query(sql, createRowMapper(), serviceId).stream()
                    .map(mapper::toDomain)
                    .toList();
        } catch (Exception e) {
            log.error("Error querying incidents by service_id: {}", serviceId, e);
            throw e;
        }
    }

    @Override
    public List<Incident> findByRuleId(UUID ruleId) {
        String sql = "SELECT id, project_id, service_id, rule_id, status, severity, started_at, " +
                "resolved_at, summary, ai_summary FROM incidents " +
                "WHERE rule_id = ? ORDER BY started_at DESC";
        return jdbcTemplate.query(sql, createRowMapper()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Incident> findOpenIncident(UUID projectId, UUID ruleId, UUID serviceId) {
        String sql = "SELECT id, project_id, service_id, rule_id, status, severity, started_at, " +
                "resolved_at, summary, ai_summary FROM incidents " +
                "WHERE project_id = ? AND rule_id = ? AND service_id = ? AND status != 'RESOLVED' " +
                "ORDER BY started_at DESC LIMIT 1";
        try {
            IncidentEntity entity = jdbcTemplate.queryForObject(sql, createRowMapper(),
                    projectId, ruleId, serviceId);
            return Optional.ofNullable(mapper.toDomain(entity));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Incident> findByProjectIdAndStatus(UUID projectId, IncidentStatus status) {
        String sql = "SELECT id, project_id, service_id, rule_id, status, severity, started_at, " +
                "resolved_at, summary, ai_summary FROM incidents " +
                "WHERE project_id = ? AND status = ?::incident_status ORDER BY started_at DESC";
        return jdbcTemplate.query(sql, createRowMapper(), projectId, status.name()).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
