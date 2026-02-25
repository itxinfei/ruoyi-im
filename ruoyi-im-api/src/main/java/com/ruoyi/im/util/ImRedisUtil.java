package com.ruoyi.im.util;

import com.ruoyi.im.constant.RedisKeyConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;

import java.util.*;
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
        String key = RedisKeyConstants.buildUserInfoKey(userId);
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
        String key = RedisKeyConstants.buildUserInfoKey(userId);
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 设置键值对（仅当键不存在时设置）
     * 用于实现分布式锁
     *
     * @param key   键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return true 表示设置成功，false 表示键已存在
     */
    public boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        if (redisTemplate == null) {
            return false;
        }
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 删除用户信息缓存
     */
    public void evictUserInfo(Long userId) {
        if (redisTemplate == null) {
            return;
        }
        String key = RedisKeyConstants.buildUserInfoKey(userId);
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
        String key = RedisKeyConstants.CONVERSATION_INFO_PREFIX + conversationId;
        redisTemplate.opsForValue().set(key, conversationInfo, CONVERSATION_INFO_EXPIRE, TimeUnit.MINUTES);
    }

    /**
     * 获取会话信息
     */
    public Object getConversationInfo(Long conversationId) {
        if (redisTemplate == null) {
            return null;
        }
        String key = RedisKeyConstants.CONVERSATION_INFO_PREFIX + conversationId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除会话信息缓存
     */
    public void evictConversationInfo(Long conversationId) {
        if (redisTemplate == null) {
            return;
        }
        String key = RedisKeyConstants.CONVERSATION_INFO_PREFIX + conversationId;
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
        String key = RedisKeyConstants.CONVERSATION_UNREAD_PREFIX + userId;
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
        String key = RedisKeyConstants.CONVERSATION_UNREAD_PREFIX + userId;
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
        String key = RedisKeyConstants.CONVERSATION_UNREAD_PREFIX + userId;
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
        String key = RedisKeyConstants.CONVERSATION_UNREAD_PREFIX + userId;
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
        String key = RedisKeyConstants.CONVERSATION_UNREAD_PREFIX + userId;
        redisTemplate.opsForHash().delete(key, conversationId.toString());
    }

    /**
     * 清除用户所有未读数缓存
     */
    public void evictAllUnreadCount(Long userId) {
        if (redisTemplate == null) {
            return;
        }
        String key = RedisKeyConstants.CONVERSATION_UNREAD_PREFIX + userId;
        redisTemplate.delete(key);
    }

    /**
     * 减少未读数（批量）
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @param delta 减少的数量（通常为1）
     */
    public void decrementUnreadCount(Long userId, Long conversationId, int delta) {
        if (redisTemplate == null) {
            return;
        }
        String key = RedisKeyConstants.CONVERSATION_UNREAD_PREFIX + userId;
        redisTemplate.opsForHash().increment(key, conversationId.toString(), -delta);
        redisTemplate.expire(key, UNREAD_COUNT_EXPIRE, TimeUnit.MINUTES);
    }

    /**
     * 设置未读数（直接设置值）
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @param count 未读数
     */
    public void setUnreadCount(Long userId, Long conversationId, int count) {
        if (redisTemplate == null) {
            return;
        }
        String key = RedisKeyConstants.CONVERSATION_UNREAD_PREFIX + userId;
        redisTemplate.opsForHash().put(key, conversationId.toString(), String.valueOf(count));
        redisTemplate.expire(key, UNREAD_COUNT_EXPIRE, TimeUnit.MINUTES);
    }

    /**
     * 获取用户所有会话的未读数（用于同步到数据库）
     * @return Map<conversationId, unreadCount>
     */
    public Map<Long, Integer> getAllUnreadCounts(Long userId) {
        if (redisTemplate == null) {
            return new HashMap<>();
        }
        String key = RedisKeyConstants.CONVERSATION_UNREAD_PREFIX + userId;
        Map<Object, Object> rawMap = redisTemplate.opsForHash().entries(key);
        Map<Long, Integer> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : rawMap.entrySet()) {
            try {
                Long conversationId = Long.parseLong(entry.getKey().toString());
                Integer count = Integer.parseInt(entry.getValue().toString());
                result.put(conversationId, count);
            } catch (Exception e) {
                log.warn("解析未读数失败: {}", entry, e);
            }
        }
        return result;
    }

    /**
     * 批量设置未读数（用于数据库同步后更新缓存）
     * @param userId 用户ID
     * @param counts Map<conversationId, unreadCount>
     */
    public void batchSetUnreadCounts(Long userId, Map<Long, Integer> counts) {
        if (redisTemplate == null || counts == null || counts.isEmpty()) {
            return;
        }
        String key = RedisKeyConstants.CONVERSATION_UNREAD_PREFIX + userId;
        Map<String, String> stringMap = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : counts.entrySet()) {
            stringMap.put(entry.getKey().toString(), entry.getValue().toString());
        }
        redisTemplate.opsForHash().putAll(key, stringMap);
        redisTemplate.expire(key, UNREAD_COUNT_EXPIRE, TimeUnit.MINUTES);
    }

    // ==================== 在线状态管理 ====================

    /**
     * 添加在线用户 - 由WebSocket端点调用
     */
    public void addOnlineUser(Long userId) {
        if (redisTemplate == null) {
            return;
        }
        String onlineUsersKey = RedisKeyConstants.ONLINE_USERS_KEY;
        redisTemplate.opsForSet().add(onlineUsersKey, userId.toString());
        redisTemplate.expire(onlineUsersKey, ONLINE_STATUS_EXPIRE, TimeUnit.MINUTES);

        // 同时记录用户上线时间
        String onlineTimeKey = RedisKeyConstants.buildUserOnlineKey(userId);
        redisTemplate.opsForValue().set(onlineTimeKey, System.currentTimeMillis(), ONLINE_STATUS_EXPIRE,
                TimeUnit.MINUTES);

        // 初始化心跳时间
        updateHeartbeat(userId);
    }

    /**
     * 移除在线用户 - 由WebSocket端点调用
     */
    public void removeOnlineUser(Long userId) {
        if (redisTemplate == null) {
            return;
        }
        String onlineUsersKey = RedisKeyConstants.ONLINE_USERS_KEY;
        redisTemplate.opsForSet().remove(onlineUsersKey, userId.toString());

        // 同时删除上线时间记录
        String onlineTimeKey = RedisKeyConstants.buildUserOnlineKey(userId);
        redisTemplate.delete(onlineTimeKey);

        // 删除心跳时间记录
        String heartbeatKey = RedisKeyConstants.KEY_PREFIX + "user:online:heartbeat:" + userId;
        redisTemplate.delete(heartbeatKey);
    }

    /**
     * 获取在线用户列表
     */
    @SuppressWarnings("unchecked")
    public Set<String> getOnlineUsers() {
        if (redisTemplate == null) {
            return new HashSet<>();
        }
        String onlineUsersKey = RedisKeyConstants.ONLINE_USERS_KEY;
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
     * 获取在线用户ID集合（返回 Long 类型）
     */
    public Set<Long> getOnlineUserIds() {
        if (redisTemplate == null) {
            return new HashSet<>();
        }
        String onlineUsersKey = RedisKeyConstants.ONLINE_USERS_KEY;
        Set<Object> members = redisTemplate.opsForSet().members(onlineUsersKey);
        Set<Long> result = new HashSet<>();
        if (members != null) {
            for (Object member : members) {
                if (member != null) {
                    try {
                        result.add(Long.parseLong(member.toString()));
                    } catch (NumberFormatException e) {
                        log.warn("解析在线用户ID失败: {}", member, e);
                    }
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
        String onlineUsersKey = RedisKeyConstants.ONLINE_USERS_KEY;
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
        String onlineUsersKey = RedisKeyConstants.ONLINE_USERS_KEY;
        return redisTemplate.opsForSet().size(onlineUsersKey);
    }

    // ==================== WebSocket心跳管理 ====================
    /**
     * 心跳时间过期时间（2分钟）
     */
    private static final long HEARTBEAT_EXPIRE = 2;

    /**
     * 会话信息过期时间（24小时）
     */
    private static final long SESSION_INFO_EXPIRE = 24;

    /**
     * 更新用户心跳时间
     *
     * @param userId 用户ID
     */
    public void updateHeartbeat(Long userId) {
        if (redisTemplate == null || userId == null) {
            return;
        }
        String heartbeatKey = RedisKeyConstants.KEY_PREFIX + "user:online:heartbeat:" + userId;
        redisTemplate.opsForValue().set(heartbeatKey, System.currentTimeMillis(), HEARTBEAT_EXPIRE, TimeUnit.MINUTES);
        log.debug("更新用户心跳时间: userId={}", userId);
    }

    /**
     * 获取用户最后心跳时间
     *
     * @param userId 用户ID
     * @return 最后心跳时间戳（毫秒），如果不存在返回null
     */
    public Long getLastHeartbeat(Long userId) {
        if (redisTemplate == null || userId == null) {
            return null;
        }
        String heartbeatKey = RedisKeyConstants.KEY_PREFIX + "user:online:heartbeat:" + userId;
        Object heartbeat = redisTemplate.opsForValue().get(heartbeatKey);
        return heartbeat != null ? Long.parseLong(heartbeat.toString()) : null;
    }

    /**
     * 检查用户心跳是否超时
     *
     * @param userId         用户ID
     * @param timeoutSeconds 超时时间（秒）
     * @return true表示心跳超时，false表示心跳正常
     */
    public boolean isHeartbeatTimeout(Long userId, long timeoutSeconds) {
        Long lastHeartbeat = getLastHeartbeat(userId);
        if (lastHeartbeat == null) {
            return true;
        }
        long currentTime = System.currentTimeMillis();
        long elapsedSeconds = (currentTime - lastHeartbeat) / 1000;
        return elapsedSeconds > timeoutSeconds;
    }

    // ==================== WebSocket会话信息管理 ====================

    /**
     * 记录用户会话信息
     *
     * @param userId     用户ID
     * @param serverId   服务器ID（支持集群部署）
     * @param sessionId  WebSocket会话ID
     * @param clientInfo 客户端信息（如IP地址、User-Agent等）
     */
    public void recordSessionInfo(Long userId, String serverId, String sessionId, String clientInfo) {
        if (redisTemplate == null || userId == null) {
            return;
        }
        String sessionKey = RedisKeyConstants.KEY_PREFIX + "user:online:session:" + userId;
        Map<String, Object> sessionInfo = new java.util.HashMap<>();
        sessionInfo.put("userId", userId);
        sessionInfo.put("serverId", serverId);
        sessionInfo.put("sessionId", sessionId);
        sessionInfo.put("clientInfo", clientInfo);
        sessionInfo.put("connectTime", System.currentTimeMillis());
        sessionInfo.put("lastHeartbeat", System.currentTimeMillis());

        redisTemplate.opsForValue().set(sessionKey, sessionInfo, SESSION_INFO_EXPIRE, TimeUnit.HOURS);
        log.debug("记录用户会话信息: userId={}, serverId={}, sessionId={}", userId, serverId, sessionId);
    }

    /**
     * 获取用户会话信息
     *
     * @param userId 用户ID
     * @return 会话信息Map，如果不存在返回null
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getSessionInfo(Long userId) {
        if (redisTemplate == null || userId == null) {
            return null;
        }
        String sessionKey = RedisKeyConstants.KEY_PREFIX + "user:online:session:" + userId;
        Object sessionInfo = redisTemplate.opsForValue().get(sessionKey);
        return sessionInfo != null ? (Map<String, Object>) sessionInfo : null;
    }

    /**
     * 删除用户会话信息
     *
     * @param userId 用户ID
     */
    public void removeSessionInfo(Long userId) {
        if (redisTemplate == null || userId == null) {
            return;
        }
        String sessionKey = RedisKeyConstants.KEY_PREFIX + "user:online:session:" + userId;
        redisTemplate.delete(sessionKey);
        log.debug("删除用户会话信息: userId={}", userId);
    }

    /**
     * 记录断线信息
     *
     * @param userId         用户ID
     * @param reason         断线原因
     * @param disconnectTime 断线时间
     */
    public void recordDisconnectInfo(Long userId, String reason, Long disconnectTime) {
        if (redisTemplate == null || userId == null) {
            return;
        }
        String disconnectKey = RedisKeyConstants.KEY_PREFIX + "user:online:disconnect:" + userId;
        Map<String, Object> disconnectInfo = new java.util.HashMap<>();
        disconnectInfo.put("userId", userId);
        disconnectInfo.put("reason", reason);
        disconnectInfo.put("disconnectTime", disconnectTime != null ? disconnectTime : System.currentTimeMillis());

        redisTemplate.opsForValue().set(disconnectKey, disconnectInfo, SESSION_INFO_EXPIRE, TimeUnit.HOURS);
        log.debug("记录断线信息: userId={}, reason={}", userId, reason);
    }

    /**
     * 获取断线信息
     *
     * @param userId 用户ID
     * @return 断线信息Map，如果不存在返回null
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getDisconnectInfo(Long userId) {
        if (redisTemplate == null || userId == null) {
            return null;
        }
        String disconnectKey = RedisKeyConstants.KEY_PREFIX + "user:online:disconnect:" + userId;
        Object disconnectInfo = redisTemplate.opsForValue().get(disconnectKey);
        return disconnectInfo != null ? (Map<String, Object>) disconnectInfo : null;
    }

    /**
     * 清除断线信息
     *
     * @param userId 用户ID
     */
    public void clearDisconnectInfo(Long userId) {
        if (redisTemplate == null || userId == null) {
            return;
        }
        String disconnectKey = RedisKeyConstants.KEY_PREFIX + "user:online:disconnect:" + userId;
        redisTemplate.delete(disconnectKey);
        log.debug("清除断线信息: userId={}", userId);
    }

    // ==================== 离线消息 ====================

    /**
     * 缓存离线消息
     */
    public void cacheOfflineMessage(Long userId, Object message) {
        if (redisTemplate == null) {
            return;
        }
        String key = RedisKeyConstants.KEY_PREFIX + "message:offline:" + userId;
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
        String key = RedisKeyConstants.KEY_PREFIX + "message:offline:" + userId;
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
        String key = RedisKeyConstants.KEY_PREFIX + "message:offline:" + userId;
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
        String key = RedisKeyConstants.KEY_PREFIX + "message:idempotent:" + clientMsgId;

        // 先检查是否存在
        Object cachedMsgId = redisTemplate.opsForValue().get(key);
        if (cachedMsgId != null) {
            log.debug("消息幂等性命中: clientMsgId={}, messageId={}", clientMsgId, cachedMsgId);
            try {
                return Long.parseLong(cachedMsgId.toString());
            } catch (NumberFormatException e) {
                log.warn("解析缓存消息ID失败: clientMsgId={}, value={}", clientMsgId, cachedMsgId);
            }
        }
        return null;
    }

    /**
     * 记录客户端消息ID与服务器消息ID的映射关系
     * 使用 SETNX 确保原子性
     *
     * @param clientMsgId 客户端消息ID
     * @param messageId   服务器消息ID
     * @return true 表示成功记录，false 表示已存在
     */
    public boolean recordClientMsgId(String clientMsgId, Long messageId) {
        if (redisTemplate == null || clientMsgId == null || clientMsgId.isEmpty()) {
            return false;
        }
        String key = RedisKeyConstants.KEY_PREFIX + "message:idempotent:" + clientMsgId;

        // 使用 setIfAbsent (SETNX) 确保原子性
        boolean success = redisTemplate.opsForValue().setIfAbsent(key, messageId.toString(), IDEMPOTENT_MSG_EXPIRE, TimeUnit.HOURS);

        if (success) {
            log.debug("记录客户端消息ID映射: clientMsgId={}, messageId={}", clientMsgId, messageId);
        } else {
            log.warn("客户端消息ID已存在: clientMsgId={}, messageId={}", clientMsgId, messageId);
        }

        return success;
    }

    // ==================== 群组信息缓存 ====================
    private static final long GROUP_INFO_EXPIRE = 30; // 群组信息缓存30分钟
    private static final long GROUP_MEMBERS_EXPIRE = 5; // 群组成员缓存5分钟

    /**
     * 缓存群组信息
     */
    public void cacheGroupInfo(Long groupId, Object groupInfo) {
        if (redisTemplate == null) {
            return;
        }
        String key = RedisKeyConstants.GROUP_INFO_PREFIX + groupId;
        redisTemplate.opsForValue().set(key, groupInfo, GROUP_INFO_EXPIRE, TimeUnit.MINUTES);
    }

    /**
     * 获取群组信息
     */
    public Object getGroupInfo(Long groupId) {
        if (redisTemplate == null) {
            return null;
        }
        String key = RedisKeyConstants.GROUP_INFO_PREFIX + groupId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除群组信息缓存
     */
    public void evictGroupInfo(Long groupId) {
        if (redisTemplate == null) {
            return;
        }
        String key = RedisKeyConstants.GROUP_INFO_PREFIX + groupId;
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

    // ==================== 群组成员缓存 ====================

    /**
     * 缓存群组成员列表
     *
     * @param groupId 群组ID
     * @param members 成员列表
     */
    @SuppressWarnings("unchecked")
    public void cacheGroupMembers(Long groupId, List<?> members) {
        if (redisTemplate == null || groupId == null || members == null) {
            return;
        }
        String key = RedisKeyConstants.buildGroupMembersKey(groupId);
        redisTemplate.opsForValue().set(key, members, GROUP_MEMBERS_EXPIRE, TimeUnit.MINUTES);
        log.debug("缓存群组成员列表: groupId={}, memberCount={}", groupId, members.size());
    }

    /**
     * 获取群组成员列表
     *
     * @param groupId 群组ID
     * @return 成员列表，不存在返回null
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getGroupMembers(Long groupId, Class<T> clazz) {
        if (redisTemplate == null || groupId == null) {
            return null;
        }
        String key = RedisKeyConstants.buildGroupMembersKey(groupId);
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof List) {
            return (List<T>) cached;
        }
        return null;
    }

    /**
     * 删除群组成员缓存
     * 在成员变更时调用
     *
     * @param groupId 群组ID
     */
    public void evictGroupMembers(Long groupId) {
        if (redisTemplate == null || groupId == null) {
            return;
        }
        String key = RedisKeyConstants.buildGroupMembersKey(groupId);
        redisTemplate.delete(key);
        log.debug("清除群组成员缓存: groupId={}", groupId);
    }

    /**
     * 获取群组成员列表，如果不存在则从数据库加载
     *
     * @param groupId 群组ID
     * @param clazz   返回类型
     * @param loader  数据加载器
     * @return 成员列表
     */
    public <T> List<T> getOrLoadGroupMembers(Long groupId, Class<T> clazz, Supplier<List<T>> loader) {
        List<T> cached = getGroupMembers(groupId, clazz);
        if (cached != null) {
            log.debug("命中群组成员缓存: groupId={}, memberCount={}", groupId, cached.size());
            return cached;
        }
        List<T> loaded = loader.get();
        if (loaded != null && !loaded.isEmpty()) {
            cacheGroupMembers(groupId, loaded);
        }
        return loaded;
    }

    // ==================== 有序集合操作 ====================

    /**
     * 向有序集合添加成员
     *
     * @param key   有序集合的key
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
     * @param key   有序集合的key
     * @param start 起始位置
     * @param end   结束位置
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
     * @param key   有序集合的key
     * @param start 起始位置
     * @param end   结束位置
     * @return 移除的成员数量
     */
    public Long zRemoveRange(String key, long start, long end) {
        if (redisTemplate == null) {
            return 0L;
        }
        return redisTemplate.opsForZSet().removeRange(buildSimpleKey(key), start, end);
    }

    // ==================== 辅助方法 ====================

    /**
     * 构建简单的 Redis 键
     *
     * @param key 原始键
     * @return 构建后的键
     */
    private String buildSimpleKey(String key) {
        return KEY_PREFIX + key;
    }
}
