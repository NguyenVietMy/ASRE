package com.asre.asre.infra.jpa.service;

import com.asre.asre.domain.service.Service;
import org.springframework.stereotype.Component;

/**
 * Mapper between Service domain object and ServiceEntity.
 */
@Component
public class ServiceEntityMapper {

    public ServiceEntity toEntity(Service domain) {
        if (domain == null) {
            return null;
        }
        ServiceEntity entity = new ServiceEntity();
        entity.setId(domain.getId());
        entity.setProjectId(domain.getProjectId());
        entity.setName(domain.getName());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setLastSeenAt(domain.getLastSeenAt());
        return entity;
    }

    public Service toDomain(ServiceEntity entity) {
        if (entity == null) {
            return null;
        }
        Service domain = new Service();
        domain.setId(entity.getId());
        domain.setProjectId(entity.getProjectId());
        domain.setName(entity.getName());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setLastSeenAt(entity.getLastSeenAt());
        return domain;
    }
}

