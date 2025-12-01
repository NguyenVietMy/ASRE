package com.asre.asre.infra.jpa.logs;

import com.asre.asre.domain.logs.LogSamplingPolicy;
import com.asre.asre.domain.logs.LogSamplingPolicyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory implementation of LogSamplingPolicyRepository.
 * Returns default policies for all projects.
 * TODO: Add database persistence for custom policies per project.
 */
@Repository
@Slf4j
public class JpaLogSamplingPolicyRepository implements LogSamplingPolicyRepository {

    // In-memory cache for custom policies (can be replaced with DB later)
    private final Map<UUID, LogSamplingPolicy> customPolicies = new ConcurrentHashMap<>();

    @Override
    public LogSamplingPolicy findByProjectId(UUID projectId) {
        // Check for custom policy first
        LogSamplingPolicy customPolicy = customPolicies.get(projectId);
        if (customPolicy != null) {
            return customPolicy;
        }

        // Return default policy
        return LogSamplingPolicy.defaultPolicy(projectId);
    }

    @Override
    public LogSamplingPolicy save(LogSamplingPolicy policy) {
        customPolicies.put(policy.getProjectId(), policy);
        log.info("Saved custom sampling policy for project: {}", policy.getProjectId());
        return policy;
    }
}


