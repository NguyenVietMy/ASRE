package com.asre.asre.application.ratelimit;

import com.asre.asre.domain.project.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimiterService {

    private final RateLimiterPort rateLimiter;

    /**
     * Checks if request is within rate limit for the given project.
     * @param projectId Project ID
     * @param rateLimitPerMinute Rate limit per minute for this project
     * @return true if within limit, false if rate limit exceeded
     */
    public boolean isWithinLimit(UUID projectId, int rateLimitPerMinute) {
        return rateLimiter.isAllowed(projectId.toString(), rateLimitPerMinute);
    }

    /**
     * Checks rate limit using project object.
     */
    public boolean isWithinLimit(Project project) {
        if (project == null || project.getRateLimitPerMinute() == null) {
            return false;
        }
        return isWithinLimit(project.getId(), project.getRateLimitPerMinute());
    }
}


