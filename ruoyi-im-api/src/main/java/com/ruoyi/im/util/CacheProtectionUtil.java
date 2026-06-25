package com.ruoyi.im.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存防护工具类
 * 提供缓存雪崩、缓存击穿、缓存穿透等防护机制
 *
 * @author ruoyi
 */
@Component
public class CacheProtectionUtil {

    private static final Logger logger = LoggerFactory.getLogger(CacheProtectionUtil.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public CacheProtectionUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 防止缓存雪崩 - 为缓存设置随机过期时间
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param baseTimeout 基础过期时间（秒）
     * @param randomRange 随机范围（秒）
     */
    public void setWithRandomExpire(String key, Object value, long baseTimeout, long randomRange) {
        // 在基础过期时间上增加随机时间，防止同时失效
        long randomExpire = baseTimeout + (long)(Math.random() * randomRange);
        redisTemplate.opsForValue().set(key, value, randomExpire, TimeUnit.SECONDS);
        logger.debug("设置缓存: key={}, 过期时间={}秒", key, randomExpire);
    }

    /**
     * 防止缓存击穿 - 使用互斥锁
     *
     * @param cacheKey 缓存键
     * @param lockKey 锁键
     * @param supplier 数据提供者
     * @param expireTime 缓存过期时间（秒）
     * @param <T> 返回类型
     * @return 缓存数据或从数据源获取的数据
     */
    private static final int MAX_RETRY = 5;

    public <T> T getWithMutexLock(String cacheKey, String lockKey, Supplier<T> supplier, long expireTime) {
        @SuppressWarnings("unchecked")
        T result = (T) redisTemplate.opsForValue().get(cacheKey);
        if (result != null) {
            logger.debug("从缓存获取数据: key={}", cacheKey);
            return result;
        }

        for (int attempt = 0; attempt <= MAX_RETRY; attempt++) {
            String lockValue = Thread.currentThread().getName() + "_" + System.currentTimeMillis();
            Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, 30, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(lockAcquired)) {
                try {
                    result = (T) redisTemplate.opsForValue().get(cacheKey);
                    if (result != null) {
                        logger.debug("双重检查从缓存获取数据: key={}", cacheKey);
                        return result;
                    }
                    result = supplier.get();
                    if (result != null) {
                        setWithRandomExpire(cacheKey, result, expireTime, 300);
                        logger.debug("从数据源加载并缓存数据: key={}", cacheKey);
                    } else {
                        // 空值仅缓存极短时间（3-5秒），防止缓存穿透的同时避免"刚创建却读到空"的问题
                        setWithRandomExpire(cacheKey, null, 3, 5);
                        logger.debug("缓存空值防止穿透: key={}", cacheKey);
                    }
                } finally {
                    releaseLock(lockKey, lockValue);
                }
                return result;
            } else {
                try {
                    Thread.sleep(50);
                    logger.debug("等待锁释放后重试: key={}, attempt={}", cacheKey, attempt + 1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("等待锁期间被中断", e);
                    throw new RuntimeException("获取数据时被中断", e);
                }
            }
        }

        // 重试耗尽，直接从数据源加载（降级策略）
        logger.warn("获取锁重试耗尽，直接加载数据源: key={}", cacheKey);
        result = supplier.get();
        if (result != null) {
            setWithRandomExpire(cacheKey, result, expireTime, 300);
        }
        return result;
    }

    /**
     * 释放分布式锁
     *
     * @param lockKey 锁键
     * @param lockValue 锁值
     */
    private void releaseLock(String lockKey, String lockValue) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                       "return redis.call('del', KEYS[1]) " +
                       "else return 0 end";
        redisTemplate.execute(
            (org.springframework.data.redis.connection.RedisConnection connection) -> {
                connection.eval(script.getBytes(),
                    org.springframework.data.redis.connection.ReturnType.INTEGER,
                    1,
                    lockKey.getBytes(),
                    lockValue.getBytes());
                return null;
            }
        );
    }

    /**
     * 防止缓存穿透 - 使用布隆过滤器的思想（简化版）
     * 对于确定不存在的数据，将其加入黑名单
     *
     * @param cacheKey 缓存键
     * @param blackListKey 黑名单键
     * @param supplier 数据提供者
     * @param expireTime 缓存过期时间（秒）
     * @param <T> 返回类型
     * @return 数据
     */
    public <T> T getWithBloomFilter(String cacheKey, String blackListKey, Supplier<T> supplier, long expireTime) {
        // 检查是否在黑名单中
        Boolean isBlacklisted = redisTemplate.hasKey(blackListKey);
        if (Boolean.TRUE.equals(isBlacklisted)) {
            logger.debug("数据在黑名单中，直接返回null: key={}", cacheKey);
            return null;
        }

        // 尝试获取缓存
        T result = (T) redisTemplate.opsForValue().get(cacheKey);
        if (result != null) {
            logger.debug("从缓存获取数据: key={}", cacheKey);
            return result;
        }

        // 从数据源加载数据
        result = supplier.get();
        
        if (result != null) {
            // 设置缓存
            setWithRandomExpire(cacheKey, result, expireTime, 300);
            logger.debug("从数据源加载并缓存数据: key={}", cacheKey);
        } else {
            // 数据不存在，加入黑名单
            redisTemplate.opsForValue().set(blackListKey, "1", 300, TimeUnit.SECONDS); // 5分钟黑名单
            logger.debug("数据不存在，加入黑名单: key={}", cacheKey);
        }

        return result;
    }
}