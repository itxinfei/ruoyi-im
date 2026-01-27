package com.ruoyi.im.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.constant.SystemConstants;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.dto.group.ImGroupUpdateRequest;
import com.ruoyi.im.mapper.ImGroupMapper;
import com.ruoyi.im.mapper.ImGroupMemberMapper;
import com.ruoyi.im.service.ImGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
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
    private final ImGroupMapper imGroupMapper;
    private final ImGroupMemberMapper imGroupMemberMapper;

    /**
     * 构造器注入依赖
     *
     * @param imGroupService 群组服务
     * @param imGroupMapper 群组Mapper
     * @param imGroupMemberMapper 群组成员Mapper
     */
    public ImGroupAdminController(ImGroupService imGroupService,
                                    ImGroupMapper imGroupMapper,
                                    ImGroupMemberMapper imGroupMemberMapper) {
        this.imGroupService = imGroupService;
        this.imGroupMapper = imGroupMapper;
        this.imGroupMemberMapper = imGroupMemberMapper;
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
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {

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
    public Result<ImGroup> getById(@PathVariable Long id) {
        ImGroup group = imGroupMapper.selectImGroupById(id);
        if (group == null) {
            return Result.fail("群组不存在");
        }

        // 设置成员数量
        Integer memberCount = imGroupMemberMapper.countMembersByGroupId(id);
        group.setMemberCount(memberCount);

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
    public Result<Void> delete(@PathVariable Long id) {
        ImGroup group = imGroupMapper.selectImGroupById(id);
        if (group == null) {
            return Result.fail("群组不存在");
        }

        // 软删除群组
        group.setIsDeleted(SystemConstants.DELETED);
        group.setDeletedTime(LocalDateTime.now());
        imGroupMapper.updateImGroup(group);

        // 删除群组成员关系
        List<Long> groupIds = java.util.Collections.singletonList(id);
        imGroupMemberMapper.deleteByGroupIds(groupIds);

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
    public Result<Map<String, Object>> batchDelete(@RequestBody List<Long> ids) {
        int successCount = 0;
        int failCount = 0;

        for (Long id : ids) {
            ImGroup group = imGroupMapper.selectImGroupById(id);
            if (group != null) {
                group.setIsDeleted(SystemConstants.DELETED);
                group.setDeletedTime(LocalDateTime.now());
                imGroupMapper.updateImGroup(group);
                successCount++;
            } else {
                failCount++;
            }
        }

        // 删除群组成员关系
        if (!ids.isEmpty()) {
            imGroupMemberMapper.deleteByGroupIds(ids);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);

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
    public Result<Void> update(@PathVariable Long id, @RequestBody ImGroupUpdateRequest request) {
        ImGroup existGroup = imGroupMapper.selectImGroupById(id);
        if (existGroup == null) {
            return Result.fail("群组不存在");
        }

        // 将DTO属性复制到Entity
        BeanUtils.copyProperties(request, existGroup);
        existGroup.setId(id);
        existGroup.setUpdateTime(LocalDateTime.now());
        imGroupMapper.updateImGroup(existGroup);

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
        // 查询所有群组
        List<ImGroup> allGroups = imGroupMapper.selectImGroupList(new ImGroup());

        // 统计未删除的群组
        long total = allGroups.stream()
                .filter(g -> g.getIsDeleted() == null || g.getIsDeleted() == SystemConstants.NOT_DELETED)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);

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
    public Result<List<Map<String, Object>>> getMembers(@PathVariable Long id) {
        List<Map<String, Object>> members = imGroupMemberMapper.selectMembersByGroupId(id);

        // 转换为适合前端显示的格式
        for (Map<String, Object> member : members) {
            String role = (String) member.get("role");
            if ("OWNER".equals(role)) {
                member.put("roleDisplay", "群主");
            } else if ("ADMIN".equals(role)) {
                member.put("roleDisplay", "管理员");
            } else {
                member.put("roleDisplay", "成员");
            }
        }

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
    public Result<Void> removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
        imGroupMemberMapper.deleteByGroupIdAndUserId(groupId, userId);
        return Result.success("成员已移除");
    }
}
