package com.ruoyi.im.domain;

import java.time.LocalDateTime;

/**
 * 群组成员实体
 * 
 * @author ruoyi
 */
public class ImGroupMember {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 群组ID
     */
    private Long groupId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 角色（OWNER群主 ADMIN管理员 MEMBER普通成员）
     */
    private String role;
    
    /**
     * 群内昵称
     */
    private String groupNickname;
    
    /**
     * 禁言截止时间
     */
    private LocalDateTime muteEndTime;
    
    /**
     * 邀请人用户ID
     */
    private Long inviterId;
    
    /**
     * 加入时间
     */
    private LocalDateTime joinedTime;

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
}