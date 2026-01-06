package com.ruoyi.im.vo.group;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组成员视图对象
 *
 * @author ruoyi
 */
public class ImGroupMemberVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成员ID
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
     * 用户名称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 角色（OWNER群主 ADMIN管理员 MEMBER普通成员）
     */
    private String role;

    /**
     * 群内昵称
     */
    private String groupNickname;

    /**
     * 禁言止时间
     */
    private LocalDateTime muteEndTime;

    /**
     * 加入时间
     */
    private LocalDateTime joinedTime;

    /**
     * 是否被禁言
     */
    private Boolean isMuted;

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
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

    public LocalDateTime getJoinedTime() {
        return joinedTime;
    }

    public void setJoinedTime(LocalDateTime joinedTime) {
        this.joinedTime = joinedTime;
    }

    public Boolean getIsMuted() {
        return isMuted;
    }

    public void setIsMuted(Boolean isMuted) {
        this.isMuted = isMuted;
    }
}
