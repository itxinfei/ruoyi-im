package com.ruoyi.im.utils;

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
    
    @Autowired(required = false)
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
        if (redisTemplate == null) {
            return; // Redis不可用，不缓存离线消息
        }
        String key = "im:offline:messages:" + userId;
        redisTemplate.opsForZSet().add(key, message, System.currentTimeMillis());
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
    }

    /**
     * 获取离线消息
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getOfflineMessages(Long userId, Class<T> clazz) {
        if (redisTemplate == null) {
            return new ArrayList<>(); // Redis不可用，返回空列表
        }
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

        redisTemplate.delete(key);

        return messages;
    }

    /**
     * 清空用户离线消息
     */
    public void clearOfflineMessages(Long userId) {
        if (redisTemplate == null) {
            return; // Redis不可用，不做处理
        }
        String key = "im:offline:messages:" + userId;
        redisTemplate.delete(key);
    }
}