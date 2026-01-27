package com.ruoyi.im.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.dto.group.ImGroupUpdateRequest;
import com.ruoyi.im.service.ImGroupService;
import com.ruoyi.im.vo.group.ImGroupVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员-群组管理控制器
 * 提供群组管理、解散、成员管理等管理员功能
 *
 * @author ruoyi
 */
@Tag(name = "管理员-群组管理", description = "管理员群组管理接口")
@RestController
@RequestMapping("/api/admin/groups")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
public class ImGroupAdminController {

    private final ImGroupService imGroupService;

    /**
     * 构造器注入依赖
     *
     * @param imGroupService 群组服务
     */
    public ImGroupAdminController(ImGroupService imGroupService) {
        this.imGroupService = imGroupService;
    }

    /**
     * 获取群组列表（分页）
     *
     * @param keyword 搜索关键词（群名称/群号）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 群组列表
     */
    @Operation(summary = "获取群组列表", description = "管理员获取群组列表，支持分页和关键词搜索")
    @GetMapping
    public Result<Map<String, Object>> list(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(required = false, defaultValue = "20") Integer pageSize) {

        // 构建分页
        Page<ImGroup> page = new Page<>(pageNum, pageSize);

        // 查询群组列表
        IPage<ImGroup> groupPage = imGroupService.getGroupPage(page, keyword);

        // 返回结果
        Map<String, Object> data = new HashMap<>();
        data.put("list", groupPage.getRecords());
        data.put("total", groupPage.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        data.put("pages", groupPage.getPages());

        return Result.success(data);
    }

    /**
     * 获取群组详情
     *
     * @param id 群组ID
     * @return 群组详情
     */
    @Operation(summary = "获取群组详情", description = "管理员获取指定群组的详细信息")
    @GetMapping("/{id}")
    public Result<ImGroupVO> getById(@Parameter(description = "群组ID") @PathVariable Long id) {
        ImGroupVO group = imGroupService.adminGetGroupById(id);
        if (group == null) {
            return Result.fail("群组不存在");
        }
        return Result.success(group);
    }

    /**
     * 解散群组
     *
     * @param id 群组ID
     * @return 操作结果
     */
    @Operation(summary = "解散群组", description = "管理员解散指定群组")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "群组ID") @PathVariable Long id) {
        imGroupService.adminDismissGroup(id);
        return Result.success("群组已解散");
    }

    /**
     * 批量解散群组
     *
     * @param ids 群组ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量解散群组", description = "管理员批量解散群组")
    @DeleteMapping("/batch")
    public Result<Map<String, Integer>> batchDelete(
            @Parameter(description = "群组ID列表") @RequestBody List<Long> ids) {
        Map<String, Integer> result = imGroupService.adminBatchDismissGroups(ids);
        return Result.success(result);
    }

    /**
     * 更新群组信息
     *
     * @param id 群组ID
     * @param request 群组更新请求
     * @return 操作结果
     */
    @Operation(summary = "更新群组信息", description = "管理员更新群组信息")
    @PutMapping("/{id}")
    public Result<Void> update(
            @Parameter(description = "群组ID") @PathVariable Long id,
            @RequestBody ImGroupUpdateRequest request) {
        imGroupService.adminUpdateGroup(id, request);
        return Result.success("更新成功");
    }

    /**
     * 获取群组统计
     *
     * @return 统计数据
     */
    @Operation(summary = "获取群组统计", description = "获取群组总数、活跃群组等统计信息")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = imGroupService.getGroupStats();
        return Result.success(stats);
    }

    /**
     * 获取群组成员列表
     *
     * @param id 群组ID
     * @return 成员列表
     */
    @Operation(summary = "获取群组成员列表", description = "管理员获取群组成员列表")
    @GetMapping("/{id}/members")
    public Result<List<Map<String, Object>>> getMembers(@Parameter(description = "群组ID") @PathVariable Long id) {
        List<Map<String, Object>> members = imGroupService.adminGetGroupMembers(id);
        return Result.success(members);
    }

    /**
     * 移除群组成员
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @Operation(summary = "移除群组成员", description = "管理员移除群组成员")
    @DeleteMapping("/{groupId}/members/{userId}")
    public Result<Void> removeMember(
            @Parameter(description = "群组ID") @PathVariable Long groupId,
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        imGroupService.adminRemoveMember(groupId, userId);
        return Result.success("成员已移除");
    }
}
