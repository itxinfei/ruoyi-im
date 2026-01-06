package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.im.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IM会话成员实体
 *
 * 用于存储用户在会话中的状态信息，如未读消息数、是否置顶、是否免打扰等
 *
 * @author ruoyi
 */
@TableName("im_conversation_member")
public class ImConversationMember extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 关系ID，主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID */
    private Long conversationId;

    /** 用户ID */
    private Long userId;

    /** 未读消息数 */
    private Integer unreadCount;

    /** 最后已读消息ID */
    private Long lastReadMessageId;

    /** 是否置顶 */
    private Boolean isPinned;

    /** 是否免打扰 */
    private Boolean isMuted;

    /** 是否已删除（用户退出会话） */
    private Boolean isDeleted;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Long getLastReadMessageId() {
        return lastReadMessageId;
    }

    public void setLastReadMessageId(Long lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }

    public Boolean getPinned() {
        return isPinned;
    }

    public void setPinned(Boolean pinned) {
        isPinned = pinned;
    }

    public Boolean getMuted() {
        return isMuted;
    }

    public void setMuted(Boolean muted) {
        isMuted = muted;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
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