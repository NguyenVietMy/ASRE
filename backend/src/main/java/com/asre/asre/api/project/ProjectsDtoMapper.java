package com.asre.asre.api.project;

import com.asre.asre.api.project.dto.ProjectResponse;
import com.asre.asre.domain.project.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectsDtoMapper {

    public ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt());
    }

    public String maskApiKey(String apiKey) {
        // Delegate to application service which uses the port
        // For now, simple masking logic here - could be moved to domain if needed
        if (apiKey == null || apiKey.length() <= 8) {
            return "asre_sk_****";
        }
        int visibleChars = Math.min(6, apiKey.length() - 8);
        String lastChars = apiKey.substring(apiKey.length() - visibleChars);
        return "asre_sk_" + "*".repeat(Math.max(0, apiKey.length() - 8 - visibleChars)) + lastChars;
    }
}
