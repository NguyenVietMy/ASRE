package com.asre.asre.domain.logs;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for LogSamplingPolicy.
 * Implementations in infrastructure layer.
 */
public interface LogSamplingPolicyRepository {
    /**
     * Find sampling policy for a project.
     * Returns default policy if none exists.
     */
    LogSamplingPolicy findByProjectId(UUID projectId);

    /**
     * Save or update a sampling policy.
     */
    LogSamplingPolicy save(LogSamplingPolicy policy);
}


