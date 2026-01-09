package com.ruoyi.im.vo.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息响应VO
 *
 * @author ruoyi
 */
@Data
public class ImMessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息ID */
    private Long id;

    /** 会话ID */
    private Long conversationId;

    /** 发送者ID */
    private Long senderId;

    /** 发送者昵称 */
    private String senderName;

    /** 发送者头像 */
    private String senderAvatar;

    /** 消息类型 */
    private String type;

    /** 消息内容 */
    private String content;

    /** 状态: 0=发送中, 1=已发送, 2=已送达, 3=已读, 4=发送失败, 5=已撤回 */
    private Integer status;

    /** 是否撤回 */
    private Integer isRevoked;

    /** 撤回时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime revokeTime;

    /** 发送时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;

    /** 是否为当前用户发送的消息 */
    private Boolean isSelf;

    /** 回复的消息ID */
    private Long replyToMessageId;

    /** 引用消息内容（用于显示引用的原消息） */
    private QuotedMessageVO quotedMessage;

    /**
     * 引用消息内部类
     * 用于展示被回复/引用的原消息信息
     */
    @Data
    public static class QuotedMessageVO implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 消息ID */
        private Long id;

        /** 发送者昵称 */
        private String senderName;

        /** 消息内容（截取部分） */
        private String content;

        /** 消息类型 */
        private String type;

        /** 发送时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime sendTime;

        /** 是否为图片/文件类型 */
        private Boolean isFile;
    }
}
