package com.ruoyi.im.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * IM缓存服务接口
 * 提供统一的缓存操作方法，支持用户、会话、消息等数据的缓存
 *
 * @author ruoyi
 */
public interface ImCacheService {

    String CACHE_KEY_PREFIX = "im:";

    String USER_PREFIX = CACHE_KEY_PREFIX + "user:";
    String CONVERSATION_PREFIX = CACHE_KEY_PREFIX + "conversation:";
    String MESSAGE_PREFIX = CACHE_KEY_PREFIX + "message:";
    String ONLINE_USER_PREFIX = CACHE_KEY_PREFIX + "online:";
    String UNREAD_COUNT_PREFIX = CACHE_KEY_PREFIX + "unread:";

    /**
     * 设置缓存
     *
     * @param key 缓存键
     * @param value 缓存值
     */
    void set(String key, Object value);

    /**
     * 设置缓存并指定过期时间
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    void set(String key, Object value, long timeout, TimeUnit unit);

    /**
     * 获取缓存
     *
     * @param key 缓存键
     * @return 缓存值
     */
    <T> T get(String key);

    /**
     * 删除缓存
     *
     * @param key 缓存键
     */
    void delete(String key);

    /**
     * 批量删除缓存
     *
     * @param keys 缓存键集合
     */
    void delete(Set<String> keys);

    /**
     * 判断缓存是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     */
    Boolean hasKey(String key);

    /**
     * 设置过期时间
     *
     * @param key 缓存键
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    Boolean expire(String key, long timeout, TimeUnit unit);

    /**
     * 获取过期时间
     *
     * @param key 缓存键
     * @return 过期时间（秒）
     */
    Long getExpire(String key);

    /**
     * 缓存用户信息
     *
     * @param userId 用户ID
     * @param userInfo 用户信息
     */
    void cacheUserInfo(Long userId, Object userInfo);

    /**
     * 获取缓存的用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    <T> T getUserInfo(Long userId);

    /**
     * 删除用户缓存
     *
     * @param userId 用户ID
     */
    void deleteUserCache(Long userId);

    /**
     * 缓存会话信息
     *
     * @param conversationId 会话ID
     * @param conversationInfo 会话信息
     */
    void cacheConversationInfo(Long conversationId, Object conversationInfo);

    /**
     * 获取缓存的会话信息
     *
     * @param conversationId 会话ID
     * @return 会话信息
     */
    <T> T getConversationInfo(Long conversationId);

    /**
     * 删除会话缓存
     *
     * @param conversationId 会话ID
     */
    void deleteConversationCache(Long conversationId);

    /**
     * 缓存用户未读消息数
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @param count 未读数
     */
    void cacheUnreadCount(Long userId, Long conversationId, Integer count);

    /**
     * 获取用户未读消息数
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @return 未读数
     */
    Integer getUnreadCount(Long userId, Long conversationId);

    /**
     * 增加未读消息数
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @param delta 增量
     */
    void incrementUnreadCount(Long userId, Long conversationId, Integer delta);

    /**
     * 清除用户未读消息数
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     */
    void clearUnreadCount(Long userId, Long conversationId);

    /**
     * 缓存最近消息列表
     *
     * @param conversationId 会话ID
     * @param messages 消息列表
     */
    void cacheRecentMessages(Long conversationId, List<?> messages);

    /**
     * 获取缓存的最近消息列表
     *
     * @param conversationId 会话ID
     * @return 消息列表
     */
    <T> List<T> getRecentMessages(Long conversationId);

    /**
     * 删除消息缓存
     *
     * @param conversationId 会话ID
     */
    void deleteMessageCache(Long conversationId);

    /**
     * 根据模式删除缓存
     *
     * @param pattern 匹配模式
     */
    void deleteByPattern(String pattern);
}
