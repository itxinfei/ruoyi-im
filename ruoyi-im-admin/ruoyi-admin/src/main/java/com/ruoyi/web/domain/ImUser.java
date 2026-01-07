package com.ruoyi.web.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * IM用户实体
 *
 * 用于存储IM系统中的用户基本信息，包括登录凭证、个人资料、状态信息等
 *
 * @author ruoyi
 */
public class ImUser extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID，主键，自增 */
    private Long id;

    /** 用户名，用于登录系统，唯一标识 */
    private String username;

    /** 昵称，用户在系统中显示的名称 */
    private String nickname;

    /** 密码，加密存储 */
    private String password;

    /** 头像，存储头像文件的URL路径 */
    private String avatar;

    /** 邮箱，用于找回密码或接收通知 */
    private String email;

    /** 手机号，用于登录或接收短信通知 */
    private String mobile;

    /** 性别: 0=未知, 1=男, 2=女 */
    private Integer gender;

    /** 状态: ACTIVE=在线, OFFLINE=离线 */
    private String status;

    /** 签名，用户个人简介或个性签名 */
    private String signature;

    /** 最后在线时间，记录用户最近一次在线的时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastOnlineTime;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
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

    public LocalDateTime getLastOnlineTime() {
        return lastOnlineTime;
    }

    public void setLastOnlineTime(LocalDateTime lastOnlineTime) {
        this.lastOnlineTime = lastOnlineTime;
    }

}
