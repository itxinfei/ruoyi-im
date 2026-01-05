package com.ruoyi.im.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * IM用户更新请求对象
 * 
 * @author ruoyi
 * @since 2024-01-05
 */
@ApiModel(description = "IM用户更新请求")
public class ImUserUpdateRequest {
    
    @ApiModelProperty(value = "用户ID", required = true, example = "1")
    @NotNull(message = "用户ID不能为空")
    private Long id;
    
    @ApiModelProperty(value = "昵称", example = "张三")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;
    
    @ApiModelProperty(value = "邮箱", example = "zhangsan@example.com")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;
    
    @ApiModelProperty(value = "手机号", example = "13800138000")
    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;
    
    @ApiModelProperty(value = "头像URL", example = "https://example.com/avatar.jpg")
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatar;
    
    @ApiModelProperty(value = "用户状态", example = "online")
    private String status;
    
    @ApiModelProperty(value = "个性签名", example = "这个人很懒，什么都没有留下")
    @Size(max = 200, message = "个性签名长度不能超过200个字符")
    private String signature;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}