package com.ruoyi.im.controller;

import com.ruoyi.im.dto.BasePageRequest;
import com.ruoyi.im.dto.user.ImRegisterRequest;
import com.ruoyi.im.dto.user.ImUserUpdateRequest;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.vo.user.ImUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 管理员用户管理控制器
 * 提供用户查询、管理、状态更新等功能
 *
 * @author ruoyi
 */
@Tag(name = "用户管理", description = "管理员用户管理接口")
@RestController
@RequestMapping("/api/admin/user")
public class ImAdminUserController {

    @Autowired
    private ImUserService imUserService;

    /**
     * 获取用户列表（分页）
     *
     * @param request 分页请求参数
     * @return 用户列表
     */
    @Operation(summary = "获取用户列表", description = "分页查询用户列表，支持按昵称、手机号搜索")
    @PostMapping("/list")
    public Result<List<ImUserVO>> list(@RequestBody BasePageRequest request) {
        List<ImUserVO> list = imUserService.getUserList(request);
        return Result.success(list);
    }

    /**
     * 获取用户总数
     *
     * @return 用户总数
     */
    @Operation(summary = "获取用户总数", description = "获取系统中的用户总数")
    @GetMapping("/total")
    public Result<Long> getTotalCount() {
        Long total = imUserService.getTotalUserCount();
        return Result.success(total);
    }

    /**
     * 创建用户
     *
     * @param request 创建请求参数
     * @return 新用户ID
     */
    @Operation(summary = "创建用户", description = "管理员创建新用户")
    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody ImRegisterRequest request) {
        Long userId = imUserService.createUser(request);
        return Result.success("创建成功", userId);
    }

    /**
     * 更新用户信息
     *
     * @param id 用户ID
     * @param request 更新请求参数
     * @return 更新结果
     */
    @Operation(summary = "更新用户信息", description = "管理员更新用户信息")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ImUserUpdateRequest request) {
        imUserService.updateUser(id, request);
        return Result.success("更新成功");
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 删除结果
     */
    @Operation(summary = "删除用户", description = "删除指定用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        imUserService.deleteUser(id);
        return Result.success("删除成功");
    }

    /**
     * 批量删除用户
     *
     * @param ids 用户ID列表
     * @return 删除结果
     */
    @Operation(summary = "批量删除用户", description = "批量删除多个用户")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        imUserService.batchDeleteUsers(ids);
        return Result.success("删除成功");
    }

    /**
     * 启用/禁用用户
     *
     * @param id 用户ID
     * @param status 状态：1=启用，0=禁用
     * @return 操作结果
     */
    @Operation(summary = "设置用户状态", description = "启用或禁用用户账号")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        imUserService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }

    /**
     * 重置用户密码
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @Operation(summary = "重置用户密码", description = "管理员重置用户密码为默认密码")
    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        imUserService.resetPassword(id);
        return Result.success("密码重置成功");
    }

    /**
     * 获取在线用户列表
     *
     * @return 在线用户列表
     */
    @Operation(summary = "获取在线用户", description = "获取当前在线的用户列表")
    @GetMapping("/online")
    public Result<List<ImUserVO>> getOnlineUsers() {
        List<ImUserVO> list = imUserService.getOnlineUsers();
        return Result.success(list);
    }
}
