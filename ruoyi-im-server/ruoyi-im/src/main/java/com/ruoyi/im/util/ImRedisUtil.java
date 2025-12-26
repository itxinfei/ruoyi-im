package com.ruoyi.im.util;

import com.ruoyi.common.core.redis.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * IM Redis工具类
 * 
 * @author ruoyi
 */
@Component
public class ImRedisUtil {
    
    @Autowired
    private RedisCache redisCache;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final AtomicLong MESSAGE_ID_COUNTER = new AtomicLong(System.currentTimeMillis());
    
    /**
     * 生成消息ID
     */
    public Long generateMessageId() {
        return MESSAGE_ID_COUNTER.incrementAndGet();
    }
    
    /**
     * 生成会话ID
     */
    public Long generateSessionId() {
        return MESSAGE_ID_COUNTER.incrementAndGet();
    }
    
    /**
     * 缓存离线消息
     */
    public void cacheOfflineMessage(Long userId, Object message) {
        String key = "im:offline:messages:" + userId;
        // 使用Redis的ZSet操作，通过RedisTemplate直接操作
        redisTemplate.opsForZSet().add(key, message, System.currentTimeMillis());
        
        // 设置过期时间为7天
        redisCache.expire(key, 7, TimeUnit.DAYS);
    }
    
    /**
     * 获取离线消息
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getOfflineMessages(Long userId, Class<T> clazz) {
        String key = "im:offline:messages:" + userId;
        Set<Object> messageObjects = redisTemplate.opsForZSet().range(key, 0, -1);
        
        List<T> messages = new ArrayList<>();
        if (messageObjects != null) {
            for (Object messageObject : messageObjects) {
                T message = (T) messageObject;
                if (message != null) {
                    messages.add(message);
                }
            }
        }
        
        // 清空离线消息
        redisCache.deleteObject(key);
        
        return messages;
    }
    
    /**
     * 清空用户离线消息
     */
    public void clearOfflineMessages(Long userId) {
        String key = "im:offline:messages:" + userId;
        redisCache.deleteObject(key);
    }
}