package com.ruoyi.im.domain;

import java.time.LocalDateTime;

/**
 * 缇ょ粍鎴愬憳瀹炰綋
 * 
 * @author ruoyi
 */
public class ImGroupMember {
    /**
     * 涓婚敭ID
     */
    private Long id;
    
    /**
     * 缇ょ粍ID
     */
    private Long groupId;
    
    /**
     * 鐢ㄦ埛ID
     */
    private Long userId;
    
    /**
     * 瑙掕壊锛圤WNER缇や富 ADMIN绠＄悊鍛?MEMBER鏅€氭垚鍛橈級
     */
    private String role;
    
    /**
     * 缇ゅ唴鏄电О
     */
    private String groupNickname;
    
    /**
     * 绂佽█鎴鏃堕棿
     */
    private LocalDateTime muteEndTime;
    
    /**
     * 閭€璇蜂汉鐢ㄦ埛ID
     */
    private Long inviterId;
    
    /**
     * 鍔犲叆鏃堕棿
     */
    private LocalDateTime joinedTime;
    
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

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getGroupNickname() {
        return groupNickname;
    }

    public void setGroupNickname(String groupNickname) {
        this.groupNickname = groupNickname;
    }

    public LocalDateTime getMuteEndTime() {
        return muteEndTime;
    }

    public void setMuteEndTime(LocalDateTime muteEndTime) {
        this.muteEndTime = muteEndTime;
    }

    public Long getInviterId() {
        return inviterId;
    }

    public void setInviterId(Long inviterId) {
        this.inviterId = inviterId;
    }

    public LocalDateTime getJoinedTime() {
        return joinedTime;
    }

    public void setJoinedTime(LocalDateTime joinedTime) {
        this.joinedTime = joinedTime;
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
