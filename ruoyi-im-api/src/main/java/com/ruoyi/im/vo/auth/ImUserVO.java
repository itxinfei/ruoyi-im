package com.ruoyi.im.vo.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * 鐢ㄦ埛淇℃伅VO
 * 
 * @author ruoyi
 * @since 2024-01-05
 */
@ApiModel(description = "鐢ㄦ埛淇℃伅")
public class ImUserVO {

    @ApiModelProperty(value = "鐢ㄦ埛ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "鐢ㄦ埛鍚?, example = "admin")
    private String username;

    @ApiModelProperty(value = "鏄电О", example = "绠＄悊鍛?)
    private String nickname;

    @ApiModelProperty(value = "澶村儚", example = "/profile/avatar.png")
    private String avatar;

    @ApiModelProperty(value = "閭", example = "admin@example.com")
    private String email;

    @ApiModelProperty(value = "鎵嬫満鍙?, example = "13800138000")
    private String phone;

    @ApiModelProperty(value = "鐢ㄦ埛鐘舵€?, example = "ACTIVE")
    private String status;

    @ApiModelProperty(value = "鐢ㄦ埛鐘舵€佹弿杩?, example = "姝ｅ父")
    private String statusDesc;

    @ApiModelProperty(value = "鏈€鍚庣櫥褰曟椂闂?, example = "2024-01-05 10:30:00")
    private LocalDateTime lastLoginTime;

    @ApiModelProperty(value = "娉ㄥ唽鏃堕棿", example = "2024-01-01 09:00:00")
    private LocalDateTime createTime;

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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
