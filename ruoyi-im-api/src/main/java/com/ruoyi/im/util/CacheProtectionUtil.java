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
    public <T> T getWithMutexLock(String cacheKey, String lockKey, Supplier<T> supplier, long expireTime) {
        // 尝试获取缓存
        T result = (T) redisTemplate.opsForValue().get(cacheKey);
        if (result != null) {
            logger.debug("从缓存获取数据: key={}", cacheKey);
            return result;
        }

        // 缓存未命中，尝试获取锁
        String lockValue = Thread.currentThread().getName() + "_" + System.currentTimeMillis();
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, 30, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(lockAcquired)) {
            try {
                // 再次检查缓存，防止重复加载
                result = (T) redisTemplate.opsForValue().get(cacheKey);
                if (result != null) {
                    logger.debug("双重检查从缓存获取数据: key={}", cacheKey);
                    return result;
                }

                // 从数据源加载数据
                result = supplier.get();
                
                if (result != null) {
                    // 设置缓存，使用随机过期时间防止缓存雪崩
                    setWithRandomExpire(cacheKey, result, expireTime, 300); // 在基础时间上增加最多5分钟随机时间
                    logger.debug("从数据源加载并缓存数据: key={}", cacheKey);
                } else {
                    // 缓存空值，防止缓存穿透，但设置较短过期时间
                    setWithRandomExpire(cacheKey, null, 60, 60); // 1-2分钟
                    logger.debug("缓存空值防止穿透: key={}", cacheKey);
                }
            } finally {
                // 释放锁（使用Lua脚本确保原子性）
                releaseLock(lockKey, lockValue);
            }
        } else {
            // 获取锁失败，等待一段时间后递归调用
            try {
                Thread.sleep(50);
                logger.debug("等待锁释放后重试: key={}", cacheKey);
                return getWithMutexLock(cacheKey, lockKey, supplier, expireTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("等待锁期间被中断", e);
                throw new RuntimeException("获取数据时被中断", e);
            }
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
            (connection) -> connection.eval(script.getBytes(), 1, 
                lockKey.getBytes(), lockValue.getBytes()),
            null
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