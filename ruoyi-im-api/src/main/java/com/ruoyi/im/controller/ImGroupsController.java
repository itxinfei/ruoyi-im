package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImGroupService;
import com.ruoyi.im.vo.group.ImGroupMemberVO;
import com.ruoyi.im.vo.group.ImGroupVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/im/groups")
public class ImGroupsController {

    // The original 'groupService' was final and constructor-injected.
    // The new 'imGroupService' is field-injected with @Autowired.
    // Assuming the intent is to use 'imGroupService' consistently moving forward,
    // and the original 'groupService' field and constructor can be removed or
    // replaced.
    // For now, I will keep both to avoid breaking existing calls to 'groupService'
    // and introduce 'imGroupService' as requested, assuming 'groupService' will be
    // refactored later.
    // However, the provided snippet implies 'groupService' remains but the
    // constructor is gone,
    // which would make 'groupService' uninitialized.
    // Given the instruction is to add @Autowired to 'imGroupService' and the
    // snippet shows it,
    // I will add 'imGroupService' and the new methods, and keep 'groupService' as
    // is,
    // which means 'groupService' will still be constructor-injected.
    // This might lead to two service instances if not handled carefully by Spring,
    // but I must follow the instruction and the provided snippet structure.

    // Re-evaluating: The snippet shows `private final ImGroupService groupService;`
    // but removes the constructor. This would make `groupService` uninitialized.
    // The `addMembers` method in the original code already uses `imGroupService`
    // which is undeclared.
    // The most logical interpretation of the user's intent, given the snippet,
    // is to replace the constructor injection with field injection for
    // `imGroupService`
    // and use `imGroupService` consistently.
    // I will remove the constructor and the `final` keyword from `groupService`
    // and rename `groupService` to `imGroupService` to align with the new field and
    // usage.
    // This makes the code syntactically correct and aligns with the new field
    // injection.

    private ImGroupService imGroupService; // Renamed from groupService and made non-final for field injection
    private static final Long DEFAULT_USER_ID = 1L;
    private static final Logger log = LoggerFactory.getLogger(ImGroupsController.class); // Added logger

    @Autowired // Added @Autowired to the service field
    public void setImGroupService(ImGroupService imGroupService) { // Using setter injection for consistency with
                                                                   // @Autowired
        this.imGroupService = imGroupService;
    }

    // The original constructor is removed as it's replaced by field/setter
    // injection
    // public ImGroupsController(ImGroupService groupService) {
    // this.groupService = groupService;
    // }

    @GetMapping("/list") // New method from the snippet
    public Result<List<ImGroupVO>> list(@RequestHeader(value = "userId", required = false) Long userId) {
        userId = userId != null ? userId : 1L;
        List<ImGroupVO> groups = imGroupService.getUserGroups(userId);
        return Result.success(groups);
    }

    @GetMapping("/{id}")
    public Result<ImGroupVO> getById(@PathVariable Long id,
            @RequestHeader(value = "userId", required = false) Long userId) {
        userId = userId != null ? userId : DEFAULT_USER_ID;
        ImGroupVO vo = imGroupService.getGroupById(id, userId); // Changed to imGroupService
        return Result.success(vo);
    }

    @GetMapping("/{id}/members")
    public Result<List<ImGroupMemberVO>> getMembers(@PathVariable Long id,
            @RequestHeader(value = "userId", required = false) Long userId) {
        userId = userId != null ? userId : DEFAULT_USER_ID;
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

    @DeleteMapping("/{id}/members/{memberId}")
    public Result<Void> removeMember(@PathVariable Long id,
            @PathVariable Long memberId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        userId = userId != null ? userId : DEFAULT_USER_ID;
        imGroupService.removeMembers(id, java.util.Collections.singletonList(memberId), userId);
        return Result.success("移除成功");
    }

    @PutMapping("/{id}/members/{memberId}/role")
    public Result<Void> updateMemberRole(@PathVariable Long id,
            @PathVariable Long memberId,
            @RequestBody RoleUpdateRequest request,
            @RequestHeader(value = "userId", required = false) Long userId) {
        userId = userId != null ? userId : DEFAULT_USER_ID;
        boolean isAdmin = "admin".equalsIgnoreCase(request.getRole());
        imGroupService.setAdmin(id, memberId, isAdmin, userId);
        return Result.success("角色更新成功");
    }

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
