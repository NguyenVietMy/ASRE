package com.asre.asre.infra.redis;

import com.asre.asre.domain.alerts.AlertState;
import com.asre.asre.domain.alerts.AlertStateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Redis implementation of AlertStateRepository.
 * Stores alert firing state for "for duration" logic.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class AlertStateRepositoryImpl implements AlertStateRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Generates Redis key for alert state.
     * Infrastructure-specific key formatting is kept here, not in domain.
     */
    private String generateRedisKey(UUID projectId, UUID ruleId, UUID serviceId) {
        return String.format("alert:%s:%s:%s:state", projectId, ruleId, serviceId);
    }

    @Override
    public void save(AlertState state, int ttlSeconds) {
        try {
            String key = generateRedisKey(state.getProjectId(), state.getRuleId(), state.getServiceId());
            String value = objectMapper.writeValueAsString(state);
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.error("Failed to save alert state: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save alert state", e);
        }
    }

    @Override
    public Optional<AlertState> findByKey(UUID projectId, UUID ruleId, UUID serviceId) {
        try {
            String key = generateRedisKey(projectId, ruleId, serviceId);
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return Optional.empty();
            }
            AlertState state = objectMapper.readValue(value, AlertState.class);
            return Optional.of(state);
        } catch (Exception e) {
            log.warn("Error reading alert state from cache: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void delete(UUID projectId, UUID ruleId, UUID serviceId) {
        try {
            String key = generateRedisKey(projectId, ruleId, serviceId);
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Error deleting alert state: {}", e.getMessage());
        }
    }
}

