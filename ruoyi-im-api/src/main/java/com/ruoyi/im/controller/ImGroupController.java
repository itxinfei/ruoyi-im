package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.group.ImGroupCreateRequest;
import com.ruoyi.im.dto.group.ImGroupUpdateRequest;
import com.ruoyi.im.service.ImGroupService;
import com.ruoyi.im.vo.group.ImGroupMemberVO;
import com.ruoyi.im.vo.group.ImGroupVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 群组控制器
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/group")
public class ImGroupController {

    @Autowired
    private ImGroupService imGroupService;

    /**
     * 创建群组
     */
    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody ImGroupCreateRequest request,
                                @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        Long groupId = imGroupService.createGroup(request, userId);
        return Result.success("创建成功", groupId);
    }

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
     * 获取用户的群组列表
     */
    @GetMapping("/list")
    public Result<List<ImGroupVO>> getUserGroups(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        List<ImGroupVO> list = imGroupService.getUserGroups(userId);
        return Result.success(list);
    }

    /**
     * 更新群组信息
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody ImGroupUpdateRequest request,
                               @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        imGroupService.updateGroup(id, request, userId);
        return Result.success("更新成功");
    }

    /**
     * 解散群组
     */
    @DeleteMapping("/{id}")
    public Result<Void> dismiss(@PathVariable Long id,
                                @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        imGroupService.dismissGroup(id, userId);
        return Result.success("解散成功");
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
     * 移除群组成员
     */
    @DeleteMapping("/{id}/members")
    public Result<Void> removeMembers(@PathVariable Long id,
                                      @RequestBody List<Long> userIds,
                                      @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        imGroupService.removeMembers(id, userIds, userId);
        return Result.success("移除成功");
    }

    /**
     * 退出群组
     */
    @PostMapping("/{id}/quit")
    public Result<Void> quit(@PathVariable Long id,
                            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        imGroupService.quitGroup(id, userId);
        return Result.success("退出成功");
    }

    /**
     * 设置/取消管理员
     */
    @PutMapping("/{id}/admin/{userId}")
    public Result<Void> setAdmin(@PathVariable Long id,
                                 @PathVariable Long targetUserId,
                                 @RequestParam Boolean isAdmin,
                                 @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        imGroupService.setAdmin(id, targetUserId, isAdmin, userId);
        return Result.success(isAdmin ? "设置管理员成功" : "取消管理员成功");
    }

    /**
     * 禁言/取消禁言成员
     */
    @PutMapping("/{id}/mute/{userId}")
    public Result<Void> muteMember(@PathVariable Long id,
                                   @PathVariable Long targetUserId,
                                   @RequestParam(required = false) Long duration,
                                   @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        imGroupService.muteMember(id, targetUserId, duration, userId);
        return Result.success(duration == null ? "取消禁言成功" : "禁言成功");
    }

    /**
     * 转让群主
     */
    @PutMapping("/{id}/transfer/{userId}")
    public Result<Void> transferOwner(@PathVariable Long id,
                                      @PathVariable Long newOwnerId,
                                      @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        imGroupService.transferOwner(id, newOwnerId, userId);
        return Result.success("转让成功");
    }
}
