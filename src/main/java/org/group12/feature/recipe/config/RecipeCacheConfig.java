package org.group12.feature.recipe.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Cache Redis cho kết quả gọi API công thức bên ngoài (giữ hạn mức request của gói miễn phí).
 * Giá trị cache là các record {@code Serializable} trong {@code recipe.client.model}.
 */
@Configuration
@EnableCaching
public class RecipeCacheConfig {

    public static final String SEARCH_CACHE = "recipe-search";
    public static final String DETAIL_CACHE = "recipe-detail";
    public static final String SIMILAR_CACHE = "recipe-similar";

    /** Spoonacular chỉ cho phép cache dữ liệu tối đa 1 giờ, không đặt TTL vượt mức này. */
    private static final long MAX_TTL_MINUTES = 60;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                     @Value("${app.recipe.cache.ttl-minutes}") long ttlMinutes) {
        Duration ttl = Duration.ofMinutes(Math.min(Math.max(ttlMinutes, 1), MAX_TTL_MINUTES));

        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .prefixCacheNameWith("pantry-chef:")
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(
                        new JdkSerializationRedisSerializer(getClass().getClassLoader())));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(configuration)
                .build();
    }
}
