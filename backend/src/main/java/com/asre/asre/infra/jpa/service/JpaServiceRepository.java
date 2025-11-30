package com.asre.asre.infra.jpa.service;

import com.asre.asre.domain.service.Service;
import com.asre.asre.domain.service.ServiceRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaServiceRepository implements ServiceRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ServiceEntityMapper mapper;

    public JpaServiceRepository(@Qualifier("supabaseJdbcTemplate") JdbcTemplate jdbcTemplate,
            ServiceEntityMapper mapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
    }

    private static final RowMapper<ServiceEntity> SERVICE_ENTITY_ROW_MAPPER = (rs, rowNum) -> {
        ServiceEntity entity = new ServiceEntity();
        entity.setId((UUID) rs.getObject("id"));
        entity.setProjectId((UUID) rs.getObject("project_id"));
        entity.setName(rs.getString("name"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        entity.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp lastSeenAt = rs.getTimestamp("last_seen_at");
        entity.setLastSeenAt(lastSeenAt != null ? lastSeenAt.toInstant() : null);
        return entity;
    };

    @Override
    public Optional<Service> findByProjectIdAndName(UUID projectId, String name) {
        String sql = "SELECT id, project_id, name, created_at, last_seen_at FROM services WHERE project_id = ? AND name = ?";
        try {
            ServiceEntity entity = jdbcTemplate.queryForObject(sql, SERVICE_ENTITY_ROW_MAPPER, projectId, name);
            return Optional.ofNullable(mapper.toDomain(entity));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Service> findById(UUID id) {
        String sql = "SELECT id, project_id, name, created_at, last_seen_at FROM services WHERE id = ?";
        try {
            ServiceEntity entity = jdbcTemplate.queryForObject(sql, SERVICE_ENTITY_ROW_MAPPER, id);
            return Optional.ofNullable(mapper.toDomain(entity));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Service save(Service service) {
        ServiceEntity entity = mapper.toEntity(service);
        if (entity.getId() == null) {
            // Insert new service
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();
            String sql = "INSERT INTO services (id, project_id, name, created_at, last_seen_at) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, id, entity.getProjectId(), entity.getName(),
                    Timestamp.from(now), Timestamp.from(now));
            entity.setId(id);
            entity.setCreatedAt(now);
            entity.setLastSeenAt(now);
        } else {
            // Check if service exists
            Optional<Service> existing = findById(entity.getId());
            if (existing.isEmpty()) {
                // Insert with provided ID
                Instant now = entity.getCreatedAt() != null ? entity.getCreatedAt() : Instant.now();
                String sql = "INSERT INTO services (id, project_id, name, created_at, last_seen_at) VALUES (?, ?, ?, ?, ?)";
                jdbcTemplate.update(sql, entity.getId(), entity.getProjectId(), entity.getName(),
                        Timestamp.from(now), Timestamp.from(entity.getLastSeenAt()));
                entity.setCreatedAt(now);
            } else {
                // Update existing service
                String sql = "UPDATE services SET name = ?, last_seen_at = ? WHERE id = ?";
                jdbcTemplate.update(sql, entity.getName(), Timestamp.from(entity.getLastSeenAt()), entity.getId());
            }
        }
        return mapper.toDomain(entity);
    }

    @Override
    public boolean existsByProjectIdAndName(UUID projectId, String name) {
        String sql = "SELECT COUNT(*) FROM services WHERE project_id = ? AND name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, projectId, name);
        return count != null && count > 0;
    }
}

