package com.ruoyi.im.vo.group;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * 缇ょ粍淇℃伅VO
 * 
 * @author ruoyi
 */
@ApiModel(description = "缇ょ粍淇℃伅")
public class ImGroupVO {

    @ApiModelProperty(value = "缇ょ粍ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "缇ょ粍鍚嶇О", example = "鎶€鏈氦娴佺兢")
    private String name;

    @ApiModelProperty(value = "缇や富ID", example = "1")
    private Long ownerId;

    @ApiModelProperty(value = "缇や富鐢ㄦ埛鍚?, example = "zhangsan")
    private String ownerUsername;

    @ApiModelProperty(value = "缇や富鏄电О", example = "寮犱笁")
    private String ownerNickname;

    @ApiModelProperty(value = "缇ょ粍鍏憡", example = "娆㈣繋澶у鍔犲叆鎶€鏈氦娴佺兢")
    private String notice;

    @ApiModelProperty(value = "缇ょ粍澶村儚", example = "/profile/group.png")
    private String avatar;

    @ApiModelProperty(value = "缇ょ粍鐘舵€?, example = "ACTIVE")
    private String status;

    @ApiModelProperty(value = "缇ょ粍鐘舵€佹弿杩?, example = "娲昏穬")
    private String statusDesc;

    @ApiModelProperty(value = "鎴愬憳鏁伴噺", example = "10")
    private Integer memberCount;

    @ApiModelProperty(value = "鍒涘缓鏃堕棿", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "鏇存柊鏃堕棿", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    @ApiModelProperty(value = "缇ょ粍鍚嶇О锛堝埆鍚嶏級", example = "鎶€鏈氦娴佺兢")
    private String groupName;
    
    @ApiModelProperty(value = "缇ょ粍鎻忚堪", example = "杩欐槸涓€涓妧鏈氦娴佺兢")
    private String groupDesc;
    
    @ApiModelProperty(value = "缇ょ粍绫诲瀷", example = "COMMON")
    private String groupType;
    
    @ApiModelProperty(value = "鎴愬憳鏁伴噺闄愬埗", example = "200")
    private Integer memberLimit;
    
    @ApiModelProperty(value = "缇や富鍚嶇О", example = "寮犱笁")
    private String ownerName;
    
    @ApiModelProperty(value = "缇や富澶村儚", example = "/profile/avatar.png")
    private String ownerAvatar;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getOwnerNickname() {
        return ownerNickname;
    }

    public void setOwnerNickname(String ownerNickname) {
        this.ownerNickname = ownerNickname;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
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

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDesc() {
        return groupDesc;
    }

    public void setGroupDesc(String groupDesc) {
        this.groupDesc = groupDesc;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public Integer getMemberLimit() {
        return memberLimit;
    }

    public void setMemberLimit(Integer memberLimit) {
        this.memberLimit = memberLimit;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerAvatar() {
        return ownerAvatar;
    }

    public void setOwnerAvatar(String ownerAvatar) {
        this.ownerAvatar = ownerAvatar;
    }
}
