package com.asre.asre.infra.jpa.user;

import com.asre.asre.domain.user.User;
import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper {

    public UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        UserEntity entity = new UserEntity();
        entity.setId(domain.getId());
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setRole(domain.getRole() != null ? domain.getRole().name() : null);
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        User domain = new User();
        domain.setId(entity.getId());
        domain.setEmail(entity.getEmail());
        domain.setPasswordHash(entity.getPasswordHash());
        domain.setRole(entity.getRole() != null ? User.UserRole.valueOf(entity.getRole().toUpperCase()) : null);
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}

