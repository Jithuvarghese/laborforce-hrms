package com.example.demo.workforce;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("null")
public class ActiveWorkerCacheService {

    private static final Logger log = LoggerFactory.getLogger(ActiveWorkerCacheService.class);
    private static final @NonNull Duration ACTIVE_WORKER_TTL = Duration.ofHours(16);

    private final @NonNull RedisTemplate<String, Object> redisTemplate;

    public ActiveWorkerCacheService(@NonNull RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addActiveWorker(@NonNull Long workerId,
                                @NonNull String workerName,
                                @NonNull Long siteId,
                                @NonNull String siteName,
                                @NonNull LocalDateTime clockIn) {
        String key = buildKey(workerId);
        Map<String, Object> payload = new HashMap<>();
        payload.put("workerId", workerId);
        payload.put("workerName", workerName);
        payload.put("siteId", siteId);
        payload.put("siteName", siteName);
        payload.put("clockInTime", clockIn.toString());

        try {
            redisTemplate.opsForHash().putAll(key, payload);
            redisTemplate.expire(key, ACTIVE_WORKER_TTL);
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable while adding active worker {}. Degrading gracefully.", workerId, ex);
        } catch (DataAccessException ex) {
            log.warn("Redis data access error while adding active worker {}. Degrading gracefully.", workerId, ex);
        } catch (Exception ex) {
            log.warn("Unexpected Redis error while adding active worker {}. Degrading gracefully.", workerId, ex);
        }
    }

    public void removeActiveWorker(@NonNull Long workerId) {
        String key = buildKey(workerId);
        try {
            redisTemplate.delete(key);
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable while removing active worker {}. Ignoring.", workerId, ex);
        } catch (DataAccessException ex) {
            log.warn("Redis data access error while removing active worker {}. Ignoring.", workerId, ex);
        } catch (Exception ex) {
            log.warn("Unexpected Redis error while removing active worker {}. Ignoring.", workerId, ex);
        }
    }

    public List<Map<String, Object>> getAllActiveWorkers() {
        try {
            Set<String> keys = redisTemplate.keys("active_worker:*");
            if (keys == null || keys.isEmpty()) {
                return List.of();
            }

            List<Map<String, Object>> result = new ArrayList<>();
            for (String key : keys) {
                Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
                if (entries == null || entries.isEmpty()) {
                    continue;
                }
                Map<String, Object> casted = new HashMap<>();
                entries.forEach((k, v) -> casted.put(Objects.toString(k, null), v));
                result.add(casted);
            }
            return result;
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable while fetching active workers. Returning empty list.", ex);
            return List.of();
        } catch (DataAccessException ex) {
            log.warn("Redis data access error while fetching active workers. Returning empty list.", ex);
            return List.of();
        } catch (Exception ex) {
            log.warn("Unexpected Redis error while fetching active workers. Returning empty list.", ex);
            return List.of();
        }
    }

    public boolean isWorkerActive(@NonNull Long workerId) {
        String key = buildKey(workerId);
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable while checking worker {} active state. Returning false.", workerId, ex);
            return false;
        } catch (DataAccessException ex) {
            log.warn("Redis data access error while checking worker {} active state. Returning false.", workerId, ex);
            return false;
        } catch (Exception ex) {
            log.warn("Unexpected Redis error while checking worker {} active state. Returning false.", workerId, ex);
            return false;
        }
    }

    public void invalidateWorkerCache(@NonNull Long workerId) {
        removeActiveWorker(workerId);
    }

    private String buildKey(@NonNull Long workerId) {
        return "active_worker:" + workerId;
    }
}
