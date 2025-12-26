package com.ruoyi.im.constant;

/**
 * 消息类型常量
 *
 * @author ruoyi
 * @date 2024-01-01
 */
public class MessageType {

    /** 文本消息 */
    public static final String TEXT = "TEXT";

    /** 文件消息 */
    public static final String FILE = "FILE";

    /** 图片消息 */
    public static final String IMAGE = "IMAGE";

    /** 语音消息 */
    public static final String VOICE = "VOICE";

    /** 视频消息 */
    public static final String VIDEO = "VIDEO";

    /** 位置消息 */
    public static final String LOCATION = "LOCATION";

    /** 系统通知 */
    public static final String NOTICE = "NOTICE";

    /** 撤回消息 */
    public static final String RECALL = "RECALL";

    /** 回复消息 */
    public static final String REPLY = "REPLY";

    /** 转发消息 */
    public static final String FORWARD = "FORWARD";

    private MessageType() {
        // 私有构造器，防止实例化
    }
}
