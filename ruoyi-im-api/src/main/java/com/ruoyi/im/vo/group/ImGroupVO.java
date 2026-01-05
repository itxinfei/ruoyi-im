package com.ruoyi.im.vo.group;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * 群组信息VO
 * 
 * @author ruoyi
 */
@ApiModel(description = "群组信息")
public class ImGroupVO {

    @ApiModelProperty(value = "群组ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "群组名称", example = "技术交流群")
    private String name;

    @ApiModelProperty(value = "群主ID", example = "1")
    private Long ownerId;

    @ApiModelProperty(value = "群主用户名", example = "zhangsan")
    private String ownerUsername;

    @ApiModelProperty(value = "群主昵称", example = "张三")
    private String ownerNickname;

    @ApiModelProperty(value = "群组公告", example = "欢迎大家加入技术交流群")
    private String notice;

    @ApiModelProperty(value = "群组头像", example = "/profile/group.png")
    private String avatar;

    @ApiModelProperty(value = "群组状态", example = "ACTIVE")
    private String status;

    @ApiModelProperty(value = "群组状态描述", example = "活跃")
    private String statusDesc;

    @ApiModelProperty(value = "成员数量", example = "10")
    private Integer memberCount;

    @ApiModelProperty(value = "创建时间", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

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
}