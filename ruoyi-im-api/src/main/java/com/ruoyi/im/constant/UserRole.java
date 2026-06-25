package com.ruoyi.im.constant;

/**
 * 用户角色常量
 *
 * @author ruoyi
 */
public class UserRole {

    /** 普通用户 */
    public static final String USER = "USER";

    /** 管理员 */
    public static final String ADMIN = "ADMIN";

    /** 超级管理员 */
    public static final String SUPER_ADMIN = "SUPER_ADMIN";

    /** 角色前缀，Spring Security 需要 */
    public static final String ROLE_PREFIX = "ROLE_";

    /**
     * 获取带前缀的角色名称，用于 Spring Security
     *
     * @param role 角色名称
     * @return 带前缀的角色名称，如 "ROLE_ADMIN"
     */
    public static String withPrefix(String role) {
        if (role == null) {
            return null;
        }
        return role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role;
    }
}
