package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.group.ImGroupCreateRequest;
import com.ruoyi.im.dto.group.ImGroupUpdateRequest;
import com.ruoyi.im.service.ImGroupService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.group.ImGroupMemberVO;
import com.ruoyi.im.vo.group.ImGroupVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import java.util.List;

/**
 * 群组控制器
 * 提供群组创建、信息管理、成员管理、权限管理等功能
 *
 * @author ruoyi
 */
@Tag(name = "群组管理", description = "群组创建、信息管理、成员管理、权限管理等接口")
@RestController
@RequestMapping("/api/im/group")
@Validated
public class ImGroupController {

    private final ImGroupService imGroupService;

    /**
     * 构造器注入依赖
     *
     * @param imGroupService 群组服务
     */
    public ImGroupController(ImGroupService imGroupService) {
        this.imGroupService = imGroupService;
    }

    /**
     * 创建群组
     * 创建新群组，创建者自动成为群主
     *
     * @param request 群组创建请求参数，包含群组名称、头像、初始成员等
     * @return 创建结果，包含新群组ID
     * @apiNote 使用 @Valid 注解进行参数校验；创建后会自动创建群组会话
     * @throws BusinessException 当参数无效或创建失败时抛出业务异常
     */
    @Operation(summary = "创建群组", description = "创建新群组，创建者自动成为群主")
    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody ImGroupCreateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        Long groupId = imGroupService.createGroup(request, userId);
        return Result.success("创建成功", groupId);
    }

    /**
     * 获取群组详情
     * 查询指定群组的详细信息
     *
     * @param id 群组ID
     * @return 群组详细信息，包含群组基本信息和当前用户在群组中的角色
     * @apiNote 只有群组成员才能查看群组详情
     * @throws BusinessException 当群组不存在或用户不是群组成员时抛出业务异常
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
     * 查询当前用户加入的所有群组
     *
     * @return 群组列表，按最后活跃时间倒序排列
     * @apiNote 返回的群组信息包含未读消息数、最后消息等
     */
    @Operation(summary = "获取用户的群组列表", description = "查询当前用户加入的所有群组")
    @GetMapping("/list")
    public Result<List<ImGroupVO>> getUserGroups() {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImGroupVO> list = imGroupService.getUserGroups(userId);
        return Result.success(list);
    }

    /**
     * 更新群组信息
     * 更新群组的名称、头像、公告、描述等信息
     *
     * @param id 群组ID
     * @param request 群组更新请求参数，包含需要更新的字段
     * @return 更新结果
     * @apiNote 使用 @Valid 注解进行参数校验；只有群主和管理员有权限更新
     * @throws BusinessException 当群组不存在或无权限更新时抛出业务异常
     */
    @Operation(summary = "更新群组信息", description = "更新群组的名称、头像、公告、描述等信息")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable @Positive(message = "群组ID必须为正数") Long id,
                               @Valid @RequestBody ImGroupUpdateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        imGroupService.updateGroup(id, request, userId);
        return Result.success("更新成功");
    }

    /**
     * 解散群组
     * 解散指定群组，所有成员将被移除
     *
     * @param id 群组ID
     * @return 解散结果
     * @apiNote 只有群主有权限解散群组；解散后群组会话也会被删除
     * @throws BusinessException 当群组不存在或无权限解散时抛出业务异常
     */
    @Operation(summary = "解散群组", description = "解散指定群组，所有成员将被移除")
    @DeleteMapping("/{id}")
    public Result<Void> dismiss(@PathVariable @Positive(message = "群组ID必须为正数") Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        imGroupService.dismissGroup(id, userId);
        return Result.success("解散成功");
    }

    /**
     * 获取群组成员列表
     * 查询指定群组的所有成员
     *
     * @param id 群组ID
     * @return 成员列表，按角色排序（群主 > 管理员 > 普通成员）
     * @apiNote 只有群组成员才能查看成员列表
     * @throws BusinessException 当群组不存在时抛出业务异常
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
     * 批量添加用户到群组
     *
     * @param id 群组ID
     * @param userIds 要添加的用户ID列表
     * @return 添加结果
     * @apiNote 只有群主和管理员有权限添加成员；添加后会发送群组通知消息
     * @throws BusinessException 当群组不存在、无权限添加或超过人数限制时抛出业务异常
     */
    @Operation(summary = "添加群组成员", description = "批量添加用户到群组")
    @PostMapping("/{id}/members")
    public Result<Void> addMembers(@PathVariable @Positive(message = "群组ID必须为正数") Long id,
                                   @RequestBody @NotEmpty(message = "用户ID列表不能为空") List<Long> userIds) {
        Long userId = SecurityUtils.getLoginUserId();
        imGroupService.addMembers(id, userIds, userId);
        return Result.success("添加成功");
    }

    /**
     * 移除群组成员
     * 批量移除群组成员
     *
     * @param id 群组ID
     * @param userIds 要移除的用户ID列表
     * @return 移除结果
     * @apiNote 只有群主和管理员有权限移除成员；群主不能被移除；移除后会发送群组通知消息
     * @throws BusinessException 当群组不存在、无权限移除或移除群主时抛出业务异常
     */
    @Operation(summary = "移除群组成员", description = "批量移除群组成员")
    @DeleteMapping("/{id}/members")
    public Result<Void> removeMembers(@PathVariable @Positive(message = "群组ID必须为正数") Long id,
                                      @RequestBody @NotEmpty(message = "用户ID列表不能为空") List<Long> userIds) {
        Long userId = SecurityUtils.getLoginUserId();
        imGroupService.removeMembers(id, userIds, userId);
        return Result.success("移除成功");
    }

    /**
     * 退出群组
     * 当前用户退出指定群组
     *
     * @param id 群组ID
     * @return 退出结果
     * @apiNote 群主不能退出群组，只能转让群主或解散群组；退出后会发送群组通知消息
     * @throws BusinessException 当群组不存在或群主尝试退出时抛出业务异常
     */
    @Operation(summary = "退出群组", description = "当前用户退出指定群组")
    @PostMapping("/{id}/quit")
    public Result<Void> quit(@PathVariable @Positive(message = "群组ID必须为正数") Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        imGroupService.quitGroup(id, userId);
        return Result.success("退出成功");
    }

    /**
     * 设置/取消管理员
     * 设置或取消指定成员的管理员权限
     *
     * @param id 群组ID
     * @param targetUserId 目标用户ID
     * @param isAdmin 是否设置为管理员，true表示设置，false表示取消
     * @return 操作结果
     * @apiNote 只有群主有权限设置管理员；群主不能被取消管理员权限
     * @throws BusinessException 当群组不存在、无权限操作或目标用户不是成员时抛出业务异常
     */
    @Operation(summary = "设置/取消管理员", description = "设置或取消指定成员的管理员权限")
    @PutMapping("/{id}/admin/{userId}")
    public Result<Void> setAdmin(@PathVariable @Positive(message = "群组ID必须为正数") Long id,
                                 @PathVariable @Positive(message = "用户ID必须为正数") Long targetUserId,
                                 @RequestParam @NotNull(message = "管理员标识不能为空") Boolean isAdmin) {
        Long userId = SecurityUtils.getLoginUserId();
        imGroupService.setAdmin(id, targetUserId, isAdmin, userId);
        return Result.success(isAdmin ? "设置管理员成功" : "取消管理员成功");
    }

    /**
     * 禁言/取消禁言成员
     * 对指定成员进行禁言或取消禁言
     *
     * @param id 群组ID
     * @param targetUserId 目标用户ID
     * @param duration 禁言时长（秒），null表示取消禁言
     * @return 操作结果
     * @apiNote 只有群主和管理员有权限禁言成员；禁言时长为null时表示取消禁言
     * @throws BusinessException 当群组不存在、无权限操作或目标用户不是成员时抛出业务异常
     */
    @Operation(summary = "禁言/取消禁言成员", description = "对指定成员进行禁言或取消禁言")
    @PutMapping("/{id}/mute/{userId}")
    public Result<Void> muteMember(@PathVariable @Positive(message = "群组ID必须为正数") Long id,
                                   @PathVariable @Positive(message = "用户ID必须为正数") Long targetUserId,
                                   @RequestParam(required = false) Long duration) {
        Long userId = SecurityUtils.getLoginUserId();
        imGroupService.muteMember(id, targetUserId, duration, userId);
        return Result.success(duration == null ? "取消禁言成功" : "禁言成功");
    }

    /**
     * 转让群主
     * 将群主权限转让给指定成员
     *
     * @param id 群组ID
     * @param newOwnerId 新群主用户ID
     * @return 转让结果
     * @apiNote 只有当前群主有权限转让；转让后原群主变为管理员
     * @throws BusinessException 当群组不存在、无权限操作或目标用户不是成员时抛出业务异常
     */
    @Operation(summary = "转让群主", description = "将群主权限转让给指定成员")
    @PutMapping("/{id}/transfer/{userId}")
    public Result<Void> transferOwner(@PathVariable @Positive(message = "群组ID必须为正数") Long id,
                                      @PathVariable @Positive(message = "新群主ID必须为正数") Long newOwnerId) {
        Long userId = SecurityUtils.getLoginUserId();
        imGroupService.transferOwner(id, newOwnerId, userId);
        return Result.success("转让成功");
    }

    /**
     * 获取与指定用户的共同群组
     * 查询当前用户与目标用户共同加入的所有群组
     *
     * @param targetUserId 目标用户ID
     * @return 共同群组列表
     * @apiNote 用于用户详情页展示共同群组
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
     * 获取群组的二维码图片供扫码加入
     *
     * @param id 群组ID
     * @return 二维码信息，包含二维码图片URL
     * @apiNote 返回二维码图片URL或Base64数据，前端可直接展示
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
     * 重新生成群二维码，旧二维码失效
     *
     * @param id 群组ID
     * @return 新二维码信息
     * @apiNote 刷新后旧二维码将无法使用
     */
    @Operation(summary = "刷新群二维码", description = "重新生成群二维码")
    @PostMapping("/{id}/qrcode/refresh")
    public Result<java.util.Map<String, String>> refreshGroupQrcode(@PathVariable @Positive(message = "群组ID必须为正数") Long id) {
        Long currentUserId = SecurityUtils.getLoginUserId();
        java.util.Map<String, String> qrcodeInfo = imGroupService.refreshGroupQrcode(id, currentUserId);
        return Result.success(qrcodeInfo);
    }
}
