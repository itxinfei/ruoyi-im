package com.ruoyi.im.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 多级缓存配置
 * 结合本地缓存(Caffeine)和分布式缓存(Redis)
 *
 * @author ruoyi
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 配置多级缓存管理器
     * 包含本地缓存和Redis缓存
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        // 配置本地缓存
        CaffeineCache userLocalCache = new CaffeineCache("user_local_cache",
                Caffeine.newBuilder()
                        .maximumSize(1000)
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .build());

        CaffeineCache messageLocalCache = new CaffeineCache("message_local_cache",
                Caffeine.newBuilder()
                        .maximumSize(2000)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .build());

        CaffeineCache sessionLocalCache = new CaffeineCache("session_local_cache",
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(30, TimeUnit.MINUTES)
                        .build());

        cacheManager.setCaches(Arrays.asList(userLocalCache, messageLocalCache, sessionLocalCache));
        return cacheManager;
    }

    /**
     * 创建用户信息本地缓存
     */
    @Bean("userCache")
    public Cache<String, Object> userCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    /**
     * 创建消息本地缓存
     */
    @Bean("messageCache")
    public Cache<String, Object> messageCache() {
        return Caffeine.newBuilder()
                .maximumSize(2000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }
}