package com.ruoyi.im.vo.group;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * 缇ょ粍鎴愬憳淇℃伅VO
 * 
 * @author ruoyi
 */
@ApiModel(description = "缇ょ粍鎴愬憳淇℃伅")
public class ImGroupMemberVO {

    @ApiModelProperty(value = "鎴愬憳ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "缇ょ粍ID", example = "1")
    private Long groupId;

    @ApiModelProperty(value = "鐢ㄦ埛ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "鐢ㄦ埛鐢ㄦ埛鍚?, example = "zhangsan")
    private String username;

    @ApiModelProperty(value = "鐢ㄦ埛鏄电О", example = "寮犱笁")
    private String userNickname;

    @ApiModelProperty(value = "鐢ㄦ埛澶村儚", example = "/profile/avatar.png")
    private String avatar;

    @ApiModelProperty(value = "缇ょ粍瑙掕壊", example = "MEMBER")
    private String role;

    @ApiModelProperty(value = "缇ょ粍瑙掕壊鎻忚堪", example = "鏅€氭垚鍛?)
    private String roleDesc;

    @ApiModelProperty(value = "缇ゆ樀绉?, example = "鎶€鏈ぇ鐗?)
    private String groupNickname;

    @ApiModelProperty(value = "閭€璇蜂汉ID", example = "1")
    private Long inviterId;

    @ApiModelProperty(value = "閭€璇蜂汉鐢ㄦ埛鍚?, example = "lisi")
    private String inviterUsername;

    @ApiModelProperty(value = "閭€璇蜂汉鏄电О", example = "鏉庡洓")
    private String inviterNickname;

    @ApiModelProperty(value = "鍔犲叆鏃堕棿", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinedTime;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }
    
    public void setNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRoleDesc() {
        return roleDesc;
    }

    public void setRoleDesc(String roleDesc) {
        this.roleDesc = roleDesc;
    }

    public String getGroupNickname() {
        return groupNickname;
    }

    public void setGroupNickname(String groupNickname) {
        this.groupNickname = groupNickname;
    }

    public Long getInviterId() {
        return inviterId;
    }

    public void setInviterId(Long inviterId) {
        this.inviterId = inviterId;
    }

    public String getInviterUsername() {
        return inviterUsername;
    }

    public void setInviterUsername(String inviterUsername) {
        this.inviterUsername = inviterUsername;
    }

    public String getInviterNickname() {
        return inviterNickname;
    }

    public void setInviterNickname(String inviterNickname) {
        this.inviterNickname = inviterNickname;
    }

    public LocalDateTime getJoinedTime() {
        return joinedTime;
    }

    public void setJoinedTime(LocalDateTime joinedTime) {
        this.joinedTime = joinedTime;
    }
}
