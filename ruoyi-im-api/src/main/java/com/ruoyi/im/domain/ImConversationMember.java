package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
    @TableField("conversation_id")
    private Long conversationId;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 群内昵称 */
    private String nickname;

    /** 角色 */
    private String role;

    /** 未读消息数 */
    @TableField("unread_count")
    private Integer unreadCount;

    /** 是否置顶 */
    @TableField("is_pinned")
    private Integer isPinned;

    /** 是否免打扰 */
    @TableField("is_muted")
    private Integer isMuted;

    /** 最后已读消息ID */
    @TableField("last_read_message_id")
    private Long lastReadMessageId;

    /** 最后已读时间 */
    @TableField("last_read_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastReadTime;

    /** 是否删除：0=否, 1=是 */
    @TableField("is_deleted")
    private Integer isDeleted;

    /** 删除时间 */
    @TableField("deleted_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedTime;

    /** 创建时间 */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // ==================== Getters and Setters ====================

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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Integer getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Integer isPinned) {
        this.isPinned = isPinned;
    }

    public Integer getIsMuted() {
        return isMuted;
    }

    public void setIsMuted(Integer isMuted) {
        this.isMuted = isMuted;
    }

    public Long getLastReadMessageId() {
        return lastReadMessageId;
    }

    public void setLastReadMessageId(Long lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }

    public LocalDateTime getLastReadTime() {
        return lastReadTime;
    }

    public void setLastReadTime(LocalDateTime lastReadTime) {
        this.lastReadTime = lastReadTime;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public LocalDateTime getDeletedTime() {
        return deletedTime;
    }

    public void setDeletedTime(LocalDateTime deletedTime) {
        this.deletedTime = deletedTime;
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

    // 为MyBatis映射兼容性添加的方法
    public boolean isPinned() {
        return isPinned != null && isPinned == 1;
    }

    public boolean isMuted() {
        return isMuted != null && isMuted == 1;
    }

    public boolean isDeleted() {
        return isDeleted != null && isDeleted == 1;
    }
}