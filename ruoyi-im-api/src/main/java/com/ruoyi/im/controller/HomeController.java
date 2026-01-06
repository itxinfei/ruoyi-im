package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 首页控制器
 * 提供系统首页信息展示
 *
 * @author ruoyi
 */
@Tag(name = "系统首页", description = "系统首页信息展示接口")
@RestController
@RequestMapping("/")
public class HomeController {

    @Value("${spring.application.name:ruoyi-im-api}")
    private String applicationName;

    @Value("${app.env:prod}")
    private String environment;

    @Value("${app.security.enabled:true}")
    private Boolean securityEnabled;

    /**
     * 获取系统首页信息
     * 返回系统基本信息、环境配置等
     *
     * @return 系统首页信息
     */
    @Operation(summary = "获取系统首页信息", description = "返回系统基本信息、环境配置等")
    @GetMapping
    public Result<Map<String, Object>> getHomeInfo() {
        Map<String, Object> homeInfo = new HashMap<>();
        
        homeInfo.put("applicationName", applicationName);
        homeInfo.put("environment", environment);
        homeInfo.put("securityEnabled", securityEnabled);
        homeInfo.put("description", "RuoYi IM 即时通讯系统");
        homeInfo.put("version", "1.0.0");
        homeInfo.put("author", "RuoYi");
        
        return Result.success(homeInfo);
    }

    /**
     * 健康检查接口
     * 用于系统健康状态检查
     *
     * @return 健康状态
     */
    @Operation(summary = "健康检查", description = "用于系统健康状态检查")
    @GetMapping("/health")
    public Result<Map<String, String>> healthCheck() {
        Map<String, String> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("application", applicationName);
        healthInfo.put("environment", environment);
        
        return Result.success(healthInfo);
    }
}
