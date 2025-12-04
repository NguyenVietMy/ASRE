package com.asre.asre.application.apikey;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface for caching API key to project_id mappings.
 * Implementation will use Redis.
 */
public interface ApiKeyCachePort {
    Optional<UUID> getProjectId(String apiKey);

    void cacheProjectId(String apiKey, UUID projectId, int ttlSeconds);

    void invalidate(String apiKey);
}

