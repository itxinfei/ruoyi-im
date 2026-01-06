package com.ruoyi.im.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * IM鐢ㄦ埛淇℃伅鍝嶅簲瀵硅薄
 * 
 * @author ruoyi
 * @since 2024-01-05
 */
@ApiModel(description = "IM鐢ㄦ埛淇℃伅")
public class ImUserVO {
    
    @ApiModelProperty(value = "鐢ㄦ埛ID", example = "1")
    private Long id;
    
    @ApiModelProperty(value = "鐢ㄦ埛鍚?, example = "zhangsan")
    private String username;
    
    @ApiModelProperty(value = "鏄电О", example = "寮犱笁")
    private String nickname;
    
    @ApiModelProperty(value = "閭", example = "zhangsan@example.com")
    private String email;
    
    @ApiModelProperty(value = "鎵嬫満鍙?, example = "13800138000")
    private String phone;
    
    @ApiModelProperty(value = "澶村儚URL", example = "https://example.com/avatar.jpg")
    private String avatar;
    
    @ApiModelProperty(value = "鐢ㄦ埛鐘舵€?, example = "online")
    private String status;
    
    @ApiModelProperty(value = "鐢ㄦ埛鐘舵€佹弿杩?, example = "鍦ㄧ嚎")
    private String statusDesc;
    
    @ApiModelProperty(value = "涓€х鍚?, example = "杩欎釜浜哄緢鎳掞紝浠€涔堥兘娌℃湁鐣欎笅")
    private String signature;
    
    @ApiModelProperty(value = "鍒涘缓鏃堕棿", example = "2024-01-01 12:00:00")
    private LocalDateTime createTime;
    
    @ApiModelProperty(value = "鏇存柊鏃堕棿", example = "2024-01-01 12:00:00")
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
