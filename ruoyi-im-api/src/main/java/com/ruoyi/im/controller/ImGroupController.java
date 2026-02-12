package com.ruoyi.im.controller;

import com.ruoyi.im.annotation.RateLimit;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.group.ImGroupCreateRequest;
import com.ruoyi.im.dto.group.ImGroupUpdateRequest;
import com.ruoyi.im.service.ImGroupService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.group.ImGroupMemberVO;
import com.ruoyi.im.vo.group.ImGroupVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import java.util.List;

/**
 * 群组控制器
 *
 * @author ruoyi
 */
@Tag(name = "群组管理", description = "群组创建、信息管理、成员管理、权限管理等接口")
@RestController
@RequestMapping("/api/im/groups")
@Validated
public class ImGroupController {

    private final ImGroupService imGroupService;

    public ImGroupController(ImGroupService imGroupService) {
        this.imGroupService = imGroupService;
    }

    // ==================== 群组基础操作 ====================

    /**
     * 创建群组
     */
    @Operation(summary = "创建群组", description = "创建新群组，创建者自动成为群主")
    @PostMapping
    @RateLimit(key = "group_create", time = 60, count = 10, limitType = RateLimit.LimitType.USER)
    public Result<Long> create(@Valid @RequestBody ImGroupCreateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        Long groupId = imGroupService.createGroup(request, userId);
        return Result.success("创建成功", groupId);
    }

    /**
     * 获取群组详情
     */
    @Operation(summary = "获取群组详情", description = "查询指定群组的详细信息")
    @GetMapping("/{id}")
    public Result<ImGroupVO> getById(@PathVariable @Positive(message = "群组ID必须为正数") Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        ImGroupVO vo = imGroupService.getGroupById(id, userId);
        return Result.success(vo);
    }

    /**
     * 获取用户的群组列表
     */
    @Operation(summary = "获取用户的群组列表", description = "查询当前用户加入的所有群组")
    @GetMapping
    public Result<List<ImGroupVO>> getUserGroups() {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImGroupVO> list = imGroupService.getUserGroups(userId);
        return Result.success(list);
    }

    /**
     * 更新群组信息
     */
    @Operation(summary = "更新群组信息", description = "更新群组的名称、头像、公告、描述等信息")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable @Positive(message = "群组ID必须为正数") Long id,
                               @Valid @RequestBody ImGroupUpdateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        imGroupService.updateGroup(id, request, userId);
        return Result.success("更新成功");
    }

    /**
     * 解散群组
     */
    @Operation(summary = "解散群组", description = "解散指定群组，所有成员将被移除")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> dismiss(@PathVariable @Positive(message = "群组ID必须为正数") Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        imGroupService.dismissGroup(id, userId);
        return Result.success("解散成功");
    }

    // ==================== 群组成员管理 ====================

    /**
     * 获取群组成员列表
     */
    @Operation(summary = "获取群组成员列表", description = "查询指定群组的所有成员")
    @GetMapping("/{id}/members")
    public Result<List<ImGroupMemberVO>> getMembers(@PathVariable @Positive(message = "群组ID必须为正数") Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImGroupMemberVO> list = imGroupService.getGroupMembers(id, userId);
        return Result.success(list);
    }

    /**
     * 添加群组成员
     */
    @Operation(summary = "添加群组成员", description = "批量添加用户到群组")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PostMapping("/{id}/members")
    @RateLimit(key = "group_add_members", time = 60, count = 30, limitType = RateLimit.LimitType.USER)
    public Result<Void> addMembers(@PathVariable @Positive(message = "群组ID必须为正数") Long id,
                                   @RequestBody @NotEmpty(message = "用户ID列表不能为空") List<Long> userIds) {
        Long userId = SecurityUtils.getLoginUserId();
        imGroupService.addMembers(id, userIds, userId);
        return Result.success("添加成功");
    }

    /**
     * 移除群组成员
     */
    @Operation(summary = "移除群组成员", description = "批量移除群组成员")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @DeleteMapping("/{id}/members")
    public Result<Void> removeMembers(@PathVariable @Positive(message = "群组ID必须为正数") Long id,
                                      @RequestBody @NotEmpty(message = "用户ID列表不能为空") List<Long> userIds) {
        Long userId = SecurityUtils.getLoginUserId();
        imGroupService.removeMembers(id, userIds, userId);
        return Result.success("移除成功");
    }

    /**
     * 退出群组
     */
    @Operation(summary = "退出群组", description = "当前用户退出指定群组")
    @PostMapping("/{id}/quit")
    public Result<Void> quit(@PathVariable @Positive(message = "群组ID必须为正数") Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        imGroupService.quitGroup(id, userId);
        return Result.success("退出成功");
    }

    // ==================== 群组权限管理 ====================

    /**
     * 设置/取消管理员
     */
    @Operation(summary = "设置/取消管理员", description = "设置或取消指定成员的管理员权限")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PutMapping("/{id}/admin/{targetUserId}")
    public Result<Void> setAdmin(@PathVariable @Positive(message = "群组ID必须为正数") Long id,
                                 @PathVariable @Positive(message = "用户ID必须为正数") Long targetUserId,
                                 @RequestParam @NotNull(message = "管理员标识不能为空") Boolean isAdmin) {
        Long userId = SecurityUtils.getLoginUserId();
        imGroupService.setAdmin(id, targetUserId, isAdmin, userId);
        return Result.success(isAdmin ? "设置管理员成功" : "取消管理员成功");
    }

    /**
     * 禁言/取消禁言成员
     */
    @Operation(summary = "禁言/取消禁言成员", description = "对指定成员进行禁言或取消禁言")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PutMapping("/{id}/mute/{targetUserId}")
    public Result<Void> muteMember(@PathVariable @Positive(message = "群组ID必须为正数") Long id,
                                   @PathVariable @Positive(message = "用户ID必须为正数") Long targetUserId,
                                   @RequestParam(required = false) Long duration) {
        Long userId = SecurityUtils.getLoginUserId();
        imGroupService.muteMember(id, targetUserId, duration, userId);
        return Result.success(duration == null ? "取消禁言成功" : "禁言成功");
    }

    /**
     * 转让群主
     */
    @Operation(summary = "转让群主", description = "将群主权限转让给指定成员")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PutMapping("/{id}/transfer/{newOwnerId}")
    public Result<Void> transferOwner(@PathVariable @Positive(message = "群组ID必须为正数") Long id,
                                      @PathVariable @Positive(message = "新群主ID必须为正数") Long newOwnerId) {
        Long userId = SecurityUtils.getLoginUserId();
        imGroupService.transferOwner(id, newOwnerId, userId);
        return Result.success("转让成功");
    }

    // ==================== 其他功能 ====================

    /**
     * 获取与指定用户的共同群组
     */
    @Operation(summary = "获取共同群组", description = "获取当前用户与指定用户共同加入的群组列表")
    @GetMapping("/common/{targetUserId}")
    public Result<List<ImGroupVO>> getCommonGroups(@PathVariable @Positive(message = "用户ID必须为正数") Long targetUserId) {
        Long currentUserId = SecurityUtils.getLoginUserId();
        List<ImGroupVO> list = imGroupService.getCommonGroups(currentUserId, targetUserId);
        return Result.success(list);
    }

    /**
     * 获取群二维码
     */
    @Operation(summary = "获取群二维码", description = "获取群组的二维码图片供扫码加入")
    @GetMapping("/{id}/qrcode")
    public Result<java.util.Map<String, String>> getGroupQrcode(@PathVariable @Positive(message = "群组ID必须为正数") Long id) {
        Long currentUserId = SecurityUtils.getLoginUserId();
        java.util.Map<String, String> qrcodeInfo = imGroupService.getGroupQrcode(id, currentUserId);
        return Result.success(qrcodeInfo);
    }

    /**
     * 刷新群二维码
     */
    @Operation(summary = "刷新群二维码", description = "重新生成群二维码")
    @PostMapping("/{id}/qrcode/refresh")
    @RateLimit(key = "group_qrcode_refresh", time = 60, count = 10, limitType = RateLimit.LimitType.USER)
    public Result<java.util.Map<String, String>> refreshGroupQrcode(@PathVariable @Positive(message = "群组ID必须为正数") Long id) {
        Long currentUserId = SecurityUtils.getLoginUserId();
        java.util.Map<String, String> qrcodeInfo = imGroupService.refreshGroupQrcode(id, currentUserId);
        return Result.success(qrcodeInfo);
    }
}