package com.ruoyi.im.service.impl;

import com.ruoyi.im.service.ImCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * IM缓存服务实现
 * 提供统一的缓存操作方法，支持用户、会话、消息等数据的缓存
 *
 * @author ruoyi
 */
@Service
public class ImCacheServiceImpl implements ImCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存过期时间（秒）
    private static final long USER_INFO_EXPIRE = 3600;       // 用户信息缓存1小时
    private static final long CONVERSATION_EXPIRE = 1800;     // 会话信息缓存30分钟
    private static final long MESSAGE_EXPIRE = 600;           // 消息列表缓存10分钟
    private static final long UNREAD_EXPIRE = 86400;          // 未读数缓存24小时

    @Override
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void delete(Set<String> keys) {
        redisTemplate.delete(keys);
    }

    @Override
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    @Override
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    @Override
    public void cacheUserInfo(Long userId, Object userInfo) {
        String key = USER_PREFIX + userId;
        set(key, userInfo, USER_INFO_EXPIRE, TimeUnit.SECONDS);
    }

    @Override
    public <T> T getUserInfo(Long userId) {
        String key = USER_PREFIX + userId;
        return get(key);
    }

    @Override
    public void deleteUserCache(Long userId) {
        String key = USER_PREFIX + userId;
        delete(key);
    }

    @Override
    public void cacheConversationInfo(Long conversationId, Object conversationInfo) {
        String key = CONVERSATION_PREFIX + conversationId;
        set(key, conversationInfo, CONVERSATION_EXPIRE, TimeUnit.SECONDS);
    }

    @Override
    public <T> T getConversationInfo(Long conversationId) {
        String key = CONVERSATION_PREFIX + conversationId;
        return get(key);
    }

    @Override
    public void deleteConversationCache(Long conversationId) {
        String key = CONVERSATION_PREFIX + conversationId;
        delete(key);
    }

    @Override
    public void cacheUnreadCount(Long userId, Long conversationId, Integer count) {
        String key = UNREAD_COUNT_PREFIX + userId + ":" + conversationId;
        set(key, count, UNREAD_EXPIRE, TimeUnit.SECONDS);
    }

    @Override
    public Integer getUnreadCount(Long userId, Long conversationId) {
        String key = UNREAD_COUNT_PREFIX + userId + ":" + conversationId;
        Integer count = get(key);
        return count != null ? count : 0;
    }

    @Override
    public void incrementUnreadCount(Long userId, Long conversationId, Integer delta) {
        String key = UNREAD_COUNT_PREFIX + userId + ":" + conversationId;
        redisTemplate.opsForValue().increment(key, delta);
        // 设置过期时间（如果不存在）
        if (getExpire(key) == -1) {
            expire(key, UNREAD_EXPIRE, TimeUnit.SECONDS);
        }
    }

    @Override
    public void clearUnreadCount(Long userId, Long conversationId) {
        String key = UNREAD_COUNT_PREFIX + userId + ":" + conversationId;
        delete(key);
    }

    @Override
    public void cacheRecentMessages(Long conversationId, List<?> messages) {
        String key = MESSAGE_PREFIX + conversationId;
        set(key, messages, MESSAGE_EXPIRE, TimeUnit.SECONDS);
    }

    @Override
    public <T> List<T> getRecentMessages(Long conversationId) {
        String key = MESSAGE_PREFIX + conversationId;
        return get(key);
    }

    @Override
    public void deleteMessageCache(Long conversationId) {
        String key = MESSAGE_PREFIX + conversationId;
        delete(key);
    }

    @Override
    public void deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public void clearAll() {
        // 清理所有 IM 前缀的缓存
        deleteByPattern(CACHE_KEY_PREFIX + "*");
    }
}
