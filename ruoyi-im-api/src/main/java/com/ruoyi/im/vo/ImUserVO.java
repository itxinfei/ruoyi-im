package com.ruoyi.im.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * IM用户信息响应对象
 * 
 * @author ruoyi
 * @since 2024-01-05
 */
@ApiModel(description = "IM用户信息")
public class ImUserVO {
    
    @ApiModelProperty(value = "用户ID", example = "1")
    private Long id;
    
    @ApiModelProperty(value = "用户名", example = "zhangsan")
    private String username;
    
    @ApiModelProperty(value = "昵称", example = "张三")
    private String nickname;
    
    @ApiModelProperty(value = "邮箱", example = "zhangsan@example.com")
    private String email;
    
    @ApiModelProperty(value = "手机号", example = "13800138000")
    private String phone;
    
    @ApiModelProperty(value = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
    
    @ApiModelProperty(value = "用户状态", example = "online")
    private String status;
    
    @ApiModelProperty(value = "用户状态描述", example = "在线")
    private String statusDesc;
    
    @ApiModelProperty(value = "个性签名", example = "这个人很懒，什么都没有留下")
    private String signature;
    
    @ApiModelProperty(value = "创建时间", example = "2024-01-01 12:00:00")
    private LocalDateTime createTime;
    
    @ApiModelProperty(value = "更新时间", example = "2024-01-01 12:00:00")
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
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