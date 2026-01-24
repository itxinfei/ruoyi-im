package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImGroupService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.group.ImGroupMemberVO;
import com.ruoyi.im.vo.group.ImGroupVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 群组控制器
 * 提供群组列表、群组详情、成员管理等功能
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/groups")
public class ImGroupsController {

    private static final Logger log = LoggerFactory.getLogger(ImGroupsController.class);

    @Autowired
    private ImGroupService imGroupService;

    @GetMapping("/list")
    public Result<List<ImGroupVO>> list() {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImGroupVO> groups = imGroupService.getUserGroups(userId);
        return Result.success(groups);
    }

    @GetMapping("/{id}")
    public Result<ImGroupVO> getById(@PathVariable Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        ImGroupVO vo = imGroupService.getGroupById(id, userId);
        return Result.success(vo);
    }

    @GetMapping("/{id}/members")
    public Result<List<ImGroupMemberVO>> getMembers(@PathVariable Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImGroupMemberVO> list = imGroupService.getGroupMembers(id, userId);
        return Result.success(list);
    }

    /**
     * 添加群组成员
     *
     * @param id     群组ID
     * @param userIds 要添加的用户ID列表
     * @return 添加结果
     */
    @PostMapping("/{id}/members")
    public Result<Void> addMembers(
            @PathVariable Long id,
            @RequestBody List<Long> userIds) {
        Long userId = SecurityUtils.getLoginUserId();
        imGroupService.addMembers(id, userIds, userId);
        return Result.success("添加成功");
    }

    /**
     * 移除群组成员
     *
     * @param id       群组ID
     * @param memberId 要移除的成员ID
     * @return 移除结果
     */
    @DeleteMapping("/{id}/members/{memberId}")
    public Result<Void> removeMember(@PathVariable Long id,
                                      @PathVariable Long memberId) {
        Long userId = SecurityUtils.getLoginUserId();
        imGroupService.removeMembers(id, java.util.Collections.singletonList(memberId), userId);
        return Result.success("移除成功");
    }

    /**
     * 更新成员角色
     *
     * @param id       群组ID
     * @param memberId 成员ID
     * @param request  角色更新请求
     * @return 更新结果
     */
    @PutMapping("/{id}/members/{memberId}/role")
    public Result<Void> updateMemberRole(
            @PathVariable Long id,
            @PathVariable Long memberId,
            @RequestBody RoleUpdateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        boolean isAdmin = "admin".equalsIgnoreCase(request.getRole());
        imGroupService.setAdmin(id, memberId, isAdmin, userId);
        return Result.success("角色更新成功");
    }

    /**
     * 角色更新请求DTO
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
