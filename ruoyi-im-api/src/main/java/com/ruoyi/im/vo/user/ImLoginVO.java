package com.ruoyi.im.vo.user;

import java.io.Serializable;

/**
 * 登录响应VO
 *
 * @author ruoyi
 */
public class ImLoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 访问令牌 */
    private String token;

    /** 用户信息 */
    private UserInfo userInfo;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    /**
     * 用户信息内部类
     */
    public static class UserInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long id;
        private String username;
        private String nickname;
        private String avatar;

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
    }
}
