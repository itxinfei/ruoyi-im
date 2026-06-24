package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImConfigService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.config.*;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户配置控制器
 * 提供通知设置、隐私设置、通用配置等功能
 *
 * @author ruoyi
 */

@RestController
@RequestMapping("/api/im/config")
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
     *
     * @return 通知设置
     */
    
    @GetMapping("/notification")
    public Result<NotificationSettingsVO> getNotificationSettings() {
        Long userId = SecurityUtils.getLoginUserId();
        Map<String, Object> settings = imConfigService.getNotificationSettings(userId);
        return Result.success(NotificationSettingsVO.fromMap(settings));
    }

    /**
     * 更新通知设置
     *
     * @param settings 设置项
     * @return 更新结果
     */
    
    @PutMapping("/notification")
    public Result<Void> updateNotificationSettings(
            @RequestBody Map<String, Object> settings) {
        Long userId = SecurityUtils.getLoginUserId();
        imConfigService.updateNotificationSettings(userId, settings);
        return Result.success("更新成功");
    }

    /**
     * 获取隐私设置
     *
     * @return 隐私设置
     */
    
    @GetMapping("/privacy")
    public Result<PrivacySettingsVO> getPrivacySettings() {
        Long userId = SecurityUtils.getLoginUserId();
        Map<String, Object> settings = imConfigService.getPrivacySettings(userId);
        return Result.success(PrivacySettingsVO.fromMap(settings));
    }

    /**
     * 更新隐私设置
     *
     * @param settings 设置项
     * @return 更新结果
     */
    
    @PutMapping("/privacy")
    public Result<Void> updatePrivacySettings(
            @RequestBody Map<String, Object> settings) {
        Long userId = SecurityUtils.getLoginUserId();
        imConfigService.updatePrivacySettings(userId, settings);
        return Result.success("更新成功");
    }

    /**
     * 获取通用设置
     *
     * @return 通用设置
     */
    
    @GetMapping
    public Result<GeneralSettingsVO> getGeneralSettings() {
        Long userId = SecurityUtils.getLoginUserId();
        Map<String, Object> settings = imConfigService.getGeneralSettings(userId);
        return Result.success(GeneralSettingsVO.fromMap(settings));
    }

    /**
     * 更新通用设置
     *
     * @param settings 设置项
     * @return 更新结果
     */
    
    @PutMapping
    public Result<Void> updateGeneralSettings(
            @RequestBody Map<String, Object> settings) {
        Long userId = SecurityUtils.getLoginUserId();
        imConfigService.updateGeneralSettings(userId, settings);
        return Result.success("更新成功");
    }

    /**
     * 获取黑名单
     *
     * @return 黑名单用户列表
     */
    
    @GetMapping("/blocked")
    public Result<List<BlockedUserVO>> getBlockedUsers() {
        Long userId = SecurityUtils.getLoginUserId();
        List<Map<String, Object>> blockedUsers = imConfigService.getBlockedUsers(userId);
        List<BlockedUserVO> result = new ArrayList<>();
        if (blockedUsers != null) {
            for (Map<String, Object> user : blockedUsers) {
                BlockedUserVO vo = new BlockedUserVO();
                vo.setUserId(toLong(user.get("userId")));
                vo.setNickname((String) user.get("nickname"));
                vo.setAvatar((String) user.get("avatar"));
                vo.setBlockTime((String) user.get("blockTime"));
                result.add(vo);
            }
        }
        return Result.success(result);
    }

    /**
     * 拉黑用户
     *
     * @param targetUserId 目标用户ID
     * @return 操作结果
     */
    
    @PostMapping("/blocked/{targetUserId}")
    public Result<Void> blockUser(
            @PathVariable Long targetUserId) {
        Long userId = SecurityUtils.getLoginUserId();
        imConfigService.blockUser(userId, targetUserId);
        return Result.success("已拉黑");
    }

    /**
     * 解除拉黑
     *
     * @param targetUserId 目标用户ID
     * @return 操作结果
     */
    
    @DeleteMapping("/blocked/{targetUserId}")
    public Result<Void> unblockUser(
            @PathVariable Long targetUserId) {
        Long userId = SecurityUtils.getLoginUserId();
        imConfigService.unblockUser(userId, targetUserId);
        return Result.success("已解除拉黑");
    }

    /**
     * 获取快捷键设置
     *
     * @return 快捷键设置
     */

    @GetMapping("/shortcut")
    public Result<ShortcutSettingsVO> getShortcutSettings() {
        Long userId = SecurityUtils.getLoginUserId();
        Map<String, Object> settings = imConfigService.getShortcutSettings(userId);
        return Result.success(ShortcutSettingsVO.fromMap(settings));
    }

    /**
     * 更新快捷键设置
     *
     * @param settings 设置项
     * @return 更新结果
     */

    @PutMapping("/shortcut")
    public Result<Void> updateShortcutSettings(
            @RequestBody Map<String, Object> settings) {
        Long userId = SecurityUtils.getLoginUserId();
        imConfigService.updateShortcutSettings(userId, settings);
        return Result.success("更新成功");
    }

    private Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }
}
