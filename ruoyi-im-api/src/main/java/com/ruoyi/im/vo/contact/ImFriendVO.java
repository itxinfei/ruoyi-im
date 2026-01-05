package com.ruoyi.im.vo.contact;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * 好友信息视图对象
 * 
 * @author ruoyi
 */
@ApiModel(description = "好友信息")
public class ImFriendVO {

    @ApiModelProperty(value = "好友关系ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "好友用户ID", example = "1")
    private Long friendUserId;

    @ApiModelProperty(value = "好友用户名", example = "zhangsan")
    private String friendUsername;

    @ApiModelProperty(value = "好友昵称", example = "张三")
    private String friendNickname;

    @ApiModelProperty(value = "好友头像", example = "/profile/avatar.png")
    private String friendAvatar;

    @ApiModelProperty(value = "好友状态", example = "ACTIVE")
    private String friendStatus;

    @ApiModelProperty(value = "好友状态描述", example = "在线")
    private String friendStatusDesc;

    @ApiModelProperty(value = "好友邮箱", example = "zhangsan@example.com")
    private String friendEmail;

    @ApiModelProperty(value = "好友电话", example = "13800138000")
    private String friendPhone;

    @ApiModelProperty(value = "好友别名", example = "张三")
    private String alias;

    @ApiModelProperty(value = "好友备注", example = "技术专家")
    private String remark;

    @ApiModelProperty(value = "好友关系状态", example = "ACTIVE")
    private String friendRelationStatus;

    @ApiModelProperty(value = "好友关系状态描述", example = "正常")
    private String friendRelationStatusDesc;

    @ApiModelProperty(value = "是否为本人的好友", example = "true")
    private Boolean isFriend;

    @ApiModelProperty(value = "好友关系创建时间", example = "2023-12-01T10:30:00")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "好友关系更新时间", example = "2023-12-01T10:30:00")
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFriendUserId() {
        return friendUserId;
    }

    public void setFriendUserId(Long friendUserId) {
        this.friendUserId = friendUserId;
    }

    public String getFriendUsername() {
        return friendUsername;
    }

    public void setFriendUsername(String friendUsername) {
        this.friendUsername = friendUsername;
    }

    public String getFriendNickname() {
        return friendNickname;
    }

    public void setFriendNickname(String friendNickname) {
        this.friendNickname = friendNickname;
    }

    public String getFriendAvatar() {
        return friendAvatar;
    }

    public void setFriendAvatar(String friendAvatar) {
        this.friendAvatar = friendAvatar;
    }

    public String getFriendStatus() {
        return friendStatus;
    }

    public void setFriendStatus(String friendStatus) {
        this.friendStatus = friendStatus;
    }

    public String getFriendStatusDesc() {
        return friendStatusDesc;
    }

    public void setFriendStatusDesc(String friendStatusDesc) {
        this.friendStatusDesc = friendStatusDesc;
    }

    public String getFriendEmail() {
        return friendEmail;
    }

    public void setFriendEmail(String friendEmail) {
        this.friendEmail = friendEmail;
    }

    public String getFriendPhone() {
        return friendPhone;
    }

    public void setFriendPhone(String friendPhone) {
        this.friendPhone = friendPhone;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getFriendRelationStatus() {
        return friendRelationStatus;
    }

    public void setFriendRelationStatus(String friendRelationStatus) {
        this.friendRelationStatus = friendRelationStatus;
    }

    public String getFriendRelationStatusDesc() {
        return friendRelationStatusDesc;
    }

    public void setFriendRelationStatusDesc(String friendRelationStatusDesc) {
        this.friendRelationStatusDesc = friendRelationStatusDesc;
    }

    public Boolean getIsFriend() {
        return isFriend;
    }

    public void setIsFriend(Boolean isFriend) {
        this.isFriend = isFriend;
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