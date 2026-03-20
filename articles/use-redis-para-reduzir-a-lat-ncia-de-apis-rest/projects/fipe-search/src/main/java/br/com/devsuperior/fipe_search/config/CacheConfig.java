package br.com.devsuperior.fipe_search.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    private final RedisConnectionFactory connectionFactory;
    private final String cacheName;
    private final long ttlHours;

    public CacheConfig(
            RedisConnectionFactory connectionFactory,
            @Value("${app.cache.name}") String cacheName,
            @Value("${app.cache.ttl-hours:24}") long ttlHours
    ) {
        this.connectionFactory = connectionFactory;
        this.cacheName = cacheName;
        this.ttlHours = ttlHours;
    }

    @Override
    public CacheManager cacheManager() {
        var defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(Duration.ofHours(ttlHours));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .initialCacheNames(Set.of(cacheName))
                .disableCreateOnMissingCache()
                .build();
    }

    @Override
    public CacheResolver cacheResolver() {
        return context -> List.of(
                Objects.requireNonNull(
                        cacheManager().getCache(cacheName),
                        () -> "Cache not found: " + cacheName
                )
        );
    }

    @Override
    public KeyGenerator keyGenerator() {
        return (_, _, params) -> String.join(":", Arrays.stream(params)
                .map(String::valueOf)
                .toList());
    }
}
