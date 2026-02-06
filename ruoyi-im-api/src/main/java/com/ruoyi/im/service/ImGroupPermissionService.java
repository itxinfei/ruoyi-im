package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImGroupPermission;
import com.ruoyi.im.vo.group.GroupPermissionVO;

import java.util.List;
import java.util.Map;

/**
 * 群组权限服务接口
 *
 * @author ruoyi
 */
public interface ImGroupPermissionService {

    /**
     * 初始化群组权限配置
     * 创建群组时调用，设置默认权限
     *
     * @param groupId 群组ID
     */
    void initGroupPermissions(Long groupId);

    /**
     * 获取群组所有角色的权限配置
     *
     * @param groupId 群组ID
     * @return 权限配置列表
     */
    List<GroupPermissionVO> getGroupPermissions(Long groupId);

    /**
     * 获取指定角色的权限配置
     *
     * @param groupId 群组ID
     * @param role 角色
     * @return 权限配置
     */
    ImGroupPermission getPermission(Long groupId, String role);

    /**
     * 更新角色权限配置
     * 仅群主可以调用
     *
     * @param groupId 群组ID
     * @param role 角色
     * @param permissions 权限配置
     * @param operatorId 操作者ID
     */
    void updatePermission(Long groupId, String role, Map<String, Integer> permissions, Long operatorId);

    /**
     * 检查用户是否有指定权限
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param permission 权限名称（can_invite, can_kick等）
     * @return 是否有权限
     */
    boolean hasPermission(Long groupId, Long userId, String permission);

    /**
     * 检查用户是否是群主
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 是否是群主
     */
    boolean isOwner(Long groupId, Long userId);

    /**
     * 检查用户是否是管理员或群主
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 是否是管理员或群主
     */
    boolean isAdminOrOwner(Long groupId, Long userId);

    /**
     * 重置群组权限为默认配置
     *
     * @param groupId 群组ID
     */
    void resetToDefault(Long groupId);
}
