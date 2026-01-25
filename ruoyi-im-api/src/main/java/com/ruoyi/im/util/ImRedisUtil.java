package com.ruoyi.im.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * IM Redis工具类
 * 提供缓存、在线状态、分布式锁等功能
 *
 * @author ruoyi
 */
@Component
public class ImRedisUtil {

    private static final Logger log = LoggerFactory.getLogger(ImRedisUtil.class);

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private static final AtomicLong MESSAGE_ID_COUNTER = new AtomicLong(System.currentTimeMillis());

    // ==================== Key 前缀 ====================
    private static final String KEY_PREFIX = "im:";
    private static final String USER_PREFIX = "user:";
    private static final String CONVERSATION_PREFIX = "conversation:";
    private static final String MESSAGE_PREFIX = "message:";
    private static final String ONLINE_PREFIX = "online:";
    private static final String UNREAD_PREFIX = "unread:";
    private static final String OFFLINE_MSG_PREFIX = "offline:messages:";

    // ==================== 过期时间 ====================
    private static final long USER_INFO_EXPIRE = 30; // 用户信息缓存30分钟
    private static final long CONVERSATION_INFO_EXPIRE = 10; // 会话信息缓存10分钟
    private static final long UNREAD_COUNT_EXPIRE = 5; // 未读数缓存5分钟
    private static final long OFFLINE_MSG_EXPIRE = 7; // 离线消息保留7天
    private static final long ONLINE_STATUS_EXPIRE = 30; // 在线状态30分钟

    // ==================== ID生成 ====================
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

    // ==================== 用户信息缓存 ====================
    /**
     * 缓存用户信息
     */
    public void cacheUserInfo(Long userId, Object userInfo) {
        if (redisTemplate == null) {
            return;
        }
        String key = buildKey(USER_PREFIX, "info", String.valueOf(userId));
        redisTemplate.opsForValue().set(key, userInfo, USER_INFO_EXPIRE, TimeUnit.MINUTES);
        log.debug("缓存用户信息: userId={}", userId);
    }

    /**
     * 获取用户信息
     */
    public Object getUserInfo(Long userId) {
        if (redisTemplate == null) {
            return null;
        }
        String key = buildKey(USER_PREFIX, "info", String.valueOf(userId));
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除用户信息缓存
     */
    public void evictUserInfo(Long userId) {
        if (redisTemplate == null) {
            return;
        }
        String key = buildKey(USER_PREFIX, "info", String.valueOf(userId));
        redisTemplate.delete(key);
        log.debug("清除用户信息缓存: userId={}", userId);
    }

    /**
     * 获取用户信息，如果不存在则从数据库加载
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrLoadUserInfo(Long userId, Class<T> clazz, Supplier<T> loader) {
        T cached = (T) getUserInfo(userId);
        if (cached != null) {
            return cached;
        }
        T loaded = loader.get();
        if (loaded != null) {
            cacheUserInfo(userId, loaded);
        }
        return loaded;
    }

    // ==================== 会话信息缓存 ====================
    /**
     * 缓存会话信息
     */
    public void cacheConversationInfo(Long conversationId, Object conversationInfo) {
        if (redisTemplate == null) {
            return;
        }
        String key = buildKey(CONVERSATION_PREFIX, "info", String.valueOf(conversationId));
        redisTemplate.opsForValue().set(key, conversationInfo, CONVERSATION_INFO_EXPIRE, TimeUnit.MINUTES);
    }

    /**
     * 获取会话信息
     */
    public Object getConversationInfo(Long conversationId) {
        if (redisTemplate == null) {
            return null;
        }
        String key = buildKey(CONVERSATION_PREFIX, "info", String.valueOf(conversationId));
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除会话信息缓存
     */
    public void evictConversationInfo(Long conversationId) {
        if (redisTemplate == null) {
            return;
        }
        String key = buildKey(CONVERSATION_PREFIX, "info", String.valueOf(conversationId));
        redisTemplate.delete(key);
    }

    // ==================== 未读数缓存 ====================
    /**
     * 缓存用户未读数
     */
    public void cacheUnreadCount(Long userId, Long conversationId, Integer count) {
        if (redisTemplate == null) {
            return;
        }
        String key = buildKey(UNREAD_PREFIX, String.valueOf(userId));
        redisTemplate.opsForHash().put(key, conversationId.toString(), count.toString());
        redisTemplate.expire(key, UNREAD_COUNT_EXPIRE, TimeUnit.MINUTES);
    }

    /**
     * 获取用户在指定会话的未读数
     */
    public Integer getUnreadCount(Long userId, Long conversationId) {
        if (redisTemplate == null) {
            return null;
        }
        String key = buildKey(UNREAD_PREFIX, String.valueOf(userId));
        Object count = redisTemplate.opsForHash().get(key, conversationId.toString());
        return count != null ? Integer.parseInt(count.toString()) : null;
    }

    /**
     * 获取用户总未读数
     */
    public Integer getTotalUnreadCount(Long userId) {
        if (redisTemplate == null) {
            return null;
        }
        String key = buildKey(UNREAD_PREFIX, String.valueOf(userId));
        Long total = 0L;
        for (Object count : redisTemplate.opsForHash().values(key)) {
            if (count != null) {
                total += Long.parseLong(count.toString());
            }
        }
        return total.intValue();
    }

    /**
     * 增加未读数
     */
    public void incrementUnreadCount(Long userId, Long conversationId) {
        if (redisTemplate == null) {
            return;
        }
        String key = buildKey(UNREAD_PREFIX, String.valueOf(userId));
        redisTemplate.opsForHash().increment(key, conversationId.toString(), 1);
        redisTemplate.expire(key, UNREAD_COUNT_EXPIRE, TimeUnit.MINUTES);
    }

    /**
     * 清除会话未读数
     */
    public void clearConversationUnread(Long userId, Long conversationId) {
        if (redisTemplate == null) {
            return;
        }
        String key = buildKey(UNREAD_PREFIX, String.valueOf(userId));
        redisTemplate.opsForHash().delete(key, conversationId.toString());
    }

    /**
     * 清除用户所有未读数缓存
     */
    public void evictAllUnreadCount(Long userId) {
        if (redisTemplate == null) {
            return;
        }
        String key = buildKey(UNREAD_PREFIX, String.valueOf(userId));
        redisTemplate.delete(key);
    }

    // ==================== 在线状态管理 ====================
    /**
     * 添加在线用户 - 由WebSocket端点调用
     */
    public void addOnlineUser(Long userId) {
        if (redisTemplate == null) {
            return;
        }
        String onlineUsersKey = buildKey(ONLINE_PREFIX, "users");
        redisTemplate.opsForSet().add(onlineUsersKey, userId.toString());
        redisTemplate.expire(onlineUsersKey, ONLINE_STATUS_EXPIRE, TimeUnit.MINUTES);

        // 同时记录用户上线时间
        String onlineTimeKey = buildKey(ONLINE_PREFIX, "time", String.valueOf(userId));
        redisTemplate.opsForValue().set(onlineTimeKey, System.currentTimeMillis(), ONLINE_STATUS_EXPIRE,
                TimeUnit.MINUTES);
    }

    /**
     * 移除在线用户 - 由WebSocket端点调用
     */
    public void removeOnlineUser(Long userId) {
        if (redisTemplate == null) {
            return;
        }
        String onlineUsersKey = buildKey(ONLINE_PREFIX, "users");
        redisTemplate.opsForSet().remove(onlineUsersKey, userId.toString());

        // 同时删除上线时间记录
        String onlineTimeKey = buildKey(ONLINE_PREFIX, "time", String.valueOf(userId));
        redisTemplate.delete(onlineTimeKey);
    }

    /**
     * 获取在线用户列表
     */
    @SuppressWarnings("unchecked")
    public Set<String> getOnlineUsers() {
        if (redisTemplate == null) {
            return new HashSet<>();
        }
        String onlineUsersKey = buildKey(ONLINE_PREFIX, "users");
        Set<Object> members = redisTemplate.opsForSet().members(onlineUsersKey);
        Set<String> result = new HashSet<>();
        if (members != null) {
            for (Object member : members) {
                if (member != null) {
                    result.add(member.toString());
                }
            }
        }
        return result;
    }

    /**
     * 检查用户是否在线
     */
    public boolean isOnlineUser(Long userId) {
        if (redisTemplate == null) {
            return false;
        }
        String onlineUsersKey = buildKey(ONLINE_PREFIX, "users");
        Boolean isMember = redisTemplate.opsForSet().isMember(onlineUsersKey, userId.toString());
        return isMember != null && isMember;
    }

    /**
     * 获取在线用户数量
     */
    public Long getOnlineUserCount() {
        if (redisTemplate == null) {
            return 0L;
        }
        String onlineUsersKey = buildKey(ONLINE_PREFIX, "users");
        return redisTemplate.opsForSet().size(onlineUsersKey);
    }

    // ==================== 离线消息 ====================
    /**
     * 缓存离线消息
     */
    public void cacheOfflineMessage(Long userId, Object message) {
        if (redisTemplate == null) {
            return;
        }
        String key = buildKey(OFFLINE_MSG_PREFIX, String.valueOf(userId));
        redisTemplate.opsForZSet().add(key, message, System.currentTimeMillis());
        redisTemplate.expire(key, OFFLINE_MSG_EXPIRE, TimeUnit.DAYS);
    }

    /**
     * 获取离线消息
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getOfflineMessages(Long userId, Class<T> clazz) {
        if (redisTemplate == null) {
            return new ArrayList<>();
        }
        String key = buildKey(OFFLINE_MSG_PREFIX, String.valueOf(userId));
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
     * 清除用户离线消息
     */
    public void clearOfflineMessages(Long userId) {
        if (redisTemplate == null) {
            return;
        }
        String key = buildKey(OFFLINE_MSG_PREFIX, String.valueOf(userId));
        redisTemplate.delete(key);
    }

    // ==================== 通用缓存方法 ====================
    /**
     * 设置缓存
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        if (redisTemplate == null) {
            return;
        }
        redisTemplate.opsForValue().set(buildSimpleKey(key), value, timeout, unit);
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        if (redisTemplate == null) {
            return null;
        }
        return redisTemplate.opsForValue().get(buildSimpleKey(key));
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        if (redisTemplate == null) {
            return;
        }
        redisTemplate.delete(buildSimpleKey(key));
    }

    /**
     * 批量删除缓存
     */
    public void deletePattern(String pattern) {
        if (redisTemplate == null) {
            return;
        }
        Set<String> keys = redisTemplate.keys(buildSimpleKey(pattern));
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 检查缓存是否存在
     */
    public boolean exists(String key) {
        if (redisTemplate == null) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildSimpleKey(key)));
    }

    /**
     * 设置过期时间
     */
    public void expire(String key, long timeout, TimeUnit unit) {
        if (redisTemplate == null) {
            return;
        }
        redisTemplate.expire(buildSimpleKey(key), timeout, unit);
    }

    // ==================== 消息幂等性 ====================
    /**
     * 消息幂等性键前缀
     */
    private static final String IDEMPOTENT_MSG_PREFIX = "message:idempotent:";
    /**
     * 消息幂等性缓存过期时间（24小时）
     */
    private static final long IDEMPOTENT_MSG_EXPIRE = 24;

    /**
     * 检查并记录客户端消息ID（实现幂等性）
     * 如果该clientMsgId已存在，返回对应的消息ID
     * 如果不存在，则记录并返回null
     *
     * @param clientMsgId 客户端消息ID
     * @return 已存在时返回对应的消息ID，不存在返回null
     */
    public Long checkAndRecordClientMsgId(String clientMsgId) {
        if (redisTemplate == null || clientMsgId == null || clientMsgId.isEmpty()) {
            return null;
        }
        String key = buildKey(IDEMPOTENT_MSG_PREFIX, clientMsgId);
        Object cachedMsgId = redisTemplate.opsForValue().get(key);
        if (cachedMsgId != null) {
            log.debug("消息幂等性命中: clientMsgId={}, messageId={}", clientMsgId, cachedMsgId);
            return Long.parseLong(cachedMsgId.toString());
        }
        return null;
    }

    /**
     * 记录客户端消息ID与服务器消息ID的映射关系
     *
     * @param clientMsgId 客户端消息ID
     * @param messageId   服务器消息ID
     */
    public void recordClientMsgId(String clientMsgId, Long messageId) {
        if (redisTemplate == null || clientMsgId == null || clientMsgId.isEmpty()) {
            return;
        }
        String key = buildKey(IDEMPOTENT_MSG_PREFIX, clientMsgId);
        redisTemplate.opsForValue().set(key, messageId.toString(), IDEMPOTENT_MSG_EXPIRE, TimeUnit.HOURS);
        log.debug("记录客户端消息ID映射: clientMsgId={}, messageId={}", clientMsgId, messageId);
    }

    // ==================== 群组信息缓存 ====================
    private static final String GROUP_PREFIX = "group:";
    private static final long GROUP_INFO_EXPIRE = 30; // 群组信息缓存30分钟

    /**
     * 缓存群组信息
     */
    public void cacheGroupInfo(Long groupId, Object groupInfo) {
        if (redisTemplate == null) {
            return;
        }
        String key = buildKey(GROUP_PREFIX, "info", String.valueOf(groupId));
        redisTemplate.opsForValue().set(key, groupInfo, GROUP_INFO_EXPIRE, TimeUnit.MINUTES);
    }

    /**
     * 获取群组信息
     */
    public Object getGroupInfo(Long groupId) {
        if (redisTemplate == null) {
            return null;
        }
        String key = buildKey(GROUP_PREFIX, "info", String.valueOf(groupId));
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除群组信息缓存
     */
    public void evictGroupInfo(Long groupId) {
        if (redisTemplate == null) {
            return;
        }
        String key = buildKey(GROUP_PREFIX, "info", String.valueOf(groupId));
        redisTemplate.delete(key);
    }

    /**
     * 获取群组信息，如果不存在则从数据库加载
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrLoadGroupInfo(Long groupId, Class<T> clazz, Supplier<T> loader) {
        T cached = (T) getGroupInfo(groupId);
        if (cached != null) {
            return cached;
        }
        T loaded = loader.get();
        if (loaded != null) {
            cacheGroupInfo(groupId, loaded);
        }
        return loaded;
    }

    // ==================== 私有方法 ====================
    /**
     * 构建简单缓存Key
     */
    private String buildSimpleKey(String... parts) {
        StringBuilder sb = new StringBuilder(KEY_PREFIX);
        for (String part : parts) {
            sb.append(part);
        }
        return sb.toString();
    }

    /**
     * 构建缓存Key（带前缀）
     */
    private String buildKey(String prefix, String... parts) {
        StringBuilder sb = new StringBuilder(KEY_PREFIX).append(prefix);
        for (String part : parts) {
            sb.append(":").append(part);
        }
        return sb.toString();
    }

    // ==================== 有序集合操作 ====================
    /**
     * 向有序集合添加成员
     *
     * @param key 有序集合的key
     * @param value 成员值
     * @param score 分数
     */
    public void zAdd(String key, String value, long score) {
        if (redisTemplate == null) {
            return;
        }
        redisTemplate.opsForZSet().add(buildSimpleKey(key), value, score);
    }

    /**
     * 获取有序集合指定范围内的成员（按分数降序）
     *
     * @param key 有序集合的key
     * @param start 起始位置
     * @param end 结束位置
     * @return 成员集合
     */
    public java.util.Set<String> zReverseRange(String key, long start, long end) {
        if (redisTemplate == null) {
            return new java.util.HashSet<>();
        }
        Set<Object> members = redisTemplate.opsForZSet().reverseRange(buildSimpleKey(key), start, end);
        Set<String> result = new java.util.HashSet<>();
        if (members != null) {
            for (Object member : members) {
                if (member != null) {
                    result.add(member.toString());
                }
            }
        }
        return result;
    }

    /**
     * 移除有序集合指定排名范围内的成员
     *
     * @param key 有序集合的key
     * @param start 起始位置
     * @param end 结束位置
     * @return 移除的成员数量
     */
    public Long zRemoveRange(String key, long start, long end) {
        if (redisTemplate == null) {
            return 0L;
        }
        return redisTemplate.opsForZSet().removeRange(buildSimpleKey(key), start, end);
    }
}
