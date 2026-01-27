package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.setting.UserSettingUpdateRequest;
import com.ruoyi.im.dto.setting.UserSettingsBatchUpdateRequest;
import com.ruoyi.im.service.IImUserSettingService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.setting.UserSettingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 用户设置控制器
 * 提供用户设置查询、更新等接口
 *
 * @author ruoyi
 */
@Tag(name = "用户设置", description = "用户偏好设置管理接口")
@RestController
@RequestMapping("/api/im/user/settings")
@Validated
public class ImUserSettingController {

    private final IImUserSettingService userSettingService;

    public ImUserSettingController(IImUserSettingService userSettingService) {
        this.userSettingService = userSettingService;
    }

    /**
     * 获取当前用户所有设置
     *
     * @return 用户设置列表
     */
    @Operation(summary = "获取用户设置", description = "获取当前登录用户的所有偏好设置")
    @GetMapping
    public Result<List<UserSettingVO>> getUserSettings() {
        Long userId = SecurityUtils.getLoginUserId();
        List<UserSettingVO> settings = userSettingService.getUserSettings(userId);
        return Result.success(settings);
    }

    /**
     * 获取当前用户设置（按类型）
     *
     * @param settingType 设置类型（CHAT, NOTIFICATION, PRIVACY, FILE, DATA, GENERAL）
     * @return 用户设置列表
     */
    @Operation(summary = "按类型获取用户设置", description = "根据设置类型获取用户偏好设置")
    @GetMapping("/type/{settingType}")
    public Result<List<UserSettingVO>> getUserSettingsByType(
            @PathVariable String settingType) {
        Long userId = SecurityUtils.getLoginUserId();
        List<UserSettingVO> settings = userSettingService.getUserSettingsByType(userId, settingType);
        return Result.success(settings);
    }

    /**
     * 获取当前用户设置的键值对映射
     *
     * @return 用户设置键值对映射
     */
    @Operation(summary = "获取用户设置映射", description = "获取当前用户设置的键值对形式，便于前端使用")
    @GetMapping("/map")
    public Result<Map<String, String>> getUserSettingsMap() {
        Long userId = SecurityUtils.getLoginUserId();
        Map<String, String> settingsMap = userSettingService.getUserSettingsMap(userId);
        return Result.success(settingsMap);
    }

    /**
     * 更新用户单个设置
     *
     * @param request 更新请求
     * @return 更新结果
     */
    @Operation(summary = "更新单个设置", description = "更新用户的单个偏好设置")
    @PutMapping
    public Result<Void> updateSetting(@Valid @RequestBody UserSettingUpdateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        userSettingService.updateSetting(userId, request);
        return Result.success("设置更新成功");
    }

    /**
     * 批量更新用户设置
     *
     * @param request 批量更新请求
     * @return 更新结果
     */
    @Operation(summary = "批量更新设置", description = "批量更新用户的多个偏好设置")
    @PutMapping("/batch")
    public Result<Void> batchUpdateSettings(@Valid @RequestBody UserSettingsBatchUpdateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        userSettingService.batchUpdateSettings(userId, request);
        return Result.success("设置批量更新成功");
    }

    /**
     * 删除用户单个设置
     *
     * @param settingKey 设置键名
     * @return 删除结果
     */
    @Operation(summary = "删除单个设置", description = "删除用户的单个偏好设置，恢复默认值")
    @DeleteMapping("/{settingKey}")
    public Result<Void> deleteSetting(
            @Parameter(description = "设置键名，如 chat.fontSize")
            @PathVariable String settingKey) {
        Long userId = SecurityUtils.getLoginUserId();
        userSettingService.deleteSetting(userId, settingKey);
        return Result.success("设置删除成功");
    }
}
