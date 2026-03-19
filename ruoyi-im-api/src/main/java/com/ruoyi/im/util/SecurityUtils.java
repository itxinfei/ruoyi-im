package com.ruoyi.im.util;

import com.ruoyi.im.domain.ImUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

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

    private static JwtUtils jwtUtils;

    @Autowired
    public void setJwtUtils(JwtUtils jwtUtils) {
        SecurityUtils.jwtUtils = jwtUtils;
    }

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
                    return null;
                } else {
                    LOGGER.warn("未知的principal类型: {}", principal != null ? principal.getClass().getName() : "null");
                    return null;
                }
            }
        } catch (Exception e) {
            LOGGER.error("获取当前用户ID时发生异常", e);
            return null;
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
            // 复用 JwtUtils 的方法，避免硬编码密钥
            if (jwtUtils == null) {
                LOGGER.error("JwtUtils 未初始化，无法解析Token");
                return null;
            }
            return jwtUtils.getUsernameFromToken(token);
        } catch (Exception e) {
            LOGGER.error("解析Token失败", e);
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

    /**
     * 获取当前登录用户的角色
     *
     * @return 用户角色 (USER/ADMIN/SUPER_ADMIN)，如果未登录则返回 null
     */
    public static String getLoginUserRole() {
        ImUser user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }

    /**
     * 检查当前用户是否是管理员
     *
     * @return true 如果是管理员角色
     */
    public static boolean isAdmin() {
        String role = getLoginUserRole();
        return "ADMIN".equalsIgnoreCase(role) || "SUPER_ADMIN".equalsIgnoreCase(role);
    }

    /**
     * 校验资源操作权限
     * 检查当前用户是否有权操作指定资源
     *
     * @param ownerId 资源所有者ID
     * @param operation 操作类型（删除、修改等）
     * @throws BusinessException 如果无权限
     */
    public static void checkOwnership(Long ownerId, String operation) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new com.ruoyi.im.exception.BusinessException(401, "未登录");
        }
        // 所有者本人可操作
        if (currentUserId.equals(ownerId)) {
            return;
        }
        // 管理员可操作
        if (isAdmin()) {
            return;
        }
        throw new com.ruoyi.im.exception.BusinessException(403, "无权限" + operation + "该资源");
    }

    /**
     * 校验资源操作权限（批量）
     * 检查当前用户是否有权操作指定资源列表
     *
     * @param ownerIds 资源所有者ID列表
     * @param operation 操作类型
     * @throws BusinessException 如果无权限
     */
    public static void checkOwnership(Collection<Long> ownerIds, String operation) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new com.ruoyi.im.exception.BusinessException(401, "未登录");
        }
        // 管理员不受限制
        if (isAdmin()) {
            return;
        }
        // 检查是否所有资源都属于当前用户
        for (Long ownerId : ownerIds) {
            if (!currentUserId.equals(ownerId)) {
                throw new com.ruoyi.im.exception.BusinessException(403, "无权限" + operation + "部分资源");
            }
        }
    }
}
