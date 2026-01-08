package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IM消息实体
 *
 * 用于存储IM系统中的消息信息，包括文本、图片、文件、语音、视频等多种类型的消息
 * 支持消息撤回、回复、转发等功能
 *
 * @author ruoyi
 */
@TableName("im_message")
public class ImMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息ID，主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID，关联到im_conversation表 */
    private Long conversationId;

    /** 发送者用户ID，关联到im_user表 */
    private Long senderId;

    /** 消息类型：TEXT文本 FILE文件 IMAGE图片 VOICE语音 VIDEO视频 */
    @TableField("message_type")
    private String messageType;

    /** 消息内容 */
    private String content;

    /** 文件URL */
    @TableField("file_url")
    private String fileUrl;

    /** 文件名 */
    @TableField("file_name")
    private String fileName;

    /** 文件大小 */
    @TableField("file_size")
    private Long fileSize;

    /** 敏感级别：NONE无 WARN警告 BLOCK拦截 */
    @TableField("sensitive_level")
    private String sensitiveLevel;

    /** 是否撤回：0否 1是 */
    @TableField("is_revoked")
    private Integer isRevoked;

    /** 撤回时间 */
    @TableField("revoked_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime revokedTime;

    /** 撤回者ID */
    @TableField("revoker_id")
    private Long revokerId;

    /** 创建时间 */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // ==================== 以下字段为非数据库字段 ====================

    /**
     * 接收者用户ID（非数据库字段，通过conversation获取）
     */
    @TableField(exist = false)
    private Long receiverId;

    /**
     * 父消息ID（非数据库字段，用于回复和转发）
     */
    @TableField(exist = false)
    private Long parentId;

    /**
     * 消息状态（非数据库字段）
     */
    @TableField(exist = false)
    private Integer status;

    /**
     * 发送时间（非数据库字段，使用createTime）
     */
    @TableField(exist = false)
    private LocalDateTime sendTime;

    /**
     * 撤回时间（非数据库字段，使用revokedTime）
     */
    @TableField(exist = false)
    private LocalDateTime revokeTime;

    /**
     * 消息类型（非数据库字段，使用messageType）
     */
    @TableField(exist = false)
    private String type;

    public String getType() {
        return type != null ? type : messageType;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * 回复的消息ID（非数据库字段）
     */
    @TableField(exist = false)
    private Long replyToMessageId;

    /**
     * 转发的消息ID（非数据库字段）
     */
    @TableField(exist = false)
    private Long forwardFromMessageId;

    /**
     * 是否删除（非数据库字段）
     */
    @TableField(exist = false)
    private Integer isDeleted;

    /**
     * 删除时间（非数据库字段）
     */
    @TableField(exist = false)
    private LocalDateTime deletedTime;

    /**
     * 更新时间（非数据库字段）
     */
    @TableField(exist = false)
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

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getSensitiveLevel() {
        return sensitiveLevel;
    }

    public void setSensitiveLevel(String sensitiveLevel) {
        this.sensitiveLevel = sensitiveLevel;
    }

    public Integer getIsRevoked() {
        return isRevoked;
    }

    public void setIsRevoked(Integer isRevoked) {
        this.isRevoked = isRevoked;
    }

    public LocalDateTime getRevokedTime() {
        return revokedTime;
    }

    public void setRevokedTime(LocalDateTime revokedTime) {
        this.revokedTime = revokedTime;
    }

    public Long getRevokerId() {
        return revokerId;
    }

    public void setRevokerId(Long revokerId) {
        this.revokerId = revokerId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public LocalDateTime getRevokeTime() {
        return revokeTime;
    }

    public void setRevokeTime(LocalDateTime revokeTime) {
        this.revokeTime = revokeTime;
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

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
