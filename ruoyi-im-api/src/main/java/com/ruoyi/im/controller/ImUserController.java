package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.constant.SystemConstants;
import com.ruoyi.im.dto.base.BasePageRequest;
import com.ruoyi.im.dto.user.ChatBackgroundRequest;
import com.ruoyi.im.dto.user.ImRegisterRequest;
import com.ruoyi.im.dto.user.ImUserStatusUpdateRequest;
import com.ruoyi.im.dto.user.ImUserUpdateRequest;
import com.ruoyi.im.service.ImFriendService;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.contact.ImFriendVO;
import com.ruoyi.im.vo.user.ImUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户控制器
 * 提供用户信息查询、更新、密码修改等用户管理功能
 * 认证相关功能（登录、注册）请使用ImAuthController
 *
 * @author ruoyi
 */
@Tag(name = "用户管理", description = "用户信息查询、更新、头像上传、密码修改等接口")
@RestController
@RequestMapping("/api/im/users")
@Validated
public class ImUserController {

    private static final Logger log = LoggerFactory.getLogger(ImUserController.class);

    private final ImUserService imUserService;
    private final ImFriendService imFriendService;

    public ImUserController(ImUserService imUserService, ImFriendService imFriendService) {
        this.imUserService = imUserService;
        this.imFriendService = imFriendService;
    }

    // ==================== 用户基础操作 ====================

    /**
     * 创建用户
     * 管理员创建新用户
     */
    @Operation(summary = "创建用户", description = "管理员创建新用户账户")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ImRegisterRequest request) {
        log.info("创建用户: username={}", request.getUsername());
        Long userId = imUserService.createUser(request);
        log.info("用户创建成功: userId={}, username={}", userId, request.getUsername());
        return Result.success("创建成功", userId);
    }

    /**
     * 删除用户
     * 管理员删除指定用户
     */
    @Operation(summary = "删除用户", description = "管理员删除指定用户账户及其关联数据")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @Positive(message = "用户ID必须为正数") Long id) {
        log.info("删除用户: userId={}", id);
        imUserService.deleteUser(id);
        log.info("用户删除成功: userId={}", id);
        return Result.success("删除成功");
    }

    /**
     * 获取用户列表
     */
    @Operation(summary = "获取用户列表", description = "根据查询条件获取用户列表")
    @GetMapping
    public Result<List<ImUserVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) {
        com.ruoyi.im.dto.BasePageRequest request = new com.ruoyi.im.dto.BasePageRequest();
        if (pageNum != null) request.setPageNum(pageNum);
        if (pageSize != null) request.setPageSize(pageSize);
        List<ImUserVO> list = imUserService.getUserList(request);
        return Result.success(list);
    }

    /**
     * 批量获取用户信息
     */
    @Operation(summary = "批量获取用户信息", description = "根据用户ID列表批量获取用户基本信息")
    @GetMapping("/batch")
    public Result<List<ImUserVO>> getBatch(@RequestParam @NotBlank(message = "用户ID列表不能为空") String ids) {
        List<Long> idList = java.util.Arrays.stream(ids.split(","))
                .map(Long::valueOf)
                .collect(Collectors.toList());
        log.debug("批量获取用户信息: ids={}, count={}", ids, idList.size());
        List<ImUserVO> list = imUserService.getUsersByIds(idList);
        return Result.success(list);
    }

    /**
     * 搜索用户（已废弃）
     * @deprecated 请使用全局搜索接口 /api/im/search/contacts
     */
    @Deprecated
    @Operation(summary = "搜索用户（已废弃）", description = "请使用全局搜索接口 /api/im/search/contacts")
    @GetMapping("/search")
    public Result<List<ImUserVO>> search(@RequestParam @NotBlank(message = "搜索关键词不能为空") String keyword) {
        List<ImUserVO> list = imUserService.searchUsers(keyword);
        return Result.success(list);
    }

    // ==================== 用户信息查询 ====================

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/me")
    public Result<ImUserVO> getCurrentUser() {
        Long userId = SecurityUtils.getLoginUserId();
        ImUserVO vo = imUserService.getUserById(userId);
        return Result.success(vo);
    }

    /**
     * 获取指定用户信息
     */
    @Operation(summary = "获取指定用户信息", description = "根据用户ID获取用户详细信息")
    @GetMapping("/{id}")
    public Result<ImUserVO> getById(@PathVariable @Positive(message = "用户ID必须为正数") Long id) {
        ImUserVO vo = imUserService.getUserById(id);
        return Result.success(vo);
    }

    /**
     * 获取用户的好友列表
     */
    @Operation(summary = "获取用户好友列表", description = "查询指定用户的所有好友信息")
    @GetMapping("/{id}/friends")
    public Result<List<ImFriendVO>> getUserFriends(@PathVariable @Positive(message = "用户ID必须为正数") Long id) {
        List<ImFriendVO> list = imFriendService.getFriendList(id);
        return Result.success(list);
    }

    // ==================== 用户信息更新 ====================

    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息", description = "更新用户的昵称、头像、个性签名等信息")
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable @Positive(message = "用户ID必须为正数") Long id,
            @Valid @RequestBody ImUserUpdateRequest request) {
        imUserService.updateUser(id, request);
        return Result.success("更新成功");
    }

    /**
     * 修改用户状态
     */
    @Operation(summary = "修改用户状态", description = "修改用户状态（启用/禁用）")
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(
            @PathVariable @Positive(message = "用户ID必须为正数") Long id,
            @Valid @RequestBody ImUserStatusUpdateRequest request) {
        Integer status = "ENABLED".equals(request.getStatus())
                ? SystemConstants.USER_STATUS_ENABLED
                : SystemConstants.USER_STATUS_DISABLED;
        log.info("修改用户状态: userId={}, status={}", id, status);
        imUserService.updateStatus(id, status);
        log.info("用户状态修改成功: userId={}, status={}", id, status);
        return Result.success("状态修改成功");
    }

    /**
     * 修改密码
     */
    @Operation(summary = "修改密码", description = "验证旧密码后更新为新密码")
    @PutMapping("/{id}/password")
    public Result<Void> changePassword(
            @PathVariable @Positive(message = "用户ID必须为正数") Long id,
            @RequestParam @NotBlank(message = "旧密码不能为空") String oldPassword,
            @RequestParam @NotBlank(message = "新密码不能为空") String newPassword) {
        log.info("修改密码: userId={}", id);
        boolean success = imUserService.changePassword(id, oldPassword, newPassword);
        if (success) {
            log.info("密码修改成功: userId={}", id);
        } else {
            log.warn("密码修改失败: userId={}, 原因: 旧密码错误", id);
        }
        return success ? Result.success("密码修改成功") : Result.fail("旧密码错误");
    }

    // ==================== 头像操作 ====================

    /**
     * 上传用户头像
     */
    @Operation(summary = "上传用户头像", description = "上传用户头像图片并更新用户头像字段")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("avatarfile") MultipartFile file) {
        Long userId = SecurityUtils.getLoginUserId();
        log.info("上传用户头像: userId={}, fileName={}, size={}", userId, file.getOriginalFilename(), file.getSize());
        String avatarUrl = imUserService.uploadAvatar(userId, file);
        log.info("用户头像上传成功: userId={}, url={}", userId, avatarUrl);
        return Result.success("头像上传成功", avatarUrl);
    }

    // ==================== 聊天背景设置 ====================

    /**
     * 获取聊天背景设置
     */
    @Operation(summary = "获取聊天背景", description = "获取用户的聊天背景设置")
    @GetMapping("/me/background")
    public Result<Map<String, Object>> getChatBackground(
            @RequestParam(required = false) Long conversationId) {
        Long userId = SecurityUtils.getLoginUserId();
        Map<String, Object> background = imUserService.getChatBackground(userId, conversationId);
        return Result.success(background);
    }

    /**
     * 设置聊天背景
     */
    @Operation(summary = "设置聊天背景", description = "设置用户的聊天背景")
    @PutMapping("/me/background")
    public Result<Void> setChatBackground(@Valid @RequestBody ChatBackgroundRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        imUserService.setChatBackground(userId, request.getType(), request.getValue(), request.getConversationId());
        return Result.success("背景设置成功");
    }

    /**
     * 清除聊天背景
     */
    @Operation(summary = "清除聊天背景", description = "清除用户的聊天背景设置")
    @DeleteMapping("/me/background")
    public Result<Void> clearChatBackground(@RequestParam(required = false) Long conversationId) {
        Long userId = SecurityUtils.getLoginUserId();
        imUserService.clearChatBackground(userId, conversationId);
        return Result.success("背景已清除");
    }

    // ==================== 其他功能 ====================

    /**
     * 扫描二维码
     */
    @Operation(summary = "扫描二维码", description = "处理用户扫描二维码的请求")
    @PostMapping("/scan-qrcode")
    public Result<Map<String, Object>> scanQRCode(@RequestParam String qrData) {
        Long userId = SecurityUtils.getLoginUserId();
        Map<String, Object> result = imUserService.scanQRCode(userId, qrData);
        return Result.success(result);
    }
}