package com.ruoyi.im.vo.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应VO
 *
 * @author ruoyi
 */
@Data
public class ImLoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 访问令牌 */
    private String token;

    /** 用户信息 */
    private UserInfo userInfo;

    /**
     * 用户信息内部类
     */
    @Data
    public static class UserInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        private String role;
    }
}
