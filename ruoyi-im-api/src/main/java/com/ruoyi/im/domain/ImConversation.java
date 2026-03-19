package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.im.common.BaseEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IM会话实体
 *
 * @author ruoyi
 */
@TableName("im_conversation")
public class ImConversation extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话类型：PRIVATE(单聊)、GROUP(群聊) */
    private String type;

    /** 目标ID：单聊为目标用户ID，群聊为群组ID */
    @TableField("target_id")
    private Long targetId;

    /** 会话名称 */
    private String name;

    /** 会话头像 */
    private String avatar;

    /** 最后消息ID */
    @TableField("last_message_id")
    private Long lastMessageId;

    /** 最后消息时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("last_message_time")
    private LocalDateTime lastMessageTime;

    /** 是否删除：0=否, 1=是 */
    @TableField("is_deleted")
    private Integer isDeleted;

    /** 删除时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("deleted_time")
    private LocalDateTime deletedTime;

    /** 是否敏感会话：0=否, 1=是 */
    @TableField("is_sensitive")
    private Integer isSensitive;

    /** 敏感级别：NORMAL=普通, CONFIDENTIAL=机密, SECRET=绝密 */
    @TableField("sensitive_level")
    private String sensitiveLevel;

    /** 禁止转发：0=允许, 1=禁止 */
    @TableField("no_forward")
    private Integer noForward;

    /** 禁止复制：0=允许, 1=禁止 */
    @TableField("no_copy")
    private Integer noCopy;

    // ==================== Getter and Setter ====================

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

    public Integer getIsSensitive() {
        return isSensitive;
    }

    public void setIsSensitive(Integer isSensitive) {
        this.isSensitive = isSensitive;
    }

    public String getSensitiveLevel() {
        return sensitiveLevel;
    }

    public void setSensitiveLevel(String sensitiveLevel) {
        this.sensitiveLevel = sensitiveLevel;
    }

    public Integer getNoForward() {
        return noForward;
    }

    public void setNoForward(Integer noForward) {
        this.noForward = noForward;
    }

    public Integer getNoCopy() {
        return noCopy;
    }

    public void setNoCopy(Integer noCopy) {
        this.noCopy = noCopy;
    }
}
