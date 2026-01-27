package com.ruoyi.im.constant;

/**
 * 系统级常量
 * 定义缓存过期时间、文件限制、心跳间隔等系统配置常量
 *
 * @author ruoyi
 */
public final class SystemConstants {

    private SystemConstants() {
        // 防止实例化
    }

    // ========== 缓存过期时间（秒） ==========

    /** 30天（秒） */
    public static final int CACHE_EXPIRE_30_DAYS = 30 * 24 * 60 * 60;

    /** 7天（秒） */
    public static final int CACHE_EXPIRE_7_DAYS = 7 * 24 * 60 * 60;

    /** 1天（秒） */
    public static final int CACHE_EXPIRE_1_DAY = 24 * 60 * 60;

    /** 30分钟（秒） */
    public static final int CACHE_EXPIRE_30_MINUTES = 30 * 60;

    /** 10分钟（秒） */
    public static final int CACHE_EXPIRE_10_MINUTES = 10 * 60;

    /** 5分钟（秒） */
    public static final int CACHE_EXPIRE_5_MINUTES = 5 * 60;

    /** 1分钟（秒） */
    public static final int CACHE_EXPIRE_1_MINUTE = 60;

    // ========== 文件限制 ==========

    /** 最大文件大小（MB） */
    public static final int MAX_FILE_SIZE_MB = 100;

    /** 最大图片文件大小（MB） */
    public static final int MAX_IMAGE_SIZE_MB = 20;

    /** 最大视频文件大小（MB） */
    public static final int MAX_VIDEO_SIZE_MB = 200;

    /** 最大下载次数 */
    public static final int MAX_DOWNLOAD_COUNT = 10;

    // ========== WebSocket/心跳配置 ==========

    /** 心跳间隔（秒） */
    public static final int HEARTBEAT_INTERVAL = 30;

    /** 心跳超时时间（秒） */
    public static final int HEARTBEAT_TIMEOUT = 60;

    /** WebSocket 连接最大空闲时间（秒） */
    public static final int WS_MAX_IDLE_TIME = 300;

    // ========== 消息撤回配置 ==========

    /** 默认消息撤回时间限制（分钟） */
    public static final int DEFAULT_MESSAGE_RECALL_MINUTES = 2;

    /** 最大消息撤回时间限制（分钟） */
    public static final int MAX_MESSAGE_RECALL_MINUTES = 10;

    // ========== 群组配置 ==========

    /** 默认群组成员上限 */
    public static final int DEFAULT_GROUP_MEMBER_LIMIT = 500;

    /** 最大群组成员上限 */
    public static final int MAX_GROUP_MEMBER_LIMIT = 2000;

    /** 超大群组成员上限 */
    public static final int SUPER_GROUP_MEMBER_LIMIT = 10000;

    // ========== 分页配置 ==========

    /** 默认分页大小 */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /** 最大分页大小 */
    public static final int MAX_PAGE_SIZE = 100;

    // ========== 用户状态 ==========

    /** 用户状态：启用 */
    public static final int USER_STATUS_ENABLED = 1;

    /** 用户状态：禁用 */
    public static final int USER_STATUS_DISABLED = 0;

    // ========== 逻辑删除标记 ==========

    /** 未删除 */
    public static final int NOT_DELETED = 0;

    /** 已删除 */
    public static final int DELETED = 1;

    // ========== 默认值配置 ==========

    /** 默认用户头像 */
    public static final String DEFAULT_USER_AVATAR = "/avatar/default.png";

    /** 默认群组头像 */
    public static final String DEFAULT_GROUP_AVATAR = "/avatar/group_default.png";

    /** 默认初始密码 */
    public static final String DEFAULT_PASSWORD = "123456";
}
