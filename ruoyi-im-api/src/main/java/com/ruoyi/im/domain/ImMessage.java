package com.ruoyi.im.domain;

import java.time.LocalDateTime;

/**
 * 消息实体
 * 
 * @author ruoyi
 */
public class ImMessage {
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 会话ID
     */
    private Long conversationId;
    
    /**
     * 发送者用户ID
     */
    private Long senderId;
    
    /**
     * 消息类型（TEXT文本 FILE文件 NOTICE通知 RECALL撤回 REPLY回复 FORWARD转发 IMAGE图片 VOICE语音）
     */
    private String type;
    
    /**
     * 消息内容（JSON格式）
     */
    private String content;
    
    /**
     * 回复的消息ID
     */
    private Long replyToMessageId;
    
    /**
     * 转发的消息ID
     */
    private Long forwardFromMessageId;
    
    /**
     * 消息状态（SENT已发送 DELIVERED已送达 READ已读 REVOKED已撤回）
     */
    private String status;
    
    /**
     * 敏感级别（NONE无 WARN警告 BLOCK拦截）
     */
    private String sensitiveLevel;
    
    /**
     * 撤回时间
     */
    private LocalDateTime revokedTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    // Getters and Setters
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

    public Long getReplyToMessageId() {
        return replyToMessageId;
    }

    public void setReplyToMessageId(Long replyToMessageId) {
        this.replyToMessageId = replyToMessageId;
    }

    public Long getForwardFromMessageId() {
        return forwardFromMessageId;
    }

    public void setForwardFromMessageId(Long forwardFromMessageId) {
        this.forwardFromMessageId = forwardFromMessageId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSensitiveLevel() {
        return sensitiveLevel;
    }

    public void setSensitiveLevel(String sensitiveLevel) {
        this.sensitiveLevel = sensitiveLevel;
    }

    public LocalDateTime getRevokedTime() {
        return revokedTime;
    }

    public void setRevokedTime(LocalDateTime revokedTime) {
        this.revokedTime = revokedTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}