package com.ruoyi.im.domain;

import java.time.LocalDateTime;

/**
 * 濂藉弸鐢宠瀹炰綋
 * 
 * @author ruoyi
 */
public class ImFriendRequest {
    /**
     * 涓婚敭ID
     */
    private Long id;
    
    /**
     * 鐢宠浜虹敤鎴稩D
     */
    private Long fromUserId;
    
    /**
     * 琚敵璇蜂汉鐢ㄦ埛ID
     */
    private Long toUserId;
    
    /**
     * 鐢宠娑堟伅
     */
    private String message;
    
    /**
     * 鐘舵€侊紙PENDING寰呭鐞?APPROVED宸插悓鎰?REJECTED宸叉嫆缁濓級
     */
    private String status;
    
    /**
     * 鍒涘缓鏃堕棿
     */
    private LocalDateTime createTime;
    
    /**
     * 澶勭悊鏃堕棿
     */
    private LocalDateTime handledTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getHandledTime() {
        return handledTime;
    }

    public void setHandledTime(LocalDateTime handledTime) {
        this.handledTime = handledTime;
    }
}
