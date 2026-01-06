package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.im.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IM会话实体
 *
 * 用于存储用户与联系人或群组的会话关系，支持单聊和群聊
 *
 * @author ruoyi
 */
@TableName("im_conversation")
public class ImConversation extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话ID，主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话类型：PRIVATE(单聊)、GROUP(群聊) */
    private String type;

    /** 目标ID：单聊为目标用户ID，群聊为群组ID */
    private Long targetId;

    /** 会话名称 */
    private String name;

    /** 会话头像 */
    private String avatar;

    /** 最后消息ID */
    private Long lastMessageId;

    /** 最后消息时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageTime;

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