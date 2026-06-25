package com.ruoyi.im.constant;

import java.time.format.DateTimeFormatter;

/**
 * 系统级常量
 * 定义日期格式、缓存Key、分页配置、状态码等系统配置常量
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

    // ========== 缓存 Key 前缀 ==========

    /** 会话列表缓存 Key 前缀 */
    public static final String CACHE_KEY_CONVERSATION_LIST = "conversation:list:";

    /** 联系人/好友列表缓存 Key 前缀 */
    public static final String CACHE_KEY_CONTACT_LIST = "contact:list:";

    // ========== 分页配置 ==========

    /** 默认分页大小 */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /** 最大分页大小 */
    public static final int MAX_PAGE_SIZE = 100;

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

    /** 消息状态：已送达 */
    public static final String MESSAGE_STATUS_DELIVERED = "DELIVERED";

    /** 消息状态：已读 */
    public static final String MESSAGE_STATUS_READ = "READ";

    /** 消息状态：发送失败 */
    public static final String MESSAGE_STATUS_FAILED = "FAILED";

    // ========== 通用状态 ==========

    /** 状态：激活/活跃 */
    public static final String STATUS_ACTIVE = "ACTIVE";

    /** 状态：待处理 */
    public static final String STATUS_PENDING = "PENDING";

    /** 状态：成功 */
    public static final String STATUS_SUCCESS = "SUCCESS";

    // ========== 群组角色 ==========

    /** 群组角色：群主 */
    public static final String ROLE_OWNER = "OWNER";

    /** 群组角色：管理员 */
    public static final String ROLE_ADMIN = "ADMIN";

    /** 群组角色：普通成员 */
    public static final String ROLE_MEMBER = "MEMBER";

    // ========== 会话类型 ==========

    /** 会话类型：群组 */
    public static final String CONVERSATION_TYPE_GROUP = "GROUP";

    /** 会话类型：私聊 */
    public static final String CONVERSATION_TYPE_PRIVATE = "PRIVATE";

    // ========== 消息标记类型 ==========

    /** 消息标记类型：待办 */
    public static final String MARKER_TYPE_TODO = "TODO";

    // ========== 提及类型 ==========

    /** 提及类型：用户 */
    public static final String MENTION_TYPE_USER = "USER";

    // ========== 考勤默认值 ==========

    /** 考勤签到缓冲时间（分钟） */
    public static final int ATTENDANCE_CHECK_IN_BEFORE_DEFAULT = 120;

    /** 考勤签退缓冲时间（分钟） */
    public static final int ATTENDANCE_CHECK_OUT_AFTER_DEFAULT = 60;

    /** 考勤打卡范围（米） */
    public static final int ATTENDANCE_CHECK_RANGE_DEFAULT = 100;

    // ========== 视频会议状态 ==========

    /** 视频会议状态：进行中 */
    public static final String MEETING_STATUS_IN_PROGRESS = "IN_PROGRESS";

    /** 视频会议状态：已结束 */
    public static final String MEETING_STATUS_ENDED = "ENDED";

    // ========== 群组配置 ==========

    /** 默认群组成员上限 */
    public static final int DEFAULT_GROUP_MEMBER_LIMIT = 500;

    // ========== 用户配置 ==========

    /** 头像文件最大大小（MB） */
    public static final int MAX_AVATAR_SIZE_MB = 2;

    // ========== 音视频通话配置 ==========

    /** 视频通话最大人数 */
    public static final int VIDEO_CALL_MAX_PARTICIPANTS = 9;
}