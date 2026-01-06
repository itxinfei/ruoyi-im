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
 * IM Redis宸ュ叿绫?
 * 
 * @author ruoyi
 */
@Component
public class ImRedisUtil {
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final AtomicLong MESSAGE_ID_COUNTER = new AtomicLong(System.currentTimeMillis());
    
    /**
     * 鐢熸垚娑堟伅ID
     */
    public Long generateMessageId() {
        return MESSAGE_ID_COUNTER.incrementAndGet();
    }
    
    /**
     * 鐢熸垚浼氳瘽ID
     */
    public Long generateSessionId() {
        return MESSAGE_ID_COUNTER.incrementAndGet();
    }
    
    /**
     * 缂撳瓨绂荤嚎娑堟伅
     */
    public void cacheOfflineMessage(Long userId, Object message) {
        if (redisTemplate == null) {
            return; // Redis涓嶅彲鐢紝涓嶇紦瀛樼绾挎秷鎭?
        }
        String key = "im:offline:messages:" + userId;
        redisTemplate.opsForZSet().add(key, message, System.currentTimeMillis());
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
    }

    /**
     * 鑾峰彇绂荤嚎娑堟伅
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getOfflineMessages(Long userId, Class<T> clazz) {
        if (redisTemplate == null) {
            return new ArrayList<>(); // Redis涓嶅彲鐢紝杩斿洖绌哄垪琛?
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
     * 娓呯┖鐢ㄦ埛绂荤嚎娑堟伅
     */
    public void clearOfflineMessages(Long userId) {
        if (redisTemplate == null) {
            return; // Redis涓嶅彲鐢紝涓嶅仛澶勭悊
        }
        String key = "im:offline:messages:" + userId;
        redisTemplate.delete(key);
    }
}
