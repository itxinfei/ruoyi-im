package com.ruoyi.im.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * IM用户创建请求对象
 * 
 * @author ruoyi
 * @since 2024-01-05
 */
@ApiModel(description = "IM用户创建请求")
public class ImUserCreateRequest {
    
    @ApiModelProperty(value = "用户名", required = true, example = "zhangsan")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;
    
    @ApiModelProperty(value = "昵称", required = true, example = "张三")
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;
    
    @ApiModelProperty(value = "密码", required = true, example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;
    
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
    private String status = "online";
    
    @ApiModelProperty(value = "个性签名", example = "这个人很懒，什么都没有留下")
    @Size(max = 200, message = "个性签名长度不能超过200个字符")
    private String signature;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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