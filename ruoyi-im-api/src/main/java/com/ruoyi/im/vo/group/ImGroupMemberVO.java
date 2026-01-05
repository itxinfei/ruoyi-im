package com.ruoyi.im.vo.group;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * 群组成员信息VO
 * 
 * @author ruoyi
 */
@ApiModel(description = "群组成员信息")
public class ImGroupMemberVO {

    @ApiModelProperty(value = "成员ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "群组ID", example = "1")
    private Long groupId;

    @ApiModelProperty(value = "用户ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "用户用户名", example = "zhangsan")
    private String username;

    @ApiModelProperty(value = "用户昵称", example = "张三")
    private String userNickname;

    @ApiModelProperty(value = "用户头像", example = "/profile/avatar.png")
    private String avatar;

    @ApiModelProperty(value = "群组角色", example = "MEMBER")
    private String role;

    @ApiModelProperty(value = "群组角色描述", example = "普通成员")
    private String roleDesc;

    @ApiModelProperty(value = "群昵称", example = "技术大牛")
    private String groupNickname;

    @ApiModelProperty(value = "邀请人ID", example = "1")
    private Long inviterId;

    @ApiModelProperty(value = "邀请人用户名", example = "lisi")
    private String inviterUsername;

    @ApiModelProperty(value = "邀请人昵称", example = "李四")
    private String inviterNickname;

    @ApiModelProperty(value = "加入时间", example = "2024-01-01 10:00:00")
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