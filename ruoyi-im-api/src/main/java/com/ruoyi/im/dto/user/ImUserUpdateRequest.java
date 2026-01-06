package com.ruoyi.im.dto.user;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 用户信息更新请求
 *
 * @author ruoyi
 */
public class ImUserUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 昵称 */
    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String mobile;

    /** 性别: 0=未知, 1=男, 2=女 */
    private Integer gender;

    /** 签名 */
    @Size(max = 200, message = "签名长度不能超过200")
    private String signature;

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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
