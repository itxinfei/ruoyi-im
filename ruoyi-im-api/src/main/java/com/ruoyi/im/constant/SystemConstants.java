package com.ruoyi.im.constant;

import java.time.format.DateTimeFormatter;

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

    // ========== 日期时间格式化 ==========

    /** 日期格式：yyyyMMdd */
    public static final DateTimeFormatter DATE_FORMAT_COMPACT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 日期格式：yyyy/MM/dd */
    public static final DateTimeFormatter DATE_FORMAT_SLASH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /** 日期时间格式：yyyyMMdd_HHmmss */
    public static final DateTimeFormatter DATETIME_FORMAT_COMPACT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /** 日期时间格式：yyyy-MM-dd HH:mm:ss */
    public static final DateTimeFormatter DATETIME_FORMAT_STANDARD = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 日期格式：yyyy-MM-dd */
    public static final DateTimeFormatter DATE_FORMAT_STANDARD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 时间格式：HH:mm */
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

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

    // ========== 缓存 Key 前缀 ==========

    /** 会话列表缓存 Key 前缀 */
    public static final String CACHE_KEY_CONVERSATION_LIST = "conversation:list:";

    /** 联系人/好友列表缓存 Key 前缀 */
    public static final String CACHE_KEY_CONTACT_LIST = "contact:list:";

    /** 用户信息缓存 Key 前缀 */
    public static final String CACHE_KEY_USER_INFO = "user:info:";

    /** 群组信息缓存 Key 前缀 */
    public static final String CACHE_KEY_GROUP_INFO = "group:info:";

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

    // ========== 音视频通话配置 ==========

    /** 视频通话最大人数 */
    public static final int VIDEO_CALL_MAX_PARTICIPANTS = 9;

    // ========== 用户配置 ==========

    /** 头像文件最大大小（MB） */
    public static final int MAX_AVATAR_SIZE_MB = 2;

    // ========== 消息配置 ==========

    /** 消息编辑时间限制（分钟） */
    public static final int MESSAGE_EDIT_TIME_LIMIT = 15;

    /** 消息内容最大长度 */
    public static final int MAX_MESSAGE_LENGTH = 2000;

    // ========== 会话配置 ==========

    /** 会话过滤类型：全部 */
    public static final String CONVERSATION_FILTER_ALL = "all";

    // ========== 任务配置 ==========

    /** 任务完成百分比 */
    public static final int TASK_COMPLETE_PERCENT = 100;

    // ========== 文档配置 ==========

    /** 文档摘要最大长度 */
    public static final int DOCUMENT_SUMMARY_MAX_LENGTH = 100;

    // ========== 系统配置限制 ==========

    /** 系统配置最小值 */
    public static final int CONFIG_MIN_VALUE = 100;

    /** 系统配置最大值 */
    public static final int CONFIG_MAX_VALUE = 10000;

    /** 系统配置最大值（周） */
    public static final int CONFIG_MAX_WEEKS = 10080;

    // ========== 任务/备份状态 ==========

    /** 任务状态：进行中 */
    public static final String STATUS_IN_PROGRESS = "in_progress";

    /** 任务状态：已完成 */
    public static final String STATUS_COMPLETED = "completed";

    /** 任务状态：失败 */
    public static final String STATUS_FAILED = "failed";

    // ========== 消息状态 ==========

    /** 消息状态：发送中 */
    public static final String MESSAGE_STATUS_SENDING = "SENDING";

    // ========== 视频会议状态 ==========

    /** 视频会议状态：进行中 */
    public static final String MEETING_STATUS_IN_PROGRESS = "IN_PROGRESS";

    /** 视频会议状态：已结束 */
    public static final String MEETING_STATUS_ENDED = "ENDED";
}
