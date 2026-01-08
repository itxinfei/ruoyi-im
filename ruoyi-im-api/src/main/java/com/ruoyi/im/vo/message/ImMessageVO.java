package com.ruoyi.im.vo.message;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息响应VO
 *
 * @author ruoyi
 */
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

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getSenderName() {
            return senderName;
        }

        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public LocalDateTime getSendTime() {
            return sendTime;
        }

        public void setSendTime(LocalDateTime sendTime) {
            this.sendTime = sendTime;
        }

        public Boolean getIsFile() {
            return isFile;
        }

        public void setIsFile(Boolean isFile) {
            this.isFile = isFile;
        }
    }

    public Long getReplyToMessageId() {
        return replyToMessageId;
    }

    public void setReplyToMessageId(Long replyToMessageId) {
        this.replyToMessageId = replyToMessageId;
    }

    public QuotedMessageVO getQuotedMessage() {
        return quotedMessage;
    }

    public void setQuotedMessage(QuotedMessageVO quotedMessage) {
        this.quotedMessage = quotedMessage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getIsRevoked() {
        return isRevoked;
    }

    public void setIsRevoked(Integer isRevoked) {
        this.isRevoked = isRevoked;
    }

    public LocalDateTime getRevokeTime() {
        return revokeTime;
    }

    public void setRevokeTime(LocalDateTime revokeTime) {
        this.revokeTime = revokeTime;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public Boolean getIsSelf() {
        return isSelf;
    }

    public void setIsSelf(Boolean isSelf) {
        this.isSelf = isSelf;
    }
}
