package com.ruoyi.im.util;

import com.ruoyi.im.domain.ImUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.ruoyi.im.util.JwtUtils;
import com.ruoyi.im.util.SpringContextUtils;

/**
 * 安全工具类
 *
 * 用于获取当前认证用户信息
 *
 * @author ruoyi
 */
@Component
public class SecurityUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityUtils.class);

    /**
     * 获取当前认证用户
     *
     * @return 当前用户，如果未认证则返回null
     */
    public static ImUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof ImUser) {
                return (ImUser) principal;
            }
        }

        return null;
    }

    /**
     * 获取当前认证用户ID
     *
     * @return 当前用户ID
     * @throws com.ruoyi.im.exception.BusinessException 如果未登录
     */
    public static Long getLoginUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new com.ruoyi.im.exception.BusinessException(401, "未登录或Token已过期");
        }
        return userId;
    }

    /**
     * 获取当前认证用户ID (允许为空)
     *
     * @return 当前用户ID，如果未认证则返回null
     */
    public static Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof ImUser) {
                    return ((ImUser) principal).getId();
                } else if (principal instanceof String && "anonymousUser".equals(principal)) {
                    // 匿名用户
                    return null;
                } else {
                    // 其他类型的principal，记录日志
                    LOGGER.warn("未知的principal类型: {}", principal != null ? principal.getClass().getName() : "null");
                    return null;
                }
            }
        } catch (Exception e) {
            LOGGER.error("获取当前用户ID时发生异常", e);
            throw new RuntimeException("获取用户信息失败", e);
        }
        return null;
    }

    /**
     * 获取当前认证用户名
     *
     * @return 当前用户名，如果未登录则返回null
     */
    public static String getCurrentUsername() {
        ImUser user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 从Token中获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public static String getUsernameFromToken(String token) {
        try {
            // 获取 Spring 容器中的 JwtUtils
            JwtUtils jwtUtils = SpringContextUtils.getBean(JwtUtils.class);
            return jwtUtils.getUsernameFromToken(token);
        } catch (Exception e) {
            LOGGER.error("通过JwtUtils解析Token失败", e);
            return null;
        }
    }

    /**
     * 检查用户是否已认证
     *
     * @return true 如果用户已认证，否则 false
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * 检查当前用户是否是指定用户
     *
     * @param userId 用户ID
     * @return true 如果当前用户是指定用户，否则 false
     */
    public static boolean isCurrentUser(Long userId) {
        Long currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(userId);
    }

    /**
     * 检查当前用户是否有指定权限
     *
     * @param permission 权限标识
     * @return true 如果有权限，否则 false
     */
    public static boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals(permission));
        }

        return false;
    }
}
