package com.asre.asre.infra.jpa.auth;

import com.asre.asre.domain.auth.RefreshToken;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenEntityMapper {

    public RefreshTokenEntity toEntity(RefreshToken domain) {
        if (domain == null) {
            return null;
        }
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setTokenHash(domain.getTokenHash());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    public RefreshToken toDomain(RefreshTokenEntity entity) {
        if (entity == null) {
            return null;
        }
        RefreshToken domain = new RefreshToken();
        domain.setId(entity.getId());
        domain.setUserId(entity.getUserId());
        domain.setTokenHash(entity.getTokenHash());
        domain.setExpiresAt(entity.getExpiresAt());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}

