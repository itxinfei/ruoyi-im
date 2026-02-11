package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统监控控制器
 * <p>
 * 提供系统运行状态、性能指标等监控数据
 * </p>
 *
 * @author ruoyi
 */
@Tag(name = "系统监控", description = "系统运行状态和性能指标接口")
@RestController
@RequestMapping("/api/im/system-monitors")
public class ImMonitorController {

    private static final Logger log = LoggerFactory.getLogger(ImMonitorController.class);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取系统概览
     */
    @Operation(summary = "系统概览", description = "获取系统整体运行状态概览")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> overview = new HashMap<>();

        // 系统状态
        overview.put("status", "RUNNING");
        overview.put("timestamp", LocalDateTime.now().format(FORMATTER));

        // JVM信息
        overview.put("jvm", getJvmInfo());

        // 线程信息
        overview.put("thread", getThreadInfo());

        // 内存信息
        overview.put("memory", getMemoryInfo());

        return Result.success(overview);
    }

    /**
     * 获取JVM信息
     */
    @Operation(summary = "JVM信息", description = "获取JVM运行时信息")
    @GetMapping("/jvm")
    public Result<Map<String, Object>> getJvmMetrics() {
        Map<String, Object> jvm = new HashMap<>();

        Runtime runtime = Runtime.getRuntime();

        // JVM基本参数
        jvm.put("javaVersion", System.getProperty("java.version"));
        jvm.put("javaVendor", System.getProperty("java.vendor"));
        jvm.put("javaHome", System.getProperty("java.home"));
        jvm.put("osName", System.getProperty("os.name"));
        jvm.put("osVersion", System.getProperty("os.version"));
        jvm.put("osArch", System.getProperty("os.arch"));
        jvm.put("processors", runtime.availableProcessors());

        // JVM启动时间
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        jvm.put("uptime", formatUptime(uptime));

        // JVM参数
        jvm.put("inputArguments", ManagementFactory.getRuntimeMXBean().getInputArguments());

        return Result.success(jvm);
    }

    /**
     * 获取内存信息
     */
    @Operation(summary = "内存信息", description = "获取JVM内存使用情况")
    @GetMapping("/memory")
    public Result<Map<String, Object>> getMemoryMetrics() {
        Map<String, Object> memory = new HashMap<>();

        MemoryMXBean memoryMxBean = ManagementFactory.getMemoryMXBean();

        // 堆内存
        MemoryUsage heapUsage = memoryMxBean.getHeapMemoryUsage();
        Map<String, Object> heap = new HashMap<>();
        heap.put("init", formatBytes(heapUsage.getInit()));
        heap.put("used", formatBytes(heapUsage.getUsed()));
        heap.put("committed", formatBytes(heapUsage.getCommitted()));
        heap.put("max", formatBytes(heapUsage.getMax()));
        heap.put("usagePercent", calculateUsagePercent(heapUsage.getUsed(), heapUsage.getMax()));

        memory.put("heap", heap);

        // 非堆内存
        MemoryUsage nonHeapUsage = memoryMxBean.getNonHeapMemoryUsage();
        Map<String, Object> nonHeap = new HashMap<>();
        nonHeap.put("init", formatBytes(nonHeapUsage.getInit()));
        nonHeap.put("used", formatBytes(nonHeapUsage.getUsed()));
        nonHeap.put("committed", formatBytes(nonHeapUsage.getCommitted()));
        nonHeap.put("max", formatBytes(nonHeapUsage.getMax()));

        memory.put("nonHeap", nonHeap);

        return Result.success(memory);
    }

    /**
     * 获取线程信息
     */
    @Operation(summary = "线程信息", description = "获取JVM线程使用情况")
    @GetMapping("/thread")
    public Result<Map<String, Object>> getThreadMetrics() {
        Map<String, Object> thread = new HashMap<>();

        ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();

        thread.put("count", threadMxBean.getThreadCount());
        thread.put("peak", threadMxBean.getPeakThreadCount());
        thread.put("daemonCount", threadMxBean.getDaemonThreadCount());
        thread.put("totalStarted", threadMxBean.getTotalStartedThreadCount());

        return Result.success(thread);
    }

    /**
     * 获取类加载信息
     */
    @Operation(summary = "类加载信息", description = "获取JVM类加载情况")
    @GetMapping("/classloading")
    public Result<Map<String, Object>> getClassLoadingMetrics() {
        Map<String, Object> classLoading = new HashMap<>();

        classLoading.put("loadedClassCount", ManagementFactory.getClassLoadingMXBean().getLoadedClassCount());
        classLoading.put("totalLoadedClassCount", ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount());
        classLoading.put("unloadedClassCount", ManagementFactory.getClassLoadingMXBean().getUnloadedClassCount());

        return Result.success(classLoading);
    }

    /**
     * 获取编译信息
     */
    @Operation(summary = "编译信息", description = "获取JIT编译信息")
    @GetMapping("/compilation")
    public Result<Map<String, Object>> getCompilationMetrics() {
        Map<String, Object> compilation = new HashMap<>();

        compilation.put("name", ManagementFactory.getCompilationMXBean().getName());
        compilation.put("totalCompilationTime", ManagementFactory.getCompilationMXBean().getTotalCompilationTime());

        return Result.success(compilation);
    }

    /**
     * 获取运行时信息
     */
    @Operation(summary = "运行时信息", description = "获取JVM运行时详细信息")
    @GetMapping("/runtime")
    public Result<Map<String, Object>> getRuntimeMetrics() {
        Map<String, Object> runtime = new HashMap<>();

        runtime.put("startTime", FORMATTER.format(java.time.LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime()),
            java.time.ZoneId.systemDefault())));

        runtime.put("uptime", formatUptime(ManagementFactory.getRuntimeMXBean().getUptime()));
        runtime.put("bootClassPath", ManagementFactory.getRuntimeMXBean().getBootClassPath());

        return Result.success(runtime);
    }

    /**
     * 健康状态检查
     */
    @Operation(summary = "健康检查", description = "检查系统健康状态")
    @GetMapping("/health")
    public Result<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();

        // 检查内存使用率
        MemoryMXBean memoryMxBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMxBean.getHeapMemoryUsage();
        double memoryUsagePercent = calculateUsagePercent(heapUsage.getUsed(), heapUsage.getMax());

        boolean healthy = memoryUsagePercent < 90;
        health.put("status", healthy ? "HEALTHY" : "WARNING");
        health.put("memoryUsage", String.format("%.2f%%", memoryUsagePercent));

        return Result.success(health);
    }

    // ==================== 私有方法 ====================

    private Map<String, Object> getJvmInfo() {
        Map<String, Object> jvm = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();

        jvm.put("javaVersion", System.getProperty("java.version"));
        jvm.put("processors", runtime.availableProcessors());
        jvm.put("uptime", formatUptime(ManagementFactory.getRuntimeMXBean().getUptime()));

        return jvm;
    }

    private Map<String, Object> getThreadInfo() {
        Map<String, Object> thread = new HashMap<>();
        ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();

        thread.put("count", threadMxBean.getThreadCount());
        thread.put("peak", threadMxBean.getPeakThreadCount());

        return thread;
    }

    private Map<String, Object> getMemoryInfo() {
        Map<String, Object> memory = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();

        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        memory.put("total", formatBytes(totalMemory));
        memory.put("used", formatBytes(usedMemory));
        memory.put("free", formatBytes(freeMemory));
        memory.put("max", formatBytes(maxMemory));
        memory.put("usagePercent", calculateUsagePercent(usedMemory, maxMemory));

        return memory;
    }

    /**
     * 计算使用率百分比
     */
    private double calculateUsagePercent(long used, long max) {
        if (max <= 0) return 0;
        return (used * 100.0) / max;
    }

    /**
     * 格式化字节大小
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 格式化运行时间
     */
    private String formatUptime(long uptime) {
        long days = uptime / (24 * 60 * 60 * 1000);
        long hours = (uptime % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
        long minutes = (uptime % (60 * 60 * 1000)) / (60 * 1000);
        long seconds = (uptime % (60 * 1000)) / 1000;

        if (days > 0) {
            return String.format("%d天%d小时%d分钟", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%d分钟%d秒", minutes, seconds);
        } else {
            return seconds + "秒";
        }
    }
}
