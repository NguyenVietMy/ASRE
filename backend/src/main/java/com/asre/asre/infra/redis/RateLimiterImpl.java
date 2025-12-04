package com.asre.asre.infra.redis;

import com.asre.asre.application.ratelimit.RateLimiterPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Redis-based rate limiter using sliding window algorithm.
 */
@Component
@Slf4j
public class RateLimiterImpl implements RateLimiterPort {

    private static final String RATE_LIMIT_KEY_PREFIX = "ratelimit:";
    private static final String LUA_SCRIPT = """
            local key = KEYS[1]
            local limit = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            
            -- Remove expired entries
            redis.call('zremrangebyscore', key, 0, now - window)
            
            -- Count current requests in window
            local count = redis.call('zcard', key)
            
            if count < limit then
                -- Add current request
                redis.call('zadd', key, now, now)
                redis.call('expire', key, window)
                return 1
            else
                return 0
            end
            """;

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> script;

    public RateLimiterImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.script = new DefaultRedisScript<>();
        this.script.setScriptText(LUA_SCRIPT);
        this.script.setResultType(Long.class);
    }

    @Override
    public boolean isAllowed(String key, int limitPerMinute) {
        try {
            String redisKey = RATE_LIMIT_KEY_PREFIX + key;
            long now = System.currentTimeMillis();
            long windowMs = 60_000; // 1 minute in milliseconds

            List<String> keys = Collections.singletonList(redisKey);
            List<String> args = List.of(
                    String.valueOf(limitPerMinute),
                    String.valueOf(windowMs),
                    String.valueOf(now)
            );

            Long result = redisTemplate.execute(script, keys, args.toArray());
            return result != null && result == 1;
        } catch (Exception e) {
            log.error("Error checking rate limit for key: {}", key, e);
            // Fail open - allow request if Redis is down
            return true;
        }
    }
}

