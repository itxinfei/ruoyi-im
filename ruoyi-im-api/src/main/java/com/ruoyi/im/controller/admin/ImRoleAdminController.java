package com.ruoyi.im.controller.admin;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.role.ImRoleCreateRequest;
import com.ruoyi.im.dto.role.ImRoleUpdateRequest;
import com.ruoyi.im.service.ImRoleService;
import com.ruoyi.im.vo.role.ImPermissionVO;
import com.ruoyi.im.vo.role.ImRoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 角色权限管理控制器
 * 提供角色管理、权限分配等功能
 *
 * @author ruoyi
 */
@Tag(name = "角色权限管理", description = "角色权限管理接口")
@RestController
@RequestMapping("/api/admin/roles")
@Validated
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
public class ImRoleAdminController {

    private final ImRoleService imRoleService;

    public ImRoleAdminController(ImRoleService imRoleService) {
        this.imRoleService = imRoleService;
    }

    /**
     * 获取角色列表（分页）
     *
     * @param params 查询参数
     * @return 分页结果
     */
    @Operation(summary = "获取角色列表", description = "分页查询角色列表，支持关键词搜索")
    @GetMapping
    public Result<Map<String, Object>> getRoleList(@RequestParam Map<String, Object> params) {
        Map<String, Object> result = imRoleService.getRoleList(params);
        return Result.success(result);
    }

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色详情
     */
    @Operation(summary = "获取角色详情", description = "根据ID获取角色详细信息，包括权限列表")
    @GetMapping("/{id}")
    public Result<ImRoleVO> getRoleDetail(@Parameter(description = "角色ID") @PathVariable Long id) {
        ImRoleVO roleVO = imRoleService.getRoleDetail(id);
        return Result.success(roleVO);
    }

    /**
     * 新增角色
     *
     * @param request 创建请求
     * @return 角色ID
     */
    @Operation(summary = "新增角色", description = "创建新的自定义角色")
    @PostMapping
    @PreAuthorize("hasAuthority('system:role:add')")
    public Result<Long> createRole(@Valid @RequestBody ImRoleCreateRequest request) {
        Long roleId = imRoleService.createRole(request);
        return Result.success("创建成功", roleId);
    }

    /**
     * 更新角色
     *
     * @param request 更新请求
     * @return 操作结果
     */
    @Operation(summary = "更新角色", description = "更新角色信息")
    @PutMapping
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<Void> updateRole(@Valid @RequestBody ImRoleUpdateRequest request) {
        imRoleService.updateRole(request);
        return Result.success();
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 操作结果
     */
    @Operation(summary = "删除角色", description = "删除指定角色（系统内置角色不可删除）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:remove')")
    public Result<Void> deleteRole(@Parameter(description = "角色ID") @PathVariable Long id) {
        imRoleService.deleteRole(id);
        return Result.success();
    }

    /**
     * 获取权限列表（树形结构）
     *
     * @return 权限树
     */
    @Operation(summary = "获取权限列表", description = "获取所有权限的树形结构")
    @GetMapping("/permissions")
    public Result<List<ImPermissionVO>> getPermissionList() {
        List<ImPermissionVO> permissionTree = imRoleService.getPermissionTree();
        return Result.success(permissionTree);
    }

    /**
     * 分配角色权限
     *
     * @param id            角色ID
     * @param permissionIds 权限ID列表
     * @return 操作结果
     */
    @Operation(summary = "分配角色权限", description = "为角色分配权限")
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('system:role:assignPermission')")
    public Result<Void> assignRolePermissions(
            @Parameter(description = "角色ID") @PathVariable Long id,
            @RequestBody List<Long> permissionIds) {
        imRoleService.assignRolePermissions(id, permissionIds);
        return Result.success();
    }

    /**
     * 获取角色成员列表
     *
     * @param id 角色ID
     * @return 用户ID列表
     */
    @Operation(summary = "获取角色成员", description = "获取指定角色的成员列表")
    @GetMapping("/{id}/members")
    public Result<List<Long>> getRoleMembers(@Parameter(description = "角色ID") @PathVariable Long id) {
        List<Long> userIds = imRoleService.getRoleMemberIds(id);
        return Result.success(userIds);
    }

    /**
     * 添加成员到角色
     *
     * @param id     角色ID
     * @param userIds 用户ID列表
     * @return 操作结果
     */
    @Operation(summary = "添加角色成员", description = "将用户添加到角色")
    @PostMapping("/{id}/members")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<Void> addRoleMembers(
            @Parameter(description = "角色ID") @PathVariable Long id,
            @RequestBody List<Long> userIds) {
        imRoleService.addRoleMembers(id, userIds);
        return Result.success();
    }

    /**
     * 移除角色成员
     *
     * @param id     角色ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @Operation(summary = "移除角色成员", description = "从角色中移除用户")
    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<Void> removeRoleMember(
            @Parameter(description = "角色ID") @PathVariable Long id,
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        imRoleService.removeRoleMember(id, userId);
        return Result.success();
    }
}
