package org.group12.feature.auth.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService {

    private static final String REFRESH_PREFIX = "refresh:";
    private static final String USER_REFRESH_PREFIX = "refresh:user:";

    @Value("${jwt.refreshable-duration}")
    private Long refreshableDuration;

    private final RedisTemplate<String, String> redis;

    public RefreshTokenService(
            @Qualifier("redisTemplate") RedisTemplate<String, String> redis
    ) {
        this.redis = redis;
    }

    public void saveRefreshToken(String refreshToken, String user) {
        String tokenKey = getKey(refreshToken);
        String userKey = getUserKey(user);

        redis.opsForValue().set(
                tokenKey,
                user,
                refreshableDuration,
                TimeUnit.SECONDS
        );

        redis.opsForSet().add(userKey, tokenKey);

        redis.expire(userKey, refreshableDuration, TimeUnit.SECONDS);
    }

    public boolean isRefreshTokenExists(String refreshToken) {
        return Boolean.TRUE.equals(redis.hasKey(getKey(refreshToken)));
    }

    public void deleteRefreshToken(String refreshToken) {
        String tokenKey = getKey(refreshToken);
        String user = getUser(refreshToken);

        redis.delete(tokenKey);

        if (user != null) {
            String userKey = getUserKey(user);
            redis.opsForSet().remove(userKey, tokenKey);

            if (Boolean.FALSE.equals(redis.hasKey(userKey))) {
                redis.delete(userKey);
            }
        }
    }

    public void revokeAllRefreshTokens(String user) {
        String userKey = getUserKey(user);

        Set<String> tokenKeys = redis.opsForSet().members(userKey);

        if (tokenKeys != null && !tokenKeys.isEmpty()) {
            redis.delete(tokenKeys);
        }

        redis.delete(userKey);
    }

    public String getUser(String refreshToken) {
        return redis.opsForValue().get(getKey(refreshToken));
    }

    private String getKey(String refreshToken) {
        return REFRESH_PREFIX + refreshToken;
    }

    private String getUserKey(String user) {
        return USER_REFRESH_PREFIX + user;
    }
}