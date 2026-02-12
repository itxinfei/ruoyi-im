package com.ruoyi.im.aspect;

import com.ruoyi.im.annotation.RequireEmailPermission;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 邮件权限验证切面
 * 在执行邮件相关操作前验证用户权限
 * 
 * @author ruoyi
 */
@Aspect
@Component
@Slf4j
public class EmailPermissionAspect {
    
    /**
     * 邮件权限验证前置通知
     * 
     * @param joinPoint 连接点
     * @param permission 权限验证注解
     */
    @Before("@annotation(permission)")
    public void checkEmailPermission(JoinPoint joinPoint, RequireEmailPermission permission) {
        try {
            // 获取当前用户ID
            Long currentUserId = getCurrentUserId(permission);
            if (currentUserId == null) {
                throw new BusinessException("用户未登录");
            }
            
            // 获取邮件ID
            Long emailId = getEmailId(joinPoint, permission);
            if (emailId == null) {
                throw new BusinessException("邮件ID不能为空");
            }
            
            // 获取目标用户ID（邮件所有者）
            Long targetUserId = getTargetUserId(joinPoint, permission);
            
            // 验证权限
            validatePermission(currentUserId, targetUserId, emailId, permission.value());
            
            log.debug("邮件权限验证通过: 用户ID={}, 邮件ID={}, 权限类型={}", 
                currentUserId, emailId, permission.value());
            
        } catch (BusinessException e) {
            log.warn("邮件权限验证失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("邮件权限验证异常", e);
            throw new BusinessException("权限验证异常");
        }
    }
    
    /**
     * 获取当前用户ID
     * 
     * @param permission 权限验证注解
     * @return 用户ID
     */
    private Long getCurrentUserId(RequireEmailPermission permission) {
        if (permission.userIdFromHeader()) {
            // 从请求头中获取用户ID
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String userIdStr = request.getHeader("X-User-Id");
                if (userIdStr != null) {
                    try {
                        return Long.parseLong(userIdStr);
                    } catch (NumberFormatException e) {
                        log.warn("无效的用户ID: {}", userIdStr);
                    }
                }
            }
        }
        
        // 从SecurityUtils获取当前用户ID
        return SecurityUtils.getCurrentUserId();
    }
    
    /**
     * 从方法参数中获取邮件ID
     * 
     * @param joinPoint 连接点
     * @param permission 权限验证注解
     * @return 邮件ID
     */
    private Long getEmailId(JoinPoint joinPoint, RequireEmailPermission permission) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String[] paramNames = signature.getParameterNames();
        Object[] paramValues = joinPoint.getArgs();
        
        String emailIdParam = permission.emailIdParam();
        
        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals(emailIdParam)) {
                Object value = paramValues[i];
                if (value instanceof Long) {
                    return (Long) value;
                } else if (value instanceof Integer) {
                    return ((Integer) value).longValue();
                } else if (value instanceof String) {
                    try {
                        return Long.parseLong((String) value);
                    } catch (NumberFormatException e) {
                        log.warn("无效的邮件ID: {}", value);
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * 从方法参数中获取目标用户ID
     * 
     * @param joinPoint 连接点
     * @param permission 权限验证注解
     * @return 用户ID
     */
    private Long getTargetUserId(JoinPoint joinPoint, RequireEmailPermission permission) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] paramValues = joinPoint.getArgs();
        
        String userIdParam = permission.userIdParam();
        
        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals(userIdParam)) {
                Object value = paramValues[i];
                if (value instanceof Long) {
                    return (Long) value;
                } else if (value instanceof Integer) {
                    return ((Integer) value).longValue();
                } else if (value instanceof String) {
                    try {
                        return Long.parseLong((String) value);
                    } catch (NumberFormatException e) {
                        log.warn("无效的用户ID: {}", value);
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * 验证权限
     * 
     * @param currentUserId 当前用户ID
     * @param targetUserId 目标用户ID
     * @param emailId 邮件ID
     * @param permissionType 权限类型
     */
    private void validatePermission(Long currentUserId, Long targetUserId, 
                                   Long emailId, RequireEmailPermission.PermissionType permissionType) {
        
        // 如果目标用户ID为空，则只验证用户是否登录
        if (targetUserId == null) {
            return;
        }
        
        // 验证用户是否是邮件所有者
        if (!currentUserId.equals(targetUserId)) {
            // 检查是否是管理员（可选）
            if (!isAdmin(currentUserId)) {
                throw new BusinessException("您没有权限访问此邮件");
            }
        }
        
        // 根据权限类型进行额外验证
        switch (permissionType) {
            case READ:
                // 读取权限：用户可以查看自己的邮件
                break;
            case WRITE:
                // 修改权限：用户可以修改自己的邮件
                break;
            case DELETE:
                // 删除权限：用户可以删除自己的邮件
                break;
            case ADMIN:
                // 管理权限：只有管理员可以执行
                if (!isAdmin(currentUserId)) {
                    throw new BusinessException("您没有管理员权限");
                }
                break;
            default:
                throw new BusinessException("未知的权限类型");
        }
    }
    
    /**
     * 检查用户是否是管理员
     * 
     * @param userId 用户ID
     * @return 是否是管理员
     */
    private boolean isAdmin(Long userId) {
        // TODO: 实现管理员检查逻辑
        // 可以从数据库查询用户角色，或从缓存中获取
        return false;
    }
}
