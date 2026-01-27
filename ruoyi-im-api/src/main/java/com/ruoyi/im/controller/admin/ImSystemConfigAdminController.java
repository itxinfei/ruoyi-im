package com.ruoyi.im.controller.admin;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImSystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员-系统配置控制器
 *
 * @author ruoyi
 */
@Tag(name = "管理员-系统配置", description = "管理员管理系统全局参数接口")
@RestController
@RequestMapping("/api/admin/config")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ADMIN')")
public class ImSystemConfigAdminController {

    @Autowired
    private ImSystemConfigService configService;

    @Operation(summary = "获取所有系统配置")
    @GetMapping("/all")
    public Result<Map<String, Object>> getAllConfigs() {
        return Result.success(configService.getAllSystemConfigs());
    }

    @Operation(summary = "更新系统配置")
    @PutMapping("/update")
    public Result<Void> updateConfig(@RequestParam String key, @RequestParam String value) {
        configService.updateSystemConfig(key, value);
        return Result.success("更新成功");
    }

    @Operation(summary = "设置消息撤回时限")
    @PutMapping("/recall-limit")
    public Result<Void> setRecallLimit(@RequestParam Integer minutes) {
        configService.setMessageRecallTimeLimit(minutes);
        return Result.success("设置成功");
    }
}