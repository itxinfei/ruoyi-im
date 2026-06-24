package com.ruoyi.im.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.dto.group.ImGroupUpdateRequest;
import com.ruoyi.im.service.ImGroupService;
import com.ruoyi.im.vo.admin.BatchOperationResult;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员 - 群组管理控制器
 * 提供群组管理、解散、成员管理等管理员功能
 *
 * @author ruoyi
 */

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
    
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {

        Page<ImGroup> page = new Page<>(pageNum, pageSize);
        IPage<ImGroup> groupPage = imGroupService.getGroupPage(page, keyword);

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
     * @param id 群组 ID
     * @return 群组详情
     */
    
    @GetMapping("/{id}")
    public Result<ImGroup> getById(@PathVariable Long id) {
        ImGroup group = imGroupService.getGroupByIdForAdmin(id);
        if (group == null) {
            return Result.fail("群组不存在");
        }
        return Result.success(group);
    }

    /**
     * 解散群组
     *
     * @param id 群组 ID
     * @return 操作结果
     */
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ImGroup group = imGroupService.getGroupByIdForAdmin(id);
        if (group == null) {
            return Result.fail("群组不存在");
        }
        imGroupService.dissolveGroupForAdmin(id);
        return Result.success("群组已解散");
    }

    /**
     * 批量解散群组
     *
     * @param ids 群组 ID 列表
     * @return 批量操作结果
     */
    
    @DeleteMapping("/batch")
    public Result<BatchOperationResult> batchDelete(@RequestBody List<Long> ids) {
        BatchOperationResult result = new BatchOperationResult();

        for (Long id : ids) {
            ImGroup group = imGroupService.getGroupByIdForAdmin(id);
            if (group == null) {
                result.addFailedItem(id, "群组不存在");
            } else {
                imGroupService.dissolveGroupForAdmin(id);
                result.setSuccessCount(result.getSuccessCount() + 1);
            }
        }

        return Result.success(result);
    }

    /**
     * 更新群组信息
     *
     * @param id 群组 ID
     * @param request 群组更新请求
     * @return 操作结果
     */
    
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ImGroupUpdateRequest request) {
        ImGroup existGroup = imGroupService.getGroupByIdForAdmin(id);
        if (existGroup == null) {
            return Result.fail("群组不存在");
        }

        BeanUtils.copyProperties(request, existGroup);
        existGroup.setId(id);
        existGroup.setUpdateTime(LocalDateTime.now());
        imGroupService.updateGroupForAdmin(existGroup);

        return Result.success("更新成功");
    }

    /**
     * 获取群组统计
     *
     * @return 统计数据
     */
    
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        long total = imGroupService.countActiveGroups();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);

        return Result.success(stats);
    }

    /**
     * 获取群组成员列表
     *
     * @param id 群组 ID
     * @return 成员列表
     */
    
    @GetMapping("/{id}/members")
    public Result<List<Map<String, Object>>> getMembers(@PathVariable Long id) {
        List<Map<String, Object>> members = imGroupService.getGroupMembersForAdmin(id);
        return Result.success(members);
    }

    /**
     * 移除群组成员
     *
     * @param groupId 群组 ID
     * @param userId 用户 ID
     * @return 操作结果
     */
    
    @DeleteMapping("/{groupId}/members/{userId}")
    public Result<Void> removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
        imGroupService.removeGroupMemberForAdmin(groupId, userId);
        return Result.success("成员已移除");
    }
}
