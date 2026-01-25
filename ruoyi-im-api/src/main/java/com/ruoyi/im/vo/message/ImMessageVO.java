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

    /** 时间戳（毫秒）- 兼容前端 */
    private Long timestamp;

    /**
     * 消息已读状态（仅发送者自己的消息包含此信息）
     */
    private MessageReadStatus readStatus;

    /**
     * 消息已读状态内部类
     * 用于展示群消息已读/未读情况
     */
    @Data
    public static class MessageReadStatus implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 已读人数
         */
        private Integer readCount;

        /**
         * 总接收人数（群聊中是成员数，私聊中是2人）
         */
        private Integer totalCount;

        /**
         * 未读人数
         */
        private Integer unreadCount;

        /**
         * 已读百分比
         */
        private Integer readPercent;

        /**
         * 是否全部已读
         */
        private Boolean allRead;

        /**
         * 已读用户头像列表（最多5个）
         */
        private java.util.List<ReadUserAvatar> readUserAvatars;

        /**
         * 已读用户头像信息
         */
        @Data
        public static class ReadUserAvatar implements Serializable {
            private static final long serialVersionUID = 1L;

            private Long userId;
            private String userName;
            private String avatar;
        }
    }

    public Long getTimestamp() {
        if (this.timestamp != null) {
            return this.timestamp;
        }
        if (this.sendTime != null) {
            return this.sendTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        return System.currentTimeMillis();
    }
}
