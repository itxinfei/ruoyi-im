package com.ruoyi.im.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.im.annotation.RequirePermission;
import com.ruoyi.im.annotation.ImApiVersion;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.util.PerformanceMonitor;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 性能监控控制器
 * 
 * @author ruoyi
 */
@Api(tags = "性能监控")
@RestController
@RequestMapping("/api/{version}/im/performance")
@ImApiVersion(value = {"v1", "v2"}, description = "性能监控API，支持v1和v2版本")
public class PerformanceController extends BaseController {

    /**
     * 获取系统性能指标
     */
    @ApiOperation("获取系统性能指标")
    @GetMapping("/metrics")
    @RequirePermission("im:performance:metrics")
    public Result<Map<String, Object>> getMetrics() {
        PerformanceMonitor.MonitorContext context = PerformanceMonitor.start("getMetrics");
        
        try {
            Map<String, Object> metrics = new HashMap<>();
            
            // 获取JVM内存信息
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            Map<String, Object> memoryMap = new HashMap<>();
            memoryMap.put("total", totalMemory);
            memoryMap.put("free", freeMemory);
            memoryMap.put("used", usedMemory);
            memoryMap.put("max", runtime.maxMemory());
            metrics.put("memory", memoryMap);
            
            // 获取CPU信息
            Map<String, Object> cpuMap = new HashMap<>();
            cpuMap.put("availableProcessors", runtime.availableProcessors());
            // loadAverage 方法在某些系统上可能不可用，使用系统属性替代
            cpuMap.put("loadAverage", getSystemLoadAverage());
            metrics.put("cpu", cpuMap);
            
            // 获取系统信息
            Map<String, Object> systemMap = new HashMap<>();
            systemMap.put("osName", System.getProperty("os.name"));
            systemMap.put("osVersion", System.getProperty("os.version"));
            systemMap.put("javaVersion", System.getProperty("java.version"));
            systemMap.put("currentTime", System.currentTimeMillis());
            metrics.put("system", systemMap);
            
            // 获取线程信息
            ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
            ThreadGroup parentGroup;
            while ((parentGroup = rootGroup.getParent()) != null) {
                rootGroup = parentGroup;
            }
            
            int totalThreads = Thread.activeCount();
            Map<String, Object> threadsMap = new HashMap<>();
            threadsMap.put("active", totalThreads);
            metrics.put("threads", threadsMap);
            
            context.end("Metrics retrieved successfully");
            return Result.success("性能指标获取成功", metrics);
        } catch (Exception e) {
            context.end("Error retrieving metrics: " + e.getMessage());
            return Result.error("性能指标获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取性能统计
     */
    @ApiOperation("获取性能统计")
    @GetMapping("/stats")
    @RequirePermission("im:performance:stats")
    public Result<Map<String, Object>> getStats() {
        PerformanceMonitor.MonitorContext context = PerformanceMonitor.start("getStats");
        
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 这里可以添加自定义的性能统计信息
            stats.put("uptime", System.currentTimeMillis()); // 简单示例
            stats.put("requestCount", 0); // 实际应用中应从计数器获取
            stats.put("errorCount", 0); // 实际应用中应从错误计数器获取
            
            context.end("Stats retrieved successfully");
            return Result.success("性能统计获取成功", stats);
        } catch (Exception e) {
            context.end("Error retrieving stats: " + e.getMessage());
            return Result.error("性能统计获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统负载平均值
     */
    private Object getSystemLoadAverage() {
        try {
            // 尝试使用 OperatingSystemMXBean 获取系统负载
            java.lang.management.OperatingSystemMXBean osBean = 
                java.lang.management.ManagementFactory.getOperatingSystemMXBean();
            
            // 检查是否是 UnixOperatingSystemMXBean（有 getSystemLoadAverage 方法）
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                double loadAverage = ((com.sun.management.OperatingSystemMXBean) osBean).getSystemLoadAverage();
                return loadAverage >= 0 ? loadAverage : "N/A";
            } else {
                // 对于其他系统，使用通用方法
                double loadAverage = osBean.getSystemLoadAverage();
                return loadAverage >= 0 ? loadAverage : "N/A";
            }
        } catch (Exception e) {
            // 如果无法获取负载平均值，返回 N/A
            return "N/A";
        }
    }

    /**
     * 触发垃圾回收
     */
    @ApiOperation("触发垃圾回收")
    @PostMapping("/gc")
    @RequirePermission("im:performance:gc")
    public Result<Void> triggerGC() {
        PerformanceMonitor.MonitorContext context = PerformanceMonitor.start("triggerGC");
        
        try {
            System.gc();
            context.end("GC triggered successfully");
            return Result.success();
        } catch (Exception e) {
            context.end("Error triggering GC: " + e.getMessage());
            return Result.error("垃圾回收触发失败: " + e.getMessage());
        }
    }
}