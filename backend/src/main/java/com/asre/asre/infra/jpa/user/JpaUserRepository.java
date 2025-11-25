package com.asre.asre.infra.jpa.user;

import com.asre.asre.domain.user.User;
import com.asre.asre.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final UserEntityMapper mapper;

    public JpaUserRepository(@Qualifier("supabaseJdbcTemplate") JdbcTemplate jdbcTemplate,
            UserEntityMapper mapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
    }

    private static final RowMapper<UserEntity> USER_ENTITY_ROW_MAPPER = (rs, rowNum) -> {
        UserEntity entity = new UserEntity();
        entity.setId((UUID) rs.getObject("id"));
        entity.setEmail(rs.getString("email"));
        entity.setPasswordHash(rs.getString("password_hash"));
        entity.setRole(rs.getString("role"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        entity.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        return entity;
    };

    @Override
    public User save(User user) {
        UserEntity entity = mapper.toEntity(user);
        if (entity.getId() == null) {
            // Insert new user
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();
            // Cast role string to user_role enum type (database enum expects lowercase)
            String roleValue = entity.getRole() != null ? entity.getRole().toLowerCase() : "member";
            String sql = "INSERT INTO users (id, email, password_hash, role, created_at) VALUES (?, ?, ?, ?::user_role, ?)";
            jdbcTemplate.update(sql, id, entity.getEmail(), entity.getPasswordHash(), roleValue,
                    Timestamp.from(now));
            entity.setId(id);
            entity.setCreatedAt(now);
        } else {
            // Update existing user
            String roleValue = entity.getRole() != null ? entity.getRole().toLowerCase() : "member";
            String sql = "UPDATE users SET email = ?, password_hash = ?, role = ?::user_role WHERE id = ?";
            jdbcTemplate.update(sql, entity.getEmail(), entity.getPasswordHash(), roleValue, entity.getId());
        }
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<User> findById(UUID id) {
        String sql = "SELECT id, email, password_hash, role, created_at FROM users WHERE id = ?";
        try {
            UserEntity entity = jdbcTemplate.queryForObject(sql, USER_ENTITY_ROW_MAPPER, id);
            return Optional.ofNullable(mapper.toDomain(entity));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, email, password_hash, role, created_at FROM users WHERE email = ?";
        try {
            UserEntity entity = jdbcTemplate.queryForObject(sql, USER_ENTITY_ROW_MAPPER, email);
            return Optional.ofNullable(mapper.toDomain(entity));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
