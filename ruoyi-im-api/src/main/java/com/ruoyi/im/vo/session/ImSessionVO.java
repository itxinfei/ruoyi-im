package com.ruoyi.im.vo.session;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话响应VO
 *
 * @author ruoyi
 */
public class ImSessionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话ID */
    private Long id;

    /** 会话类型: private=私聊, group=群聊 */
    private String type;

    /** 会话名称 */
    private String name;

    /** 会话头像 */
    private String avatar;

    /** 对方ID（私聊时为对方ID，群聊时为群组ID） */
    private Long peerId;

    /** 未读消息数 */
    private Integer unreadCount;

    /** 是否置顶: 0=否, 1=是 */
    private Integer isPinned;

    /** 是否免打扰: 0=否, 1=是 */
    private Integer isMuted;

    /** 最后消息内容 */
    private String lastMessage;

    /** 最后消息时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageTime;

    /** 最后消息类型 */
    private String lastMessageType;

    /** 对方在线状态（仅私聊） */
    private Boolean online;

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

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }
}
