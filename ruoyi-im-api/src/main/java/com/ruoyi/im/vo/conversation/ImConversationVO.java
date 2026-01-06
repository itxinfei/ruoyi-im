package com.ruoyi.im.vo.conversation;

import com.ruoyi.im.vo.message.ImMessageVO;

import java.time.LocalDateTime;

/**
 * 会话视图对象
 *
 * @author ruoyi
 */
public class ImConversationVO {

    /**
     * 会话ID
     */
    private Long id;

    /**
     * 会话类型：PRIVATE(单聊)、GROUP(群聊)
     */
    private String type;

    /**
     * 目标ID：单聊为目标用户ID，群聊为群组ID
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
     * 最后消息
     */
    private ImMessageVO lastMessage;

    /**
     * 最后消息时间
     */
    private LocalDateTime lastMessageTime;

    /**
     * 未读消息数
     */
    private Integer unreadCount;

    /**
     * 是否置顶
     */
    private Boolean isPinned;

    /**
     * 是否免打扰
     */
    private Boolean isMuted;

    /**
     * 备注
     */
    private String remark;

    /**
     * 扩展信息（JSON格式）
     */
    private String extraInfo;

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

    public ImMessageVO getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(ImMessageVO lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Boolean getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Boolean isPinned) {
        this.isPinned = isPinned;
    }

    public Boolean getIsMuted() {
        return isMuted;
    }

    public void setIsMuted(Boolean isMuted) {
        this.isMuted = isMuted;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
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