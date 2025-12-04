package com.asre.asre.infra.jpa.project;

import com.asre.asre.domain.project.Project;
import com.asre.asre.domain.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaProjectRepository implements ProjectRepository {

    @Qualifier("supabaseJdbcTemplate")
    private final JdbcTemplate jdbcTemplate;
    private final ProjectEntityMapper mapper;

    private static final String FIND_BY_ID_SQL = """
            SELECT id, name, description, api_key, owner_user_id, rate_limit_per_minute, created_at, deleted_at
            FROM projects
            WHERE id = ?
            """;

    private static final String FIND_BY_ID_NOT_DELETED_SQL = """
            SELECT id, name, description, api_key, owner_user_id, rate_limit_per_minute, created_at, deleted_at
            FROM projects
            WHERE id = ? AND deleted_at IS NULL
            """;

    private static final String FIND_BY_API_KEY_SQL = """
            SELECT id, name, description, api_key, owner_user_id, rate_limit_per_minute, created_at, deleted_at
            FROM projects
            WHERE api_key = ? AND deleted_at IS NULL
            """;

    private static final String FIND_BY_OWNER_SQL = """
            SELECT id, name, description, api_key, owner_user_id, rate_limit_per_minute, created_at, deleted_at
            FROM projects
            WHERE owner_user_id = ? AND deleted_at IS NULL
            ORDER BY created_at DESC
            """;

    private static final String INSERT_SQL = """
            INSERT INTO projects (id, name, description, api_key, owner_user_id, rate_limit_per_minute, created_at, deleted_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                name = EXCLUDED.name,
                description = EXCLUDED.description,
                rate_limit_per_minute = EXCLUDED.rate_limit_per_minute,
                deleted_at = EXCLUDED.deleted_at
            """;

    private static final String SOFT_DELETE_SQL = """
            UPDATE projects SET deleted_at = ? WHERE id = ?
            """;

    private final RowMapper<ProjectEntity> rowMapper = new RowMapper<ProjectEntity>() {
        @Override
        public ProjectEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            java.sql.Timestamp deletedAtTs = rs.getTimestamp("deleted_at");
            return new ProjectEntity(
                    UUID.fromString(rs.getString("id")),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("api_key"),
                    UUID.fromString(rs.getString("owner_user_id")),
                    rs.getInt("rate_limit_per_minute"),
                    rs.getTimestamp("created_at").toInstant(),
                    deletedAtTs != null ? deletedAtTs.toInstant() : null);
        }
    };

    @Override
    public Optional<Project> findById(UUID id) {
        try {
            ProjectEntity entity = jdbcTemplate.queryForObject(
                    FIND_BY_ID_SQL,
                    rowMapper,
                    id);
            return Optional.ofNullable(entity).map(mapper::toDomain);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Project> findByIdAndNotDeleted(UUID id) {
        try {
            ProjectEntity entity = jdbcTemplate.queryForObject(
                    FIND_BY_ID_NOT_DELETED_SQL,
                    rowMapper,
                    id);
            return Optional.ofNullable(entity).map(mapper::toDomain);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Project> findByOwnerUserId(UUID ownerUserId) {
        List<ProjectEntity> entities = jdbcTemplate.query(
                FIND_BY_OWNER_SQL,
                rowMapper,
                ownerUserId);
        return entities.stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Project> findByApiKey(String apiKey) {
        try {
            ProjectEntity entity = jdbcTemplate.queryForObject(
                    FIND_BY_API_KEY_SQL,
                    rowMapper,
                    apiKey);
            return Optional.ofNullable(entity).map(mapper::toDomain);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Project save(Project project) {
        ProjectEntity entity = mapper.toEntity(project);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }

        jdbcTemplate.update(INSERT_SQL,
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getApiKey(),
                entity.getOwnerUserId(),
                entity.getRateLimitPerMinute(),
                entity.getCreatedAt(),
                entity.getDeletedAt());

        return mapper.toDomain(entity);
    }

    public void softDelete(UUID projectId) {
        jdbcTemplate.update(SOFT_DELETE_SQL, Instant.now(), projectId);
    }
}
