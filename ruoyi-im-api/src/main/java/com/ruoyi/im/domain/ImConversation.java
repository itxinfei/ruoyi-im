package com.ruoyi.im.domain;

import java.time.LocalDateTime;

/**
 * 会话实体
 * 
 * @author ruoyi
 */
public class ImConversation {
    /**
     * 会话ID
     */
    private Long id;
    
    /**
     * 会话类型（PRIVATE私聊 GROUP群聊）
     */
    private String type;
    
    /**
     * 目标ID（私聊为好友ID，群聊为群组ID）
     */
    private Long targetId;
    
    /**
     * 最后一条消息ID
     */
    private Long lastMessageId;
    
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Long getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(Long lastMessageId) {
        this.lastMessageId = lastMessageId;
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