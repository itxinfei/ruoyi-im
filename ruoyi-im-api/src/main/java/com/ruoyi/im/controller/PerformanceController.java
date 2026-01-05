package com.ruoyi.im.controller;

import com.ruoyi.common.annotation.RequirePermission;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.im.annotation.SwaggerTag;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.monitor.PerformanceMonitor;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 性能监控控制器
 * 
 * 提供IM系统性能数据的查询和管理接口，包括指定接口的性能监控、
 * 所有接口性能数据总览、性能数据清理等功能。主要用于系统运维
 * 和性能优化分析。
 * 
 * @author ruoyi
 */
@Api(tags = "性能监控")
@RestController
@RequestMapping("/api/im/monitor")
public class PerformanceController extends BaseController {
    
    /**
     * 获取指定接口的性能数据
     * 
     * @param uri 接口URI
     * @param method 请求方法
     * @return 性能数据
     */
    @ApiOperation("获取指定接口的性能数据")
    @GetMapping("/performance")
    @RequirePermission(value = "im:monitor:performance", desc = "查看性能监控数据")
    public Result<Map<String, Object>> getPerformanceData(@RequestParam String uri, @RequestParam String method) {
        Map<String, Object> performanceData = PerformanceMonitor.getPerformanceData(uri, method);
        return Result.success(performanceData);
    }
    
    /**
     * 获取所有接口的性能数据
     * 
     * @return 所有性能数据
     */
    @ApiOperation("获取所有接口的性能数据")
    @GetMapping("/performance/all")
    @RequirePermission(value = "im:monitor:all", desc = "查看所有性能监控数据")
    public Result<Map<String, Map<String, Object>>> getAllPerformanceData() {
        Map<String, Map<String, Object>> allData = PerformanceMonitor.getAllPerformanceData();
        return Result.success(allData);
    }
    
    /**
     * 清理所有性能数据
     * 
     * @return 操作结果
     */
    @ApiOperation("清理性能数据")
    @PostMapping("/performance/clear")
    @RequirePermission(value = "im:monitor:clear", desc = "清理性能监控数据")
    public Result<Void> clearPerformanceData() {
        PerformanceMonitor.clearPerformanceData();
        return Result.success("性能数据已清理");
    }
}