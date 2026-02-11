package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImConfigService;
import com.ruoyi.im.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户配置控制器
 * 提供通知设置、隐私设置、通用配置等功能
 *
 * @author ruoyi
 */
@Tag(name = "系统配置", description = "用户配置管理接口，提供通知设置、隐私设置、通用配置等功能")
@RestController
@RequestMapping("/api/im/user-configs")
public class ImConfigController {

    private final ImConfigService imConfigService;

    /**
     * 构造器注入依赖
     *
     * @param imConfigService 配置服务
     */
    public ImConfigController(ImConfigService imConfigService) {
        this.imConfigService = imConfigService;
    }

    /**
     * 获取通知设置
     * 获取当前用户的通知配置
     *
     * @return 通知设置
     * @apiNote 包含消息通知、群组通知、系统通知等设置
     */
    @Operation(summary = "获取通知设置", description = "获取当前用户的通知配置")
    @GetMapping("/notification")
    public Result<Map<String, Object>> getNotificationSettings() {
        Long userId = SecurityUtils.getLoginUserId();
        Map<String, Object> settings = imConfigService.getNotificationSettings(userId);
        return Result.success(settings);
    }

    /**
     * 更新通知设置
     * 更新当前用户的通知配置
     *
     * @param settings 设置项
     * @return 更新结果
     * @apiNote 支持部分更新，只修改传入的设置项
     */
    @Operation(summary = "更新通知设置", description = "更新当前用户的通知配置")
    @PutMapping("/notification")
    public Result<Void> updateNotificationSettings(
            @RequestBody Map<String, Object> settings) {
        Long userId = SecurityUtils.getLoginUserId();
        imConfigService.updateNotificationSettings(userId, settings);
        return Result.success("更新成功");
    }

    /**
     * 获取隐私设置
     * 获取当前用户的隐私配置
     *
     * @return 隐私设置
     * @apiNote 包含在线状态显示、陌生人消息、语音/视频通话等设置
     */
    @Operation(summary = "获取隐私设置", description = "获取当前用户的隐私配置")
    @GetMapping("/privacy")
    public Result<Map<String, Object>> getPrivacySettings() {
        Long userId = SecurityUtils.getLoginUserId();
        Map<String, Object> settings = imConfigService.getPrivacySettings(userId);
        return Result.success(settings);
    }

    /**
     * 更新隐私设置
     * 更新当前用户的隐私配置
     *
     * @param settings 设置项
     * @return 更新结果
     * @apiNote 支持部分更新，只修改传入的设置项
     */
    @Operation(summary = "更新隐私设置", description = "更新当前用户的隐私配置")
    @PutMapping("/privacy")
    public Result<Void> updatePrivacySettings(
            @RequestBody Map<String, Object> settings) {
        Long userId = SecurityUtils.getLoginUserId();
        imConfigService.updatePrivacySettings(userId, settings);
        return Result.success("更新成功");
    }

    /**
     * 获取通用设置
     * 获取当前用户的通用配置
     *
     * @return 通用设置
     * @apiNote 包含语言、主题、字体大小等设置
     */
    @Operation(summary = "获取通用设置", description = "获取当前用户的通用配置")
    @GetMapping
    public Result<Map<String, Object>> getGeneralSettings() {
        Long userId = SecurityUtils.getLoginUserId();
        Map<String, Object> settings = imConfigService.getGeneralSettings(userId);
        return Result.success(settings);
    }

    /**
     * 更新通用设置
     * 更新当前用户的通用配置
     *
     * @param settings 设置项
     * @return 更新结果
     * @apiNote 支持部分更新，只修改传入的设置项
     */
    @Operation(summary = "更新通用设置", description = "更新当前用户的通用配置")
    @PutMapping
    public Result<Void> updateGeneralSettings(
            @RequestBody Map<String, Object> settings) {
        Long userId = SecurityUtils.getLoginUserId();
        imConfigService.updateGeneralSettings(userId, settings);
        return Result.success("更新成功");
    }

    /**
     * 获取黑名单
     * 获取当前用户的黑名单列表
     *
     * @return 黑名单用户列表
     * @apiNote 返回被拉黑的用户信息
     */
    @Operation(summary = "获取黑名单", description = "获取当前用户的黑名单列表")
    @GetMapping("/blocked")
    public Result<java.util.List<Map<String, Object>>> getBlockedUsers() {
        Long userId = SecurityUtils.getLoginUserId();
        java.util.List<Map<String, Object>> blockedUsers = imConfigService.getBlockedUsers(userId);
        return Result.success(blockedUsers);
    }

    /**
     * 拉黑用户
     * 将指定用户加入黑名单
     *
     * @param targetUserId 目标用户ID
     * @return 操作结果
     * @apiNote 拉黑后将无法接收该用户的消息
     */
    @Operation(summary = "拉黑用户", description = "将指定用户加入黑名单")
    @PostMapping("/blocked/{targetUserId}")
    public Result<Void> blockUser(
            @PathVariable Long targetUserId) {
        Long userId = SecurityUtils.getLoginUserId();
        imConfigService.blockUser(userId, targetUserId);
        return Result.success("已拉黑");
    }

    /**
     * 解除拉黑
     * 将指定用户从黑名单移除
     *
     * @param targetUserId 目标用户ID
     * @return 操作结果
     * @apiNote 解除拉黑后可以正常接收该用户的消息
     */
    @Operation(summary = "解除拉黑", description = "将指定用户从黑名单移除")
    @DeleteMapping("/blocked/{targetUserId}")
    public Result<Void> unblockUser(
            @PathVariable Long targetUserId) {
        Long userId = SecurityUtils.getLoginUserId();
        imConfigService.unblockUser(userId, targetUserId);
        return Result.success("已解除拉黑");
    }
}
