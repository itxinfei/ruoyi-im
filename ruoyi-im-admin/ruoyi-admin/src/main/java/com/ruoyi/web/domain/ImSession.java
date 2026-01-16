package com.ruoyi.web.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IM会话实体（已废弃）
 *
 * 该实体已被 ImConversationMember 替代
 * 请使用 ImConversationMember 进行会话成员管理
 *
 * @deprecated 使用 {@link } 替代
 * @author ruoyi
 */
@Deprecated
public class ImSession extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话ID，主键，自增 */
    private Long id;

    /** 会话类型: private=私聊, group=群聊 */
    private String type;

    /** 会话名称，私聊时为对方昵称，群聊时为群组名称 */
    private String name;

    /** 会话头像，私聊时为对方头像，群聊时为群组头像 */
    private String avatar;

    /** 对方ID，私聊时为对方用户ID，群聊时为群组ID */
    private Long peerId;

    /** 所属用户ID，表示该会话属于哪个用户 */
    private Long userId;

    /** 未读消息数，记录该会话中未读消息的数量 */
    private Integer unreadCount;

    /** 是否置顶: 0=否, 1=是，置顶的会话显示在会话列表顶部 */
    private Integer isPinned;

    /** 是否免打扰: 0=否, 1=是，免打扰的会话不会推送通知 */
    private Integer isMuted;

    /** 最后消息内容，用于在会话列表中显示最后一条消息 */
    private String lastMessage;

    /** 最后消息ID */
    private Long lastMessageId;

    /** 最后消息时间，用于会话列表排序 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageTime;

    /** 最后消息类型，用于显示不同的消息图标 */
    private String lastMessageType;

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

    public Long getPeerId() {
        return peerId;
    }

    public void setPeerId(Long peerId) {
        this.peerId = peerId;
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

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
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

    public String getLastMessageType() {
        return lastMessageType;
    }

    public void setLastMessageType(String lastMessageType) {
        this.lastMessageType = lastMessageType;
    }

}
