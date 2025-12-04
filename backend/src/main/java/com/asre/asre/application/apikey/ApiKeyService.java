package com.asre.asre.application.apikey;

import com.asre.asre.domain.project.Project;
import com.asre.asre.domain.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyService {

    private final ProjectRepository projectRepository;
    private final ApiKeyCachePort apiKeyCache;

    @Value("${api.key.cache.ttl-seconds:300}")
    private int cacheTtlSeconds;

    /**
     * Validates API key and returns project ID.
     * Uses Redis cache to avoid database lookups.
     */
    public Optional<UUID> validateAndGetProjectId(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }

        // Check cache first
        Optional<UUID> cachedProjectId = apiKeyCache.getProjectId(apiKey);
        if (cachedProjectId.isPresent()) {
            return cachedProjectId;
        }

        // Cache miss - query database
        Optional<Project> project = projectRepository.findByApiKey(apiKey);
        if (project.isEmpty()) {
            return Optional.empty();
        }

        UUID projectId = project.get().getId();

        // Cache the result
        apiKeyCache.cacheProjectId(apiKey, projectId, cacheTtlSeconds);

        return Optional.of(projectId);
    }

    /**
     * Gets project by API key (for rate limit lookup).
     */
    public Optional<Project> getProjectByApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }

        // Check cache first
        Optional<UUID> cachedProjectId = apiKeyCache.getProjectId(apiKey);
        if (cachedProjectId.isPresent()) {
            return projectRepository.findById(cachedProjectId.get());
        }

        // Cache miss - query database
        Optional<Project> project = projectRepository.findByApiKey(apiKey);
        if (project.isPresent()) {
            // Cache the result
            apiKeyCache.cacheProjectId(apiKey, project.get().getId(), cacheTtlSeconds);
        }

        return project;
    }
}
