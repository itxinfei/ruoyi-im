package com.ruoyi.im.controller.admin;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.constant.UserRole;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.vo.user.ImUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员-用户管理控制器
 * 提供用户管理、状态修改、角色管理等管理员功能
 *
 * @author ruoyi
 */
@Tag(name = "管理员-用户管理", description = "管理员用户管理接口")
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
@Validated
public class ImUserAdminController {

    private final ImUserService imUserService;

    /**
     * 构造器注入依赖
     *
     * @param imUserService 用户服务
     */
    public ImUserAdminController(ImUserService imUserService) {
        this.imUserService = imUserService;
    }

    /**
     * 获取用户列表（分页）
     *
     * @param keyword  搜索关键词
     * @param role     角色筛选
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 用户列表
     */
    @Operation(summary = "获取用户列表", description = "管理员获取用户列表，支持分页和关键词搜索")
    @GetMapping
    public Result<Map<String, Object>> list(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "角色筛选") @RequestParam(required = false) String role,
            @Parameter(description = "页码") @RequestParam(required = false, defaultValue = "1") @Min(value = 1, message = "页码最小为1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(required = false, defaultValue = "20") @Min(value = 1, message = "每页最少1条") @Max(value = 100, message = "每页最多100条") Integer pageSize) {

        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 分页查询
        List<ImUserVO> voList = imUserService.getUserListWithPagination(keyword, role, offset, pageSize);
        int total = imUserService.countUsers(keyword, role);

        // 返回结果
        Map<String, Object> data = new HashMap<>();
        data.put("list", voList);
        data.put("total", total);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        data.put("pages", (total + pageSize - 1) / pageSize);

        return Result.success(data);
    }

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @Operation(summary = "获取用户详情", description = "管理员获取指定用户的详细信息")
    @GetMapping("/{id}")
    public Result<ImUserVO> getById(@Parameter(description = "用户ID") @PathVariable @Positive(message = "用户ID必须为正数") Long id) {
        ImUserVO vo = imUserService.getUserById(id);
        return Result.success(vo);
    }

    /**
     * 修改用户状态
     *
     * @param id     用户ID
     * @param status 状态：0=禁用，1=启用
     * @return 操作结果
     */
    @Operation(summary = "修改用户状态", description = "管理员启用或禁用用户")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @Parameter(description = "用户ID") @PathVariable @Positive(message = "用户ID必须为正数") Long id,
            @Parameter(description = "状态：0=禁用，1=启用") @RequestParam Integer status) {
        imUserService.updateStatus(id, status);
        return Result.success("状态修改成功");
    }

    /**
     * 修改用户角色
     *
     * @param id   用户ID
     * @param role 角色：USER/ADMIN/SUPER_ADMIN
     * @return 操作结果
     */
    @Operation(summary = "修改用户角色", description = "超级管理员修改用户角色")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @PutMapping("/{id}/role")
    public Result<Void> updateRole(
            @Parameter(description = "用户ID") @PathVariable @Positive(message = "用户ID必须为正数") Long id,
            @Parameter(description = "角色：USER/ADMIN/SUPER_ADMIN") @RequestParam String role) {
        if (!UserRole.USER.equals(role) && !UserRole.ADMIN.equals(role) && !UserRole.SUPER_ADMIN.equals(role)) {
            return Result.fail("无效的角色");
        }
        imUserService.updateRole(id, role);
        return Result.success("角色修改成功");
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @Operation(summary = "删除用户", description = "管理员删除指定用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "用户ID") @PathVariable @Positive(message = "用户ID必须为正数") Long id) {
        imUserService.deleteUser(id);
        return Result.success("删除成功");
    }

    /**
     * 获取用户统计
     *
     * @return 统计数据
     */
    @Operation(summary = "获取用户统计", description = "获取用户总数、在线人数等统计信息")
    @GetMapping("/stats")
    public Result<Map<String, Long>> getStats() {
        Map<String, Long> stats = imUserService.getUserStats();
        return Result.success(stats);
    }

    /**
     * 创建用户
     *
     * @param data 用户数据
     * @return 操作结果
     */
    @Operation(summary = "创建用户", description = "管理员创建新用户")
    @PostMapping
    public Result<Long> create(@RequestBody Map<String, Object> data) {
        Long userId = imUserService.adminCreateUser(data);
        return Result.success("创建成功", userId);
    }

    /**
     * 更新用户信息
     *
     * @param id   用户ID
     * @param data 用户数据
     * @return 操作结果
     */
    @Operation(summary = "更新用户信息", description = "管理员更新用户信息")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable @Positive(message = "用户ID必须为正数") Long id, @RequestBody Map<String, Object> data) {
        imUserService.adminUpdateUser(id, data);
        return Result.success("更新成功");
    }

    /**
     * 批量删除用户
     *
     * @param ids 用户ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除用户", description = "管理员批量删除用户")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        imUserService.batchDeleteUsers(ids);
        return Result.success("批量删除成功");
    }

    /**
     * 批量更新用户状态
     *
     * @param data 包含ids和status
     * @return 操作结果
     */
    @Operation(summary = "批量更新用户状态", description = "管理员批量更新用户状态")
    @PutMapping("/batch/status")
    public Result<Void> batchUpdateStatus(@RequestBody Map<String, Object> data) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) data.get("ids");
        Integer status = (Integer) data.get("status");
        imUserService.batchUpdateUserStatus(ids, status);
        return Result.success("批量更新状态成功");
    }

    /**
     * 获取用户选项（用于下拉选择）
     *
     * @param keyword 搜索关键词
     * @return 用户选项列表
     */
    @Operation(summary = "获取用户选项", description = "获取用户选项列表，用于下拉选择")
    @GetMapping("/options")
    public Result<List<Map<String, Object>>> getOptions(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        List<Map<String, Object>> options = imUserService.getUserOptions(keyword);
        return Result.success(options);
    }

    /**
     * 重置用户密码
     *
     * @param id          用户ID
     * @param newPassword 新密码
     * @return 操作结果
     */
    @Operation(summary = "重置用户密码", description = "管理员重置指定用户的密码")
    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "新密码") @RequestParam String newPassword) {
        imUserService.resetPassword(id, newPassword);
        return Result.success("密码重置成功");
    }

    /**
     * 批量导入用户
     *
     * @param file Excel文件
     * @return 操作结果
     */
    @Operation(summary = "批量导入用户", description = "通过Excel文件批量导入用户数据")
    @PostMapping("/import")
    public Result<java.util.Map<String, Object>> importUsers(@RequestParam("file") MultipartFile file) {
        java.util.Map<String, Object> result = imUserService.importUsers(file);
        return Result.success(result);
    }

    /**
     * 下载导入模板
     *
     * @param response 响应
     */
    @Operation(summary = "下载导入模板", description = "下载用户批量导入的Excel模板")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        imUserService.downloadTemplate(response);
    }

    /**
     * 导出用户数据
     *
     * @param response 响应
     * @param keyword  搜索关键词
     * @param role     角色筛选
     */
    @Operation(summary = "导出用户数据", description = "将用户列表导出为Excel文件")
    @GetMapping("/export")
    public void exportUsers(HttpServletResponse response,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) throws IOException {
        imUserService.exportUsers(response, keyword, role);
    }
}
