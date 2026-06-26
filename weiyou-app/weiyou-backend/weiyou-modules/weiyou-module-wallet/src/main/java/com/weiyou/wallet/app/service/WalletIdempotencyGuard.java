package com.weiyou.wallet.app.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

@Component
public class WalletIdempotencyGuard {

    private static final String LOCK_PREFIX = "wy:wallet:idem:lock:";
    private static final String RESULT_PREFIX = "wy:wallet:idem:result:";

    private final StringRedisTemplate stringRedisTemplate;

    public WalletIdempotencyGuard(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public Optional<String> getResult(String operation, String key) {
        try {
            return Optional.ofNullable(stringRedisTemplate.opsForValue().get(resultKey(operation, key)));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    public boolean tryAcquire(String operation, String key, Duration ttl) {
        try {
            Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(lockKey(operation, key), "1", ttl);
            return Boolean.TRUE.equals(success);
        } catch (RuntimeException exception) {
            return true;
        }
    }

    public void saveResult(String operation, String key, String reference, Duration ttl) {
        try {
            stringRedisTemplate.opsForValue().set(resultKey(operation, key), reference, ttl);
            release(operation, key);
        } catch (RuntimeException exception) {
            // local fallback mode ignores redis persistence
        }
    }

    public void release(String operation, String key) {
        try {
            stringRedisTemplate.delete(lockKey(operation, key));
        } catch (RuntimeException exception) {
            // local fallback mode ignores redis release
        }
    }

    private String lockKey(String operation, String key) {
        return LOCK_PREFIX + operation + ":" + digest(key);
    }

    private String resultKey(String operation, String key) {
        return RESULT_PREFIX + operation + ":" + digest(key);
    }

    private String digest(String raw) {
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }
}
