package com.ruoyi.web.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话实体
 *
 * 对应数据库表 im_conversation
 * 实际数据库字段：id, type, target_id, name, avatar, last_message_id, last_message_time,
 * is_deleted, deleted_time, create_time, update_time
 *
 * @author ruoyi
 */
public class ImConversation extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID，主键
     */
    private Long id;

    /**
     * 类型: PRIVATE私聊 GROUP群聊
     */
    private String type;

    /**
     * 目标ID（私聊时为对方用户ID，群聊时为群组ID）
     */
    private Long targetId;

    /**
     * 会话名称
     */
    private String name;

    /**
     * 会话头像
     */
    private String avatar;

    /**
     * 备注
     */
    private String remark;

    /**
     * 最后消息ID
     */
    private Long lastMessageId;

    /**
     * 最后消息时间
     */
    private LocalDateTime lastMessageTime;

    /**
     * 是否删除: 0否 1是
     */
    private Integer isDeleted;

    /**
     * 删除时间
     */
    private LocalDateTime deletedTime;

    // ========== 关联查询字段（非数据库字段） ==========

    /**
     * 成员数量
     */
    private Integer memberCount;

    /**
     * 未读消息总数
     */
    private Integer totalUnreadCount;

    /**
     * 目标名称（用户名或群组名）
     */
    private String targetName;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(Long lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
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

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public Integer getTotalUnreadCount() {
        return totalUnreadCount;
    }

    public void setTotalUnreadCount(Integer totalUnreadCount) {
        this.totalUnreadCount = totalUnreadCount;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }
}
