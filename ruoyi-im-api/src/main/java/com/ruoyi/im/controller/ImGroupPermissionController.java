package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImGroupPermissionService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.group.GroupPermissionVO;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 群组权限控制器
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/group")
public class ImGroupPermissionController {

    private final ImGroupPermissionService groupPermissionService;

    public ImGroupPermissionController(ImGroupPermissionService groupPermissionService) {
        this.groupPermissionService = groupPermissionService;
    }

    /**
     * 获取群组权限配置
     *
     * @param groupId 群组ID
     * @return 权限配置列表
     */
    @GetMapping("/{groupId}/permissions")
    public Result<List<GroupPermissionVO>> getGroupPermissions(@PathVariable Long groupId) {
        List<GroupPermissionVO> permissions = groupPermissionService.getGroupPermissions(groupId);
        return Result.success(permissions);
    }

    /**
     * 更新群组角色权限
     * 仅群主可以调用
     *
     * @param groupId 群组ID
     * @param role 角色
     * @param permissions 权限配置
     * @return 操作结果
     */
    @PutMapping("/{groupId}/permissions/{role}")
    public Result<Void> updatePermission(
            @PathVariable Long groupId,
            @PathVariable String role,
            @RequestBody Map<String, Integer> permissions) {
        Long userId = SecurityUtils.getLoginUserId();
        groupPermissionService.updatePermission(groupId, role, permissions, userId);
        return Result.success("权限更新成功");
    }

    /**
     * 重置群组权限为默认配置
     * 仅群主可以调用
     *
     * @param groupId 群组ID
     * @return 操作结果
     */
    @PostMapping("/{groupId}/permissions/reset")
    public Result<Void> resetPermissions(@PathVariable Long groupId) {
        Long userId = SecurityUtils.getLoginUserId();
        // 验证操作者是群主
        if (!groupPermissionService.isOwner(groupId, userId)) {
            return Result.fail("只有群主可以重置权限配置");
        }
        groupPermissionService.resetToDefault(groupId);
        return Result.success("权限已重置为默认配置");
    }

    /**
     * 检查当前用户是否有指定权限
     *
     * @param groupId 群组ID
     * @param permission 权限名称
     * @return 权限检查结果
     */
    @GetMapping("/{groupId}/permission/check")
    public Result<Map<String, Object>> checkPermission(
            @PathVariable Long groupId,
            @RequestParam String permission) {
        Long userId = SecurityUtils.getLoginUserId();
        boolean hasPermission = groupPermissionService.hasPermission(groupId, userId, permission);

        Map<String, Object> result = new HashMap<>();
        result.put("hasPermission", hasPermission);
        result.put("permission", permission);
        return Result.success(result);
    }

    /**
     * 获取当前用户在群组中的角色
     *
     * @param groupId 群组ID
     * @return 用户角色信息
     */
    @GetMapping("/{groupId}/role")
    public Result<Map<String, Object>> getUserRole(@PathVariable Long groupId) {
        Long userId = SecurityUtils.getLoginUserId();

        Map<String, Object> result = new HashMap<>();
        result.put("isOwner", groupPermissionService.isOwner(groupId, userId));
        result.put("isAdminOrOwner", groupPermissionService.isAdminOrOwner(groupId, userId));

        return Result.success(result);
    }
}
