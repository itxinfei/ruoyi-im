package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImGroupService;
import com.ruoyi.im.vo.group.ImGroupMemberVO;
import com.ruoyi.im.vo.group.ImGroupVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 群组控制器（复数路径版本）
 * 处理 /api/im/groups 路径请求，与 /api/im/group 保持一致
 * 此控制器为前端API兼容而创建，委托给ImGroupService处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/groups")
public class ImGroupsController {

    @Autowired
    private ImGroupService imGroupService;

    /**
     * 获取群组详情
     */
    @GetMapping("/{id}")
    public Result<ImGroupVO> getById(@PathVariable Long id,
                                     @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        ImGroupVO vo = imGroupService.getGroupById(id, userId);
        return Result.success(vo);
    }

    /**
     * 获取群组成员列表
     */
    @GetMapping("/{id}/members")
    public Result<List<ImGroupMemberVO>> getMembers(@PathVariable Long id,
                                                     @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        List<ImGroupMemberVO> list = imGroupService.getGroupMembers(id, userId);
        return Result.success(list);
    }

    /**
     * 添加群组成员
     */
    @PostMapping("/{id}/members")
    public Result<Void> addMembers(@PathVariable Long id,
                                   @RequestBody List<Long> userIds,
                                   @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        imGroupService.addMembers(id, userIds, userId);
        return Result.success("添加成功");
    }

    /**
     * 删除单个群组成员
     * 与ImGroupController的批量删除不同，此接口删除单个成员
     */
    @DeleteMapping("/{id}/members/{memberId}")
    public Result<Void> removeMember(@PathVariable Long id,
                                     @PathVariable Long memberId,
                                     @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        imGroupService.removeMembers(id, List.of(memberId), userId);
        return Result.success("移除成功");
    }

    /**
     * 更新成员角色（设置/取消管理员）
     */
    @PutMapping("/{id}/members/{memberId}/role")
    public Result<Void> updateMemberRole(@PathVariable Long id,
                                         @PathVariable Long memberId,
                                         @RequestBody RoleUpdateRequest request,
                                         @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        // role: "admin" 或 "member"
        boolean isAdmin = "admin".equalsIgnoreCase(request.getRole());
        imGroupService.setAdmin(id, memberId, isAdmin, userId);
        return Result.success("角色更新成功");
    }

    /**
     * 角色更新请求体
     */
    public static class RoleUpdateRequest {
        private String role;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
