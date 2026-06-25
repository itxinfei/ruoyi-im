package com.ruoyi.im.constant;

/**
 * 消息状态常量
 * 定义消息类型、会话类型等常量
 *
 * @author ruoyi
 */
public final class MessageStatusConstants {

    private MessageStatusConstants() {
        // 防止实例化
    }

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

    // ========== 会话类型 ==========

    /** 会话类型：私聊 */
    public static final String CONVERSATION_TYPE_PRIVATE = "PRIVATE";

    /** 会话类型：群聊 */
    public static final String CONVERSATION_TYPE_GROUP = "GROUP";
}