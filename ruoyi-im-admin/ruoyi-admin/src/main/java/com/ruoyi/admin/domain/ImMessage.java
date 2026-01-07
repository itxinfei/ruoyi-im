package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
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

    /** 消息类型：TEXT文本 FILE文件 NOTICE通知 RECALL撤回 REPLY回复 FORWARD转发 IMAGE图片 VOICE语音 VIDEO视频 */
    private String type;

    /** 消息内容，JSON格式 */
    private String content;

    /** 回复的消息ID */
    private Long replyToMessageId;

    /** 转发的消息ID */
    private Long forwardFromMessageId;

    /** 是否撤回：0否 1是 */
    private Integer isRevoked;

    /** 撤回时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime revokedTime;

    /** 敏感级别：NONE无 WARN警告 BLOCK拦截 */
    private String sensitiveLevel;

    /** 是否删除：0否 1是 */
    private Integer isDeleted;

    /** 删除时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedTime;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

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

    public String getSensitiveLevel() {
        return sensitiveLevel;
    }

    public void setSensitiveLevel(String sensitiveLevel) {
        this.sensitiveLevel = sensitiveLevel;
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
}
