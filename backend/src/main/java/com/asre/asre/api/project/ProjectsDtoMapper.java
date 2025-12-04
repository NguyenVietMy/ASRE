package com.asre.asre.api.project;

import com.asre.asre.api.project.dto.ProjectResponse;
import com.asre.asre.domain.project.Project;
import com.asre.asre.infra.security.ApiKeyGenerator;
import org.springframework.stereotype.Component;

@Component
public class ProjectsDtoMapper {

    private final ApiKeyGenerator apiKeyGenerator;

    public ProjectsDtoMapper(ApiKeyGenerator apiKeyGenerator) {
        this.apiKeyGenerator = apiKeyGenerator;
    }

    public ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt());
    }

    public String maskApiKey(String apiKey) {
        return apiKeyGenerator.maskApiKey(apiKey);
    }
}

