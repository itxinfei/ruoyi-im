package com.ruoyi.im.aspect;

import com.ruoyi.im.annotation.RequirePermission;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.IUserService;
import com.ruoyi.im.utils.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * 权限控制切面
 * 
 * @author ruoyi
 */
@Aspect
@Component
public class PermissionAspect {
    
    private static final Logger log = LoggerFactory.getLogger(PermissionAspect.class);

    @Autowired
    private IUserService userService;

    @Around("@annotation(requirePermission)")
    public Object around(ProceedingJoinPoint point, RequirePermission requirePermission) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        
        log.debug("开始权限检查: method={}, permission={}", method.getName(), requirePermission.value());
        
        // 获取当前用户
        ImUser currentUser = getCurrentUser();
        if (currentUser == null) {
            log.warn("权限检查失败: 当前用户未登录");
            throw new BusinessException(401, "用户未登录");
        }
        
        // 检查权限
        boolean hasPermission = checkPermission(currentUser, requirePermission);
        if (!hasPermission) {
            log.warn("权限检查失败: userId={}, method={}, permission={}", 
                    currentUser.getId(), method.getName(), requirePermission.value());
            throw new BusinessException(403, "权限不足: " + requirePermission.desc());
        }
        
        log.debug("权限检查通过: userId={}, method={}, permission={}", 
                currentUser.getId(), method.getName(), requirePermission.value());
        
        return point.proceed();
    }

    /**
     * 获取当前用户
     */
    private ImUser getCurrentUser() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = SecurityUtils.getUsernameFromToken(token);
                if (username != null) {
                    return userService.findByUsername(username);
                }
            }
        } catch (Exception e) {
            log.error("获取当前用户失败", e);
        }
        return null;
    }

    /**
     * 检查用户权限
     */
    private boolean checkPermission(ImUser user, RequirePermission requirePermission) {
        String permission = requirePermission.value();
        if (permission.isEmpty()) {
            return true;
        }
        
        // 获取用户所有权限
        Set<String> userPermissions = userService.getUserPermissions(user.getId());
        
        // 检查单个权限
        if (!requirePermission.allRequired()) {
            return userPermissions.contains(permission);
        }
        
        // 检查多个权限（AND逻辑）
        String[] permissions = permission.split(",");
        for (String perm : permissions) {
            if (!userPermissions.contains(perm.trim())) {
                return false;
            }
        }
        return true;
    }
}