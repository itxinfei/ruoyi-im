package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.settings.UserSettingsBatchUpdateRequest;
import com.ruoyi.im.dto.settings.UserSettingsUpdateRequest;
import com.ruoyi.im.dto.settings.UserSettingsVO;
import com.ruoyi.im.service.ImUserSettingsService;
import com.ruoyi.im.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户设置控制器
 * 提供用户个人设置的查询、更新、删除接口
 *
 * @author ruoyi
 */
@Tag(name = "用户设置", description = "用户个人设置管理接口")
@RestController
@RequestMapping("/api/im/user-settings")
public class ImUserSettingsController {

    private static final Logger log = LoggerFactory.getLogger(ImUserSettingsController.class);

    private final ImUserSettingsService userSettingsService;

    public ImUserSettingsController(ImUserSettingsService userSettingsService) {
        this.userSettingsService = userSettingsService;
    }

    /**
     * 获取用户所有设置
     *
     * @return 用户设置VO
     */
    @Operation(summary = "获取用户所有设置", description = "获取用户的通用设置和通知设置")
    @GetMapping
    public Result<UserSettingsVO> getUserSettings() {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            UserSettingsVO settingsVO = userSettingsService.getUserSettings(userId);
            return Result.success(settingsVO);
        } catch (Exception e) {
            log.error("获取用户设置失败: userId={}", userId, e);
            return Result.fail("获取用户设置失败");
        }
    }

    /**
     * 获取指定类型的设置
     *
     * @param settingType 设置类型：general/notification/security/storage
     * @return 设置列表
     */
    @Operation(summary = "获取指定类型的设置", description = "根据设置类型获取用户设置列表")
    @GetMapping("/{settingType}")
    public Result<java.util.List<com.ruoyi.im.domain.ImUserSettings>> getSettingsByType(
            @PathVariable String settingType) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            java.util.List<com.ruoyi.im.domain.ImUserSettings> settings =
                    userSettingsService.getSettingsByType(userId, settingType);
            return Result.success(settings);
        } catch (Exception e) {
            log.error("获取用户设置失败: userId={}, type={}", userId, settingType, e);
            return Result.fail("获取用户设置失败");
        }
    }

    /**
     * 更新单个设置
     *
     * @param request 更新请求
     * @return 操作结果
     */
    @Operation(summary = "更新单个设置", description = "更新用户的单个设置项")
    @PutMapping
    public Result<Void> updateSetting(@Valid @RequestBody UserSettingsUpdateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            userSettingsService.updateSetting(userId, request);
            return Result.success("设置更新成功");
        } catch (Exception e) {
            log.error("更新用户设置失败: userId={}, key={}", userId, request.getSettingKey(), e);
            return Result.fail("更新设置失败");
        }
    }

    /**
     * 批量更新设置
     *
     * @param request 批量更新请求
     * @return 操作结果
     */
    @Operation(summary = "批量更新设置", description = "批量更新用户的多个设置项")
    @PutMapping("/batch")
    public Result<Void> batchUpdateSettings(@Valid @RequestBody UserSettingsBatchUpdateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            userSettingsService.batchUpdateSettings(userId, request);
            return Result.success("批量更新成功");
        } catch (Exception e) {
            log.error("批量更新用户设置失败: userId={}", userId, e);
            return Result.fail("批量更新失败");
        }
    }

    /**
     * 删除设置
     *
     * @param settingKey 设置键
     * @return 操作结果
     */
    @Operation(summary = "删除设置", description = "删除用户的指定设置项（恢复默认值）")
    @DeleteMapping("/{settingKey}")
    public Result<Void> deleteSetting(@PathVariable String settingKey) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            userSettingsService.deleteSetting(userId, settingKey);
            return Result.success("设置已删除");
        } catch (Exception e) {
            log.error("删除用户设置失败: userId={}, key={}", userId, settingKey, e);
            return Result.fail("删除设置失败");
        }
    }

    /**
     * 初始化默认设置
     *
     * @return 操作结果
     */
    @Operation(summary = "初始化默认设置", description = "为用户初始化默认设置值")
    @PostMapping("/init")
    public Result<Void> initDefaultSettings() {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            userSettingsService.initDefaultSettings(userId);
            return Result.success("默认设置初始化成功");
        } catch (Exception e) {
            log.error("初始化默认设置失败: userId={}", userId, e);
            return Result.fail("初始化设置失败");
        }
    }
}
