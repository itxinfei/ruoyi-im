package com.ruoyi.im.controller.admin;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImSystemConfigService;
import com.ruoyi.im.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统配置管理控制器（管理员）
 * 提供系统级别配置的管理接口，需要管理员权限
 * 权限说明：
 * - ADMIN: 可修改基础配置（撤回时限、文件上限、会话过期）
 * - SUPER_ADMIN: 可修改所有配置，包括安全策略
 *
 * @author ruoyi
 */
@Tag(name = "系统配置管理", description = "系统配置管理接口（管理员）")
@RestController
@RequestMapping("/api/admin/system-config")
public class ImSystemConfigAdminController {

    private static final Logger log = LoggerFactory.getLogger(ImSystemConfigAdminController.class);

    @Autowired
    private ImSystemConfigService systemConfigService;

    /**
     * 获取所有系统配置
     *
     * @return 系统配置 Map
     */
    @Operation(summary = "获取系统配置", description = "获取所有系统配置")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public Result<Map<String, Object>> getSystemConfigs() {
        Map<String, Object> configs = systemConfigService.getAllSystemConfigs();
        return Result.success(configs);
    }

    /**
     * 获取消息撤回时间限制
     *
     * @return 撤回时间限制（分钟）
     */
    @Operation(summary = "获取撤回时间限制", description = "获取消息撤回时间限制")
    @GetMapping("/recall-time-limit")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public Result<Integer> getRecallTimeLimit() {
        Integer limit = systemConfigService.getMessageRecallTimeLimit();
        return Result.success(limit);
    }

    /**
     * 设置消息撤回时间限制
     *
     * @param minutes 时间限制（分钟），0 表示不限制
     * @return 操作结果
     */
    @Operation(summary = "设置撤回时间限制", description = "设置消息撤回时间限制")
    @PutMapping("/recall-time-limit")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public Result<Void> setRecallTimeLimit(@RequestParam Integer minutes) {
        Long userId = SecurityUtils.getLoginUserId();
        log.info("管理员设置消息撤回时间限制：userId={}, minutes={}", userId, minutes);

        systemConfigService.setMessageRecallTimeLimit(minutes);
        return Result.success("设置成功");
    }

    /**
     * 更新系统配置
     * 根据配置键判断是否需要 SUPER_ADMIN 权限
     *
     * @param configKey   配置键
     * @param configValue 配置值
     * @return 操作结果
     */
    @Operation(summary = "更新系统配置", description = "更新指定的系统配置，安全策略相关配置仅 SUPER_ADMIN 可修改")
    @PutMapping("/{configKey}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public Result<Void> updateConfig(
            @PathVariable String configKey,
            @RequestBody Object configValue) {

        // 检查是否需要 SUPER_ADMIN 权限
        if (systemConfigService.requiresSuperAdmin(configKey)) {
            String userRole = SecurityUtils.getLoginUserRole();
            if (!"SUPER_ADMIN".equals(userRole)) {
                log.warn("用户权限不足，尝试修改 SUPER_ADMIN 专属配置：userId={}, configKey={}",
                    SecurityUtils.getLoginUserId(), configKey);
                return Result.error("权限不足：该配置仅 SUPER_ADMIN 可修改");
            }
        }

        Long userId = SecurityUtils.getLoginUserId();
        log.info("管理员更新系统配置：userId={}, configKey={}, value={}", userId, configKey, configValue);

        systemConfigService.updateSystemConfig(configKey, configValue);
        return Result.success("更新成功");
    }
}
