package com.ruoyi.im.constant;

/**
 * Redis 键常量
 * 统一管理 Redis 缓存键的命名规范
 *
 * @author ruoyi
 */
public final class RedisKeyConstants {

    private RedisKeyConstants() {
        // 防止实例化
    }

    // ========== 前缀 ==========

    /** Redis 键前缀 */
    public static final String KEY_PREFIX = "im:";

    // ========== 用户相关 ==========

    /** 用户信息缓存前缀 */
    public static final String USER_INFO_PREFIX = KEY_PREFIX + "user:info:";

    /** 用户在线状态前缀 */
    public static final String USER_ONLINE_PREFIX = KEY_PREFIX + "user:online:";

    /** 在线用户集合 */
    public static final String ONLINE_USERS_KEY = KEY_PREFIX + "online:users";

    /** 用户 token 缓存前缀 */
    public static final String USER_TOKEN_PREFIX = KEY_PREFIX + "user:token:";

    // ========== 会话相关 ==========

    /** 会话列表缓存前缀 */
    public static final String CONVERSATION_LIST_PREFIX = KEY_PREFIX + "conversation:list:";

    /** 会话信息缓存前缀 */
    public static final String CONVERSATION_INFO_PREFIX = KEY_PREFIX + "conversation:info:";

    /** 会话未读数前缀 */
    public static final String CONVERSATION_UNREAD_PREFIX = KEY_PREFIX + "conversation:unread:";

    // ========== 消息相关 ==========

    /** 消息缓存前缀 */
    public static final String MESSAGE_PREFIX = KEY_PREFIX + "message:";

    /** 最近消息列表前缀 */
    public static final String RECENT_MESSAGES_PREFIX = KEY_PREFIX + "messages:recent:";

    /** 消息已读状态前缀 */
    public static final String MESSAGE_READ_STATUS_PREFIX = KEY_PREFIX + "message:read:";

    // ========== 群组相关 ==========

    /** 群组信息缓存前缀 */
    public static final String GROUP_INFO_PREFIX = KEY_PREFIX + "group:info:";

    /** 群组成员列表前缀 */
    public static final String GROUP_MEMBERS_PREFIX = KEY_PREFIX + "group:members:";

    /** 群组在线成员集合前缀 */
    public static final String GROUP_ONLINE_MEMBERS_PREFIX = KEY_PREFIX + "group:online:";

    // ========== 好友相关 ==========

    /** 好友列表缓存前缀 */
    public static final String FRIEND_LIST_PREFIX = KEY_PREFIX + "friend:list:";

    /** 好友请求列表前缀 */
    public static final String FRIEND_REQUEST_PREFIX = KEY_PREFIX + "friend:request:";

    // ========== WebSocket 相关 ==========

    /** WebSocket Session 映射前缀 */
    public static final String WS_SESSION_PREFIX = KEY_PREFIX + "ws:session:";

    /** 用户 Session 映射前缀 */
    public static final String USER_SESSION_PREFIX = KEY_PREFIX + "user:session:";

    // ========== 分布式锁相关 ==========

    /** 分布式锁前缀 */
    public static final String LOCK_PREFIX = KEY_PREFIX + "lock:";

    /** 发送消息锁 */
    public static final String SEND_MESSAGE_LOCK = LOCK_PREFIX + "send:message:";

    /** 用户操作锁 */
    public static final String USER_OPERATION_LOCK = LOCK_PREFIX + "user:operation:";

    // ========== 限流相关 ==========

    /** 限流前缀 */
    public static final String RATE_LIMIT_PREFIX = KEY_PREFIX + "rate:limit:";

    /** 发送消息限流 */
    public static final String SEND_MESSAGE_RATE_LIMIT = RATE_LIMIT_PREFIX + "send:message:";

    // ========== 文件相关 ==========

    /** 文件信息缓存前缀 */
    public static final String FILE_INFO_PREFIX = KEY_PREFIX + "file:info:";

    // ========== 验证码相关 ==========

    /** 验证码前缀 */
    public static final String CAPTCHA_PREFIX = KEY_PREFIX + "captcha:";

    /** 短信验证码前缀 */
    public static final String SMS_CODE_PREFIX = KEY_PREFIX + "sms:code:";

    // ========== 辅助方法 ==========

    /**
     * 构建用户信息缓存键
     *
     * @param userId 用户ID
     * @return Redis键
     */
    public static String buildUserInfoKey(Long userId) {
        return USER_INFO_PREFIX + userId;
    }

    /**
     * 构建会话列表缓存键
     *
     * @param userId 用户ID
     * @return Redis键
     */
    public static String buildConversationListKey(Long userId) {
        return CONVERSATION_LIST_PREFIX + userId;
    }

    /**
     * 构建在线用户状态键
     *
     * @param userId 用户ID
     * @return Redis键
     */
    public static String buildUserOnlineKey(Long userId) {
        return USER_ONLINE_PREFIX + userId;
    }

    /**
     * 构建群组成员列表键
     *
     * @param groupId 群组ID
     * @return Redis键
     */
    public static String buildGroupMembersKey(Long groupId) {
        return GROUP_MEMBERS_PREFIX + groupId;
    }
}
