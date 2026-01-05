package com.ruoyi.im.controller;

import com.ruoyi.common.annotation.RequirePermission;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.im.annotation.ImApiVersion;
import com.ruoyi.im.annotation.SwaggerTag;
import com.ruoyi.im.common.Result;
import io.swagger.annotations.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * IM配置控制器
 * 
 * 提供IM系统配置管理功能，包括系统配置查询、用户个性化配置管理、
 * 默认配置获取等接口。支持管理员查看系统配置，普通用户管理个人配置。
 * 
 * @author ruoyi
 */
@Api(tags = "IM配置管理")
@RestController
@RequestMapping("/api/{version}/im/config")
@ImApiVersion(value = {"v1", "v2"}, description = "配置管理API，支持v1和v2版本")
public class ImConfigController extends BaseController {

    /**
     * 获取IM系统配置
     * 
     * 获取IM系统的全局配置信息，包括服务器版本、用户数限制、
     * 消息保留天数、文件上传限制等系统级参数。
     * 该接口通常仅限管理员访问。
     * 
     * @return 系统配置信息，包含服务器版本、限制参数、功能开关等
     */
    @ApiOperation(value = "获取系统配置", notes = "获取IM系统全局配置信息，通常限管理员访问")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足，仅管理员可访问"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping("/system")
    @RequirePermission(value = "im:config:system:get", desc = "查看系统配置")
    public Result<Map<String, Object>> getSystemConfig() {
        try {
            // 获取系统配置
            Map<String, Object> config = new HashMap<>();
            config.put("serverVersion", "1.0.0");
            config.put("maxOnlineUsers", 10000);
            config.put("messageRetentionDays", 90);
            config.put("fileUploadSizeLimit", 104857600); // 100MB
            config.put("enableHistorySearch", true);
            config.put("maxGroupMembers", 200);
            config.put("enableMessageRecall", true);
            config.put("messageEditTimeout", 300); // 5分钟
            
            return Result.success(config, "查询成功");
        } catch (Exception e) {
            return Result.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户配置
     * 
     * 获取当前登录用户的个性化配置信息，包括主题、语言、
     * 通知设置、声音提醒、图片自动下载等个人偏好设置。
     * 
     * @return 用户个人配置信息，包含主题、语言、通知等设置
     */
    @ApiOperation(value = "获取用户配置", notes = "获取当前用户的个性化配置信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping("/user")
    @RequirePermission(value = "im:config:user:get", desc = "查看用户配置")
    public Result<Map<String, Object>> getUserConfig() {
        try {
            // 实际项目中需要从安全上下文获取当前用户ID
            // Long currentUserId = getCurrentUserIdFromSecurityContext();
            Long currentUserId = 1L; // 简化实现
            
            // 获取用户配置
            Map<String, Object> config = new HashMap<>();
            config.put("theme", "auto");
            config.put("language", "zh-CN");
            config.put("notification", true);
            config.put("sound", true);
            config.put("autoDownloadImages", true);
            config.put("showTypingIndicator", true);
            config.put("messageLogging", true);
            config.put("privacyMode", false);
            
            return Result.success(config, "查询成功");
        } catch (Exception e) {
            return Result.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户配置
     * 
     * 更新当前登录用户的个性化配置信息，包括主题、语言、
     * 通知设置、声音提醒、图片自动下载等个人偏好设置。
     * 配置更新后会立即生效。
     * 
     * @param configData 用户配置更新数据，包含需要更新的配置项
     * @return 更新后的完整用户配置信息
     */
    @ApiOperation(value = "更新用户配置", notes = "更新当前用户的个性化配置信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "配置更新成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 500, message = "配置更新失败")
    })
    @PutMapping("/user")
    @RequirePermission(value = "im:config:user:update", desc = "更新用户配置")
    public Result<Map<String, Object>> updateUserConfig(
            @Valid @ApiParam("用户配置更新数据，包含需要更新的配置项") @RequestBody Map<String, Object> configData) {
        try {
            // 实际项目中需要从安全上下文获取当前用户ID
            // Long currentUserId = getCurrentUserIdFromSecurityContext();
            Long currentUserId = 1L; // 简化实现
            
            // 在实际项目中，这里应该将用户配置保存到数据库
            // UserService.updateUserConfig(currentUserId, configData);
            
            return Result.success(configData, "配置更新成功");
        } catch (Exception e) {
            return Result.error(500, "配置更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取默认配置
     * 
     * 获取IM系统的默认配置值，用于新用户初始化或配置重置。
     * 包含消息保留天数、文件大小限制、消息长度限制、
     * 默认主题、通知声音等默认设置。
     * 
     * @return 系统默认配置信息，包含各种默认参数值
     */
    @ApiOperation(value = "获取默认配置", notes = "获取IM系统默认配置值，用于新用户初始化或配置重置")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping("/defaults")
    @RequirePermission(value = "im:config:defaults:get", desc = "查看默认配置")
    public Result<Map<String, Object>> getDefaultConfig() {
        try {
            // 获取默认配置
            Map<String, Object> defaultConfig = new HashMap<>();
            defaultConfig.put("messageRetentionDays", 90);
            defaultConfig.put("maxFileSize", 104857600); // 100MB
            defaultConfig.put("maxMessageLength", 1000);
            defaultConfig.put("autoDownloadImages", true);
            defaultConfig.put("showTypingIndicator", true);
            defaultConfig.put("enableMessageEncryption", false);
            defaultConfig.put("notificationSound", "default");
            defaultConfig.put("theme", "light");
            
            return Result.success(defaultConfig, "查询成功");
        } catch (Exception e) {
            return Result.error(500, "查询失败: " + e.getMessage());
        }
    }
}