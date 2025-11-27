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
                entity.getApiKey(),
                entity.getOwnerUserId(),
                entity.getRateLimitPerMinute(),
                entity.getCreatedAt()
        );
    }

    public ProjectEntity toEntity(Project domain) {
        if (domain == null) {
            return null;
        }
        return new ProjectEntity(
                domain.getId(),
                domain.getName(),
                domain.getApiKey(),
                domain.getOwnerUserId(),
                domain.getRateLimitPerMinute(),
                domain.getCreatedAt()
        );
    }
}

