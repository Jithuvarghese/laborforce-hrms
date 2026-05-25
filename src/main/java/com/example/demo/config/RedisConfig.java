package com.example.demo.config;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Configuration
@EnableCaching
@SuppressWarnings("null")
public class RedisConfig implements CachingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.data.redis.host:${redis.host:localhost}}")
    private @NonNull String redisHost;

    @Value("${spring.data.redis.port:${redis.port:6379}}")
    private int redisPort;

    @Value("${spring.data.redis.connect-timeout:${redis.connect-timeout:2000ms}}")
    private @NonNull Duration connectTimeout;

    @Value("${spring.data.redis.timeout:${redis.timeout:2000ms}}")
    private @NonNull Duration commandTimeout;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
            RedisStandaloneConfiguration standaloneConfiguration =
                new RedisStandaloneConfiguration(redisHost, redisPort);
        JedisClientConfiguration clientConfiguration = JedisClientConfiguration.builder()
                .connectTimeout(connectTimeout)
                .readTimeout(commandTimeout)
                .usePooling()
                .build();
        return new JedisConnectionFactory(standaloneConfiguration, clientConfiguration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(@NonNull RedisConnectionFactory redisConnectionFactory) {
        try {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(redisConnectionFactory);

            StringRedisSerializer keySerializer = new StringRedisSerializer();
            Jackson2JsonRedisSerializer<Object> valueSerializer =
                    new Jackson2JsonRedisSerializer<>(Object.class);

            template.setKeySerializer(keySerializer);
            template.setHashKeySerializer(keySerializer);
            template.setValueSerializer(valueSerializer);
            template.setHashValueSerializer(valueSerializer);
            template.afterPropertiesSet();
            return template;
        } catch (Exception ex) {
            log.warn("RedisTemplate initialization failed. Continuing with degraded mode.", ex);
            RedisTemplate<String, Object> fallback = new RedisTemplate<>();
            fallback.setEnableTransactionSupport(false);
            fallback.setDefaultSerializer(new StringRedisSerializer());
            fallback.afterPropertiesSet();
            return fallback;
        }
    }

    @Bean
    public CacheManager cacheManager(@NonNull RedisConnectionFactory redisConnectionFactory) {
        try {
                RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(java.time.Duration.ofMinutes(10))
                    .disableCachingNullValues();

            return RedisCacheManager.builder(redisConnectionFactory)
                    .cacheDefaults(configuration)
                    .transactionAware()
                    .build();
        } catch (Exception ex) {
            log.warn("Redis CacheManager initialization failed. Falling back to in-memory cache.", ex);
            return new ConcurrentMapCacheManager();
        }
    }

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
                log.warn("Cache GET failed for cache={} key={}. Falling back to DB.",
                    cache.getName(),
                    key,
                    exception);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, @NonNull Cache cache, @NonNull Object key, @Nullable Object value) {
                log.warn("Cache PUT failed for cache={} key={}. Continuing without cache write.",
                    cache.getName(),
                    key,
                    exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
                log.warn("Cache EVICT failed for cache={} key={}. Continuing request.",
                    cache.getName(),
                    key,
                    exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, @NonNull Cache cache) {
                log.warn("Cache CLEAR failed for cache={}. Continuing request.",
                    cache.getName(),
                    exception);
            }
        };
    }
}
