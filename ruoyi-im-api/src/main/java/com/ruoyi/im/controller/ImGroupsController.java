package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImGroupService;
import com.ruoyi.im.vo.group.ImGroupMemberVO;
import com.ruoyi.im.vo.group.ImGroupVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/im/groups")
public class ImGroupsController {

    private final ImGroupService groupService;
    private static final Long DEFAULT_USER_ID = 1L;

    public ImGroupsController(ImGroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/{id}")
    public Result<ImGroupVO> getById(@PathVariable Long id,
                                     @RequestHeader(value = "userId", required = false) Long userId) {
        userId = userId != null ? userId : DEFAULT_USER_ID;
        ImGroupVO vo = groupService.getGroupById(id, userId);
        return Result.success(vo);
    }

    @GetMapping("/{id}/members")
    public Result<List<ImGroupMemberVO>> getMembers(@PathVariable Long id,
                                                     @RequestHeader(value = "userId", required = false) Long userId) {
        userId = userId != null ? userId : DEFAULT_USER_ID;
        List<ImGroupMemberVO> list = groupService.getGroupMembers(id, userId);
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

    @DeleteMapping("/{id}/members/{memberId}")
    public Result<Void> removeMember(@PathVariable Long id,
                                     @PathVariable Long memberId,
                                     @RequestHeader(value = "userId", required = false) Long userId) {
        userId = userId != null ? userId : DEFAULT_USER_ID;
        groupService.removeMembers(id, java.util.Collections.singletonList(memberId), userId);
        return Result.success("移除成功");
    }

    @PutMapping("/{id}/members/{memberId}/role")
    public Result<Void> updateMemberRole(@PathVariable Long id,
                                         @PathVariable Long memberId,
                                         @RequestBody RoleUpdateRequest request,
                                         @RequestHeader(value = "userId", required = false) Long userId) {
        userId = userId != null ? userId : DEFAULT_USER_ID;
        boolean isAdmin = "admin".equalsIgnoreCase(request.getRole());
        groupService.setAdmin(id, memberId, isAdmin, userId);
        return Result.success("角色更新成功");
    }

    public static class RoleUpdateRequest {
        private String role;
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
