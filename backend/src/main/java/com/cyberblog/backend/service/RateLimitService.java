package com.cyberblog.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录限流服务：防止暴力破解
 * - 同一用户名：5 次失败后锁定 15 分钟
 * - 同一 IP：每分钟最多 10 次登录请求
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    // 用户名登录失败次数限制
    private static final int MAX_LOGIN_FAILURES = 5;
    private static final long USER_LOCK_DURATION_MINUTES = 15;

    // IP 每分钟请求次数限制
    private static final int MAX_IP_LOGIN_PER_MINUTE = 10;

    // Redis key 前缀
    private static final String USER_FAIL_PREFIX = "login:fail:";
    private static final String USER_LOCK_PREFIX = "login:lock:";
    private static final String IP_COUNT_PREFIX = "login:ip:";

    /**
     * 检查用户名是否被锁定（登录失败次数超限）
     * @return true = 已锁定，应拒绝登录
     */
    public boolean isUserLocked(String username) {
        String key = USER_LOCK_PREFIX + username;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 获取用户剩余锁定时间（秒）
     */
    public long getUserLockRemainingSeconds(String username) {
        String key = USER_LOCK_PREFIX + username;
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 记录一次登录失败，超限则锁定
     */
    public void recordLoginFailure(String username) {
        String failKey = USER_FAIL_PREFIX + username;
        Long count = redisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1) {
            // 第一次失败，设置 15 分钟过期（计数器自动清零）
            redisTemplate.expire(failKey, USER_LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }
        if (count != null && count >= MAX_LOGIN_FAILURES) {
            // 达到上限 → 锁定
            String lockKey = USER_LOCK_PREFIX + username;
            redisTemplate.opsForValue().set(lockKey, "1", USER_LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
            // 清除失败计数器（锁定期间不再累加）
            redisTemplate.delete(failKey);
            log.warn("用户 {} 登录失败次数超限，锁定 {} 分钟", username, USER_LOCK_DURATION_MINUTES);
        }
    }

    /**
     * 登录成功 → 清除失败计数
     */
    public void clearLoginFailure(String username) {
        redisTemplate.delete(USER_FAIL_PREFIX + username);
    }

    /**
     * 检查 IP 是否超过每分钟请求限制
     * @return true = 应拒绝请求
     */
    public boolean isIpRateLimited(String ip) {
        String key = IP_COUNT_PREFIX + ip;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            // 第一次请求，设置 1 分钟过期
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }
        return count != null && count > MAX_IP_LOGIN_PER_MINUTE;
    }
}
