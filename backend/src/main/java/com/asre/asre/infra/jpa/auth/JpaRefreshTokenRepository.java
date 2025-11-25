package com.asre.asre.infra.jpa.auth;

import com.asre.asre.domain.auth.RefreshToken;
import com.asre.asre.domain.auth.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaRefreshTokenRepository implements RefreshTokenRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RefreshTokenEntityMapper mapper;

    public JpaRefreshTokenRepository(@Qualifier("supabaseJdbcTemplate") JdbcTemplate jdbcTemplate,
            RefreshTokenEntityMapper mapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
    }

    private static final RowMapper<RefreshTokenEntity> REFRESH_TOKEN_ENTITY_ROW_MAPPER = (rs, rowNum) -> {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId((UUID) rs.getObject("id"));
        entity.setUserId((UUID) rs.getObject("user_id"));
        entity.setTokenHash(rs.getString("token_hash"));
        Timestamp expiresAt = rs.getTimestamp("expires_at");
        entity.setExpiresAt(expiresAt != null ? expiresAt.toInstant() : null);
        Timestamp createdAt = rs.getTimestamp("created_at");
        entity.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        return entity;
    };

    @Override
    public RefreshToken save(RefreshToken token) {
        RefreshTokenEntity entity = mapper.toEntity(token);
        if (entity.getId() == null) {
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();
            String sql = "INSERT INTO refresh_tokens (id, user_id, token_hash, expires_at, created_at) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, id, entity.getUserId(), entity.getTokenHash(),
                    entity.getExpiresAt() != null ? Timestamp.from(entity.getExpiresAt()) : null,
                    Timestamp.from(now));
            entity.setId(id);
            entity.setCreatedAt(now);
        }
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        String sql = "SELECT id, user_id, token_hash, expires_at, created_at FROM refresh_tokens WHERE token_hash = ?";
        try {
            RefreshTokenEntity entity = jdbcTemplate.queryForObject(sql, REFRESH_TOKEN_ENTITY_ROW_MAPPER, tokenHash);
            return Optional.ofNullable(mapper.toDomain(entity));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteByTokenHash(String tokenHash) {
        String sql = "DELETE FROM refresh_tokens WHERE token_hash = ?";
        jdbcTemplate.update(sql, tokenHash);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        String sql = "DELETE FROM refresh_tokens WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    @Override
    public void deleteExpiredTokens() {
        String sql = "DELETE FROM refresh_tokens WHERE expires_at < ?";
        jdbcTemplate.update(sql, Timestamp.from(Instant.now()));
    }
}
