package com.ruoyi.system.constant;

/**
 * IM系统常量
 * 
 * @author ruoyi
 */
public class ImConstants
{
    /** 用户状态 */
    public static class UserStatus
    {
        /** 活跃 */
        public static final String ACTIVE = "ACTIVE";

        /** 非活跃 */
        public static final String INACTIVE = "INACTIVE";

        /** 禁用 */
        public static final String BANNED = "BANNED";
    }

    /** 群组状态 */
    public static class GroupStatus
    {
        /** 正常 */
        public static final String NORMAL = "NORMAL";

        /** 禁言 */
        public static final String MUTED = "MUTED";

        /** 已解散 */
        public static final String DISBANDED = "DISBANDED";
    }

    /** 会话类型 */
    public static class SessionType
    {
        /** 私聊 */
        public static final String PRIVATE = "PRIVATE";

        /** 群聊 */
        public static final String GROUP = "GROUP";
    }

    /** 消息类型 */
    public static class MessageType
    {
        /** 文本 */
        public static final String TEXT = "TEXT";

        /** 图片 */
        public static final String IMAGE = "IMAGE";

        /** 文件 */
        public static final String FILE = "FILE";

        /** 语音 */
        public static final String VOICE = "VOICE";

        /** 视频 */
        public static final String VIDEO = "VIDEO";
    }

    /** 文件状态 */
    public static class FileStatus
    {
        /** 上传中 */
        public static final String UPLOADING = "UPLOADING";

        /** 已上传 */
        public static final String UPLOADED = "UPLOADED";

        /** 上传失败 */
        public static final String FAILED = "FAILED";

        /** 已删除 */
        public static final String DELETED = "DELETED";
    }

    /** 消息读取状态 */
    public static final Integer MESSAGE_UNREAD = 0;
    public static final Integer MESSAGE_READ = 1;

    /** 消息撤回状态 */
    public static final Integer MESSAGE_NOT_REVOKED = 0;
    public static final Integer MESSAGE_REVOKED = 1;

    /** 分页默认值 */
    public static final Integer DEFAULT_PAGE_SIZE = 10;
    public static final Integer MAX_PAGE_SIZE = 100;

    /** 缓存相关 */
    public static class Cache
    {
        /** 用户缓存前缀 */
        public static final String USER_CACHE_PREFIX = "im:user:";

        /** 群组缓存前缀 */
        public static final String GROUP_CACHE_PREFIX = "im:group:";

        /** 会话缓存前缀 */
        public static final String SESSION_CACHE_PREFIX = "im:session:";

        /** 缓存过期时间（秒） */
        public static final Long CACHE_EXPIRE_TIME = 3600L;
    }
}
