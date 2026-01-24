package com.ruoyi.im.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.constant.UserRole;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.vo.user.ImUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
public class ImUserAdminController {

    @Autowired
    private ImUserService imUserService;

    /**
     * 获取用户列表（分页）
     *
     * @param keyword 搜索关键词
     * @param role    角色筛选
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 用户列表
     */
    @Operation(summary = "获取用户列表", description = "管理员获取用户列表，支持分页和关键词搜索")
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {

        // 构建查询条件
        LambdaQueryWrapper<ImUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(ImUser::getUsername, keyword)
                    .or().like(ImUser::getNickname, keyword)
                    .or().like(ImUser::getMobile, keyword));
        }
        if (role != null && !role.trim().isEmpty()) {
            wrapper.eq(ImUser::getRole, role);
        }
        wrapper.orderByDesc(ImUser::getCreateTime);

        // 分页查询
        Page<ImUser> page = new Page<>(pageNum, pageSize);
        Page<ImUser> result = imUserService.page(page, wrapper);

        // 转换为 VO
        List<ImUserVO> voList = result.getRecords().stream()
                .map(user -> {
                    ImUserVO vo = new ImUserVO();
                    BeanUtils.copyProperties(user, vo);
                    return vo;
                })
                .collect(java.util.stream.Collectors.toList());

        // 返回结果
        Map<String, Object> data = new HashMap<>();
        data.put("list", voList);
        data.put("total", result.getTotal());
        data.put("pageNum", result.getCurrent());
        data.put("pageSize", result.getSize());
        data.put("pages", result.getPages());

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
    public Result<ImUserVO> getById(@PathVariable Long id) {
        ImUser user = imUserService.getById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        ImUserVO vo = new ImUserVO();
        BeanUtils.copyProperties(user, vo);
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
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        ImUser user = imUserService.getById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setStatus(status);
        imUserService.updateById(user);
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
    public Result<Void> updateRole(@PathVariable Long id, @RequestParam String role) {
        if (!UserRole.USER.equals(role) && !UserRole.ADMIN.equals(role) && !UserRole.SUPER_ADMIN.equals(role)) {
            return Result.fail("无效的角色");
        }
        ImUser user = imUserService.getById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setRole(role);
        imUserService.updateById(user);
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
    public Result<Void> delete(@PathVariable Long id) {
        ImUser user = imUserService.getById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        imUserService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 获取用户统计
     *
     * @return 统计数据
     */
    @Operation(summary = "获取用户统计", description = "获取用户总数、在线人数等统计信息")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        long total = imUserService.count();
        long online = imUserService.count(new LambdaQueryWrapper<ImUser>()
                .eq(ImUser::getStatus, 1));

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("online", online);
        stats.put("offline", total - online);

        return Result.success(stats);
    }
}
