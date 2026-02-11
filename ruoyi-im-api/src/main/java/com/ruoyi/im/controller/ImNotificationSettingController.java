package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImNotificationSetting;
import com.ruoyi.im.service.ImNotificationSettingService;
import com.ruoyi.im.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 用户通知设置控制器
 * 提供通知偏好设置的管理接口
 *
 * @author ruoyi
 */
@Tag(name = "通知设置", description = "用户通知偏好设置管理接口")
@RestController
@RequestMapping("/api/im/users/notification-settings")
public class ImNotificationSettingController {

    private static final Logger log = LoggerFactory.getLogger(ImNotificationSettingController.class);

    private final ImNotificationSettingService notificationSettingService;

    public ImNotificationSettingController(ImNotificationSettingService notificationSettingService) {
        this.notificationSettingService = notificationSettingService;
    }

    /**
     * 获取用户通知设置
     *
     * @return 通知设置对象
     */
    @Operation(summary = "获取通知设置", description = "获取用户的通知偏好设置")
    @GetMapping
    public Result<ImNotificationSetting> getNotificationSetting() {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            ImNotificationSetting setting = notificationSettingService.getNotificationSetting(userId);
            return Result.success(setting);
        } catch (Exception e) {
            log.error("获取通知设置失败: userId={}", userId, e);
            return Result.fail("获取通知设置失败");
        }
    }

    /**
     * 更新通知设置
     *
     * @param setting 通知设置对象
     * @return 操作结果
     */
    @Operation(summary = "更新通知设置", description = "更新用户的通知偏好设置")
    @PutMapping
    public Result<Void> updateNotificationSetting(@RequestBody ImNotificationSetting setting) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            notificationSettingService.updateNotificationSetting(userId, setting);
            return Result.success("通知设置更新成功");
        } catch (Exception e) {
            log.error("更新通知设置失败: userId={}", userId, e);
            return Result.fail("更新通知设置失败");
        }
    }

    /**
     * 初始化默认通知设置
     *
     * @return 操作结果
     */
    @Operation(summary = "初始化通知设置", description = "为用户初始化默认的通知设置")
    @PostMapping("/init")
    public Result<Void> initDefaultNotificationSetting() {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            notificationSettingService.initDefaultNotificationSetting(userId);
            return Result.success("默认通知设置初始化成功");
        } catch (Exception e) {
            log.error("初始化默认通知设置失败: userId={}", userId, e);
            return Result.fail("初始化默认通知设置失败");
        }
    }
}
