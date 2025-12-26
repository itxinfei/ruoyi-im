package com.ruoyi.framework.web.service;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Service;

/**
 * 安全服务 - 为@PreAuthorize注解提供权限检查
 *
 * @author ruoyi
 */
@Service("ss")
public class SecurityService
{
    /**
     * 验证用户是否具备某权限
     *
     * @param permission 权限字符串
     * @return 用户是否具备某权限
     */
    public boolean hasPermi(String permission)
    {
        Subject subject = SecurityUtils.getSubject();
        if (subject == null || subject.getPrincipal() == null)
        {
            return false;
        }
        // 超级管理员拥有所有权限
        if ("*:*:*".equals(permission))
        {
            return true;
        }
        return subject.isPermitted(permission);
    }

    /**
     * 验证用户是否不具备某权限
     *
     * @param permission 权限字符串
     * @return 用户是否不具备某权限
     */
    public boolean lacksPermi(String permission)
    {
        return !hasPermi(permission);
    }

    /**
     * 验证用户是否具有以下任意一个权限
     *
     * @param permissions 权限列表（逗号分隔）
     * @return 用户是否具有以下任意一个权限
     */
    public boolean hasAnyPermi(String permissions)
    {
        Subject subject = SecurityUtils.getSubject();
        if (subject == null || subject.getPrincipal() == null)
        {
            return false;
        }
        for (String permission : permissions.split(","))
        {
            if (subject.isPermitted(permission.trim()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证用户是否具备某角色
     *
     * @param role 角色字符串
     * @return 用户是否具备某角色
     */
    public boolean hasRole(String role)
    {
        Subject subject = SecurityUtils.getSubject();
        if (subject == null || subject.getPrincipal() == null)
        {
            return false;
        }
        return subject.hasRole(role);
    }

    /**
     * 验证用户是否不具备某角色
     *
     * @param role 角色字符串
     * @return 用户是否不具备某角色
     */
    public boolean lacksRole(String role)
    {
        return !hasRole(role);
    }

    /**
     * 验证用户是否具有以下任意一个角色
     *
     * @param roles 角色列表（逗号分隔）
     * @return 用户是否具有以下任意一个角色
     */
    public boolean hasAnyRoles(String roles)
    {
        Subject subject = SecurityUtils.getSubject();
        if (subject == null || subject.getPrincipal() == null)
        {
            return false;
        }
        for (String role : roles.split(","))
        {
            if (subject.hasRole(role.trim()))
            {
                return true;
            }
        }
        return false;
    }
}
