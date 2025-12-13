package com.asre.asre.application.project;

import com.asre.asre.application.apikey.ApiKeyCachePort;
import com.asre.asre.domain.project.ApiKeyGeneratorPort;
import com.asre.asre.domain.project.Project;
import com.asre.asre.domain.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Application service for project management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ApiKeyGeneratorPort apiKeyGenerator;
    private final ApiKeyCachePort apiKeyCache;

    @Value("${api.key.cache.ttl-seconds:300}")
    private int cacheTtlSeconds;

    /**
     * Creates a new project with auto-generated API key.
     */
    @Transactional
    public Project createProject(CreateProjectCommand command) {
        // Generate API key
        String apiKey = apiKeyGenerator.generateApiKey();

        // Create project
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setName(command.getName());
        project.setDescription(command.getDescription());
        project.setApiKey(apiKey);
        project.setOwnerUserId(command.getOwnerUserId());
        project.setRateLimitPerMinute(1000); // Default rate limit
        project.setCreatedAt(Instant.now());
        project.setDeletedAt(null);

        return projectRepository.save(project);
    }

    /**
     * Lists all projects owned by a user.
     */
    public List<Project> listProjects(UUID ownerUserId) {
        return projectRepository.findByOwnerUserId(ownerUserId);
    }

    /**
     * Gets a project by ID, ensuring it belongs to the user.
     */
    public Project getProject(UUID projectId, UUID ownerUserId) {
        Project project = projectRepository.findByIdAndNotDeleted(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        project.ensureOwnedBy(ownerUserId);
        return project;
    }

    /**
     * Validates that a project exists and belongs to the user.
     * Throws IllegalArgumentException if not found or not owned.
     */
    public void validateProjectOwnership(UUID projectId, UUID ownerUserId) {
        getProject(projectId, ownerUserId);
    }

    /**
     * Updates a project.
     */
    @Transactional
    public Project updateProject(UpdateProjectCommand command) {
        Project project = getProject(command.getProjectId(), command.getOwnerUserId());

        if (command.getName() != null) {
            project.setName(command.getName());
        }
        if (command.getDescription() != null) {
            project.setDescription(command.getDescription());
        }

        return projectRepository.save(project);
    }

    /**
     * Soft deletes a project.
     */
    @Transactional
    public void deleteProject(UUID projectId, UUID ownerUserId) {
        Project project = getProject(projectId, ownerUserId);
        project.softDelete();
        projectRepository.save(project);
    }

    /**
     * Regenerates API key for a project.
     * Returns the new key (only shown once).
     */
    @Transactional
    public String regenerateApiKey(UUID projectId, UUID ownerUserId) {
        Project project = getProject(projectId, ownerUserId);

        // Get old API key before regenerating
        String oldApiKey = project.getApiKey();

        // Generate new key
        String newApiKey = apiKeyGenerator.generateApiKey();

        // Update project
        project.setApiKey(newApiKey);
        projectRepository.save(project);

        // Invalidate old key from cache immediately
        if (oldApiKey != null && !oldApiKey.isBlank()) {
            apiKeyCache.invalidate(oldApiKey);
            log.info("Invalidated old API key from cache for project: {}", projectId);
        }

        // Cache the new key
        apiKeyCache.cacheProjectId(newApiKey, projectId, cacheTtlSeconds);
        log.info("Cached new API key for project: {}", projectId);

        return newApiKey;
    }
}
