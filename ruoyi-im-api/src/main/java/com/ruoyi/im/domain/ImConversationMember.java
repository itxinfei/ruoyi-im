package com.ruoyi.im.domain;

import java.time.LocalDateTime;

/**
 * 浼氳瘽鎴愬憳瀹炰綋
 * 
 * @author ruoyi
 */
public class ImConversationMember {
    /**
     * 涓婚敭ID
     */
    private Long id;
    
    /**
     * 浼氳瘽ID
     */
    private Long conversationId;
    
    /**
     * 鐢ㄦ埛ID
     */
    private Long userId;
    
    /**
     * 鏈娑堟伅鏁?     */
    private Integer unreadCount;
    
    /**
     * 鏈€鍚庨槄璇绘秷鎭疘D
     */
    private Long lastReadMessageId;
    
    /**
     * 鏄惁缃《
     */
    private Boolean isPinned;
    
    /**
     * 鏄惁鍏嶆墦鎵?     */
    private Boolean isMuted;
    
    /**
     * 鍒涘缓鏃堕棿
     */
    private LocalDateTime createTime;
    
    /**
     * 鏇存柊鏃堕棿
     */
    private LocalDateTime updateTime;

    // Getters and Setters
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

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Long getLastReadMessageId() {
        return lastReadMessageId;
    }

    public void setLastReadMessageId(Long lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
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
