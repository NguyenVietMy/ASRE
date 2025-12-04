package com.asre.asre.infra.jpa.project;

import com.asre.asre.domain.project.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectEntityMapper {

    public Project toDomain(ProjectEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Project(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getApiKey(),
                entity.getOwnerUserId(),
                entity.getRateLimitPerMinute(),
                entity.getCreatedAt(),
                entity.getDeletedAt());
    }

    public ProjectEntity toEntity(Project domain) {
        if (domain == null) {
            return null;
        }
        return new ProjectEntity(
                domain.getId(),
                domain.getName(),
                domain.getDescription(),
                domain.getApiKey(),
                domain.getOwnerUserId(),
                domain.getRateLimitPerMinute(),
                domain.getCreatedAt(),
                domain.getDeletedAt());
    }
}
