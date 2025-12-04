package com.asre.asre.infra.redis;

import com.asre.asre.application.apikey.ApiKeyCachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyCacheImpl implements ApiKeyCachePort {

    private static final String CACHE_KEY_PREFIX = "apikey:project:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public Optional<UUID> getProjectId(String apiKey) {
        try {
            String key = CACHE_KEY_PREFIX + apiKey;
            String projectIdStr = redisTemplate.opsForValue().get(key);
            if (projectIdStr == null) {
                return Optional.empty();
            }
            return Optional.of(UUID.fromString(projectIdStr));
        } catch (Exception e) {
            log.error("Error reading from Redis cache for API key", e);
            return Optional.empty();
        }
    }

    @Override
    public void cacheProjectId(String apiKey, UUID projectId, int ttlSeconds) {
        try {
            String key = CACHE_KEY_PREFIX + apiKey;
            redisTemplate.opsForValue().set(key, projectId.toString(), Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.error("Error caching API key to project_id mapping", e);
            // Don't throw - cache failures shouldn't break the flow
        }
    }

    @Override
    public void invalidate(String apiKey) {
        try {
            String key = CACHE_KEY_PREFIX + apiKey;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Error invalidating API key cache", e);
        }
    }
}


