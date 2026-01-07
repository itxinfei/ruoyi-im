package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IM消息已读回执实体
 *
 * 用于精确追踪每个用户对每条消息的已读状态
 * 解决 im_message 表的 status 字段无法区分不同用户已读状态的问题
 *
 * @author ruoyi
 */
@TableName("im_message_read")
public class ImMessageRead implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 消息ID，关联到im_message表 */
    private Long messageId;

    /** 已读用户ID，关联到im_user表 */
    private Long userId;

    /** 会话ID，关联到im_conversation表（冗余字段，用于快速查询） */
    private Long conversationId;

    /** 已读时间，用户阅读消息的时间点 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    /** 已读类型: READ=已读, RECEIPT=已读回执 */
    private String readType;

    /** 设备类型: WEB=网页, MOBILE=移动端, PC=PC端 */
    private String deviceType;

    /** 创建时间，记录创建的时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public LocalDateTime getReadTime() {
        return readTime;
    }

    public void setReadTime(LocalDateTime readTime) {
        this.readTime = readTime;
    }

    public String getReadType() {
        return readType;
    }

    public void setReadType(String readType) {
        this.readType = readType;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "ImMessageRead{" +
                "id=" + id +
                ", messageId=" + messageId +
                ", userId=" + userId +
                ", conversationId=" + conversationId +
                ", readTime=" + readTime +
                ", readType='" + readType + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
