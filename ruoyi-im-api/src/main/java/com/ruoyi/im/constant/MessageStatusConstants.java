package com.ruoyi.im.constant;

/**
 * 消息状态常量
 * 定义消息发送、接收、阅读等状态常量
 *
 * @author ruoyi
 */
public final class MessageStatusConstants {

    private MessageStatusConstants() {
        // 防止实例化
    }

    // ========== 消息发送状态 ==========

    /** 消息状态：发送中 */
    public static final String STATUS_SENDING = "SENDING";

    /** 消息状态：已发送 */
    public static final String STATUS_SENT = "SENT";

    /** 消息状态：已送达 */
    public static final String STATUS_DELIVERED = "DELIVERED";

    /** 消息状态：已读 */
    public static final String STATUS_READ = "READ";

    /** 消息状态：发送失败 */
    public static final String STATUS_FAILED = "FAILED";

    /** 消息状态：已撤回 */
    public static final String STATUS_REVOKED = "REVOKED";

    // ========== 消息编辑状态 ==========

    /** 消息编辑状态：未编辑 */
    public static final int EDIT_STATUS_NONE = 0;

    /** 消息编辑状态：已编辑 */
    public static final int EDIT_STATUS_EDITED = 1;

    // ========== 消息类型 ==========

    /** 消息类型：文本 */
    public static final String MESSAGE_TYPE_TEXT = "TEXT";

    /** 消息类型：图片 */
    public static final String MESSAGE_TYPE_IMAGE = "IMAGE";

    /** 消息类型：文件 */
    public static final String MESSAGE_TYPE_FILE = "FILE";

    /** 消息类型：语音 */
    public static final String MESSAGE_TYPE_VOICE = "VOICE";

    /** 消息类型：视频 */
    public static final String MESSAGE_TYPE_VIDEO = "VIDEO";

    /** 消息类型：位置 */
    public static final String MESSAGE_TYPE_LOCATION = "LOCATION";

    /** 消息类型：链接 */
    public static final String MESSAGE_TYPE_LINK = "LINK";

    /** 消息类型：系统通知 */
    public static final String MESSAGE_TYPE_SYSTEM = "SYSTEM";

    /** 消息类型：自定义消息 */
    public static final String MESSAGE_TYPE_CUSTOM = "CUSTOM";

    // ========== 会话类型 ==========

    /** 会话类型：私聊 */
    public static final String CONVERSATION_TYPE_PRIVATE = "PRIVATE";

    /** 会话类型：群聊 */
    public static final String CONVERSATION_TYPE_GROUP = "GROUP";

    /** 会话类型：系统通知 */
    public static final String CONVERSATION_TYPE_SYSTEM = "SYSTEM";

    // ========== 消息方向 ==========

    /** 消息方向：发送 */
    public static final int DIRECTION_OUTGOING = 1;

    /** 消息方向：接收 */
    public static final int DIRECTION_INCOMING = 2;

    // ========== 特殊消息标记 ==========

    /** @标记消息 */
    public static final int MENTION_TYPE_ALL = 0;

    /** @指定人 */
    public static final int MENTION_TYPE_SPECIFIC = 1;
}
