package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.vo.monitor.*;
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

/**
 * 系统监控控制器
 * <p>
 * 提供系统运行状态、性能指标等监控数据
 * </p>
 *
 * @author ruoyi
 */

@RestController
@RequestMapping("/api/im/monitor")
public class ImMonitorController {

    private static final Logger log = LoggerFactory.getLogger(ImMonitorController.class);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 系统运行状态 */
    private static final String STATUS_RUNNING = "RUNNING";
    /** 健康状态 */
    private static final String STATUS_HEALTHY = "HEALTHY";
    /** 警告状态 */
    private static final String STATUS_WARNING = "WARNING";
    /** 内存使用率警告阈值 */
    private static final double MEMORY_WARNING_THRESHOLD = 90;

    /**
     * 获取系统概览
     */
    @GetMapping("/overview")
    public Result<MonitorOverviewVO> getOverview() {
        MonitorOverviewVO overview = new MonitorOverviewVO();
        overview.setStatus(STATUS_RUNNING);
        overview.setTimestamp(LocalDateTime.now().format(FORMATTER));
        overview.setJvm(buildJvmInfo());
        overview.setThread(buildThreadInfo());
        overview.setMemory(buildOverviewMemory());
        return Result.success(overview);
    }

    /**
     * 获取JVM信息
     */
    @GetMapping("/jvm")
    public Result<JvmInfoVO> getJvmMetrics() {
        return Result.success(buildJvmInfo());
    }

    /**
     * 获取内存信息
     */
    @GetMapping("/memory")
    public Result<MemoryMetricsVO> getMemoryMetrics() {
        MemoryMetricsVO memory = new MemoryMetricsVO();

        MemoryMXBean memoryMxBean = ManagementFactory.getMemoryMXBean();

        MemoryUsage heapUsage = memoryMxBean.getHeapMemoryUsage();
        MemoryDetailVO heap = new MemoryDetailVO();
        heap.setInit(formatBytes(heapUsage.getInit()));
        heap.setUsed(formatBytes(heapUsage.getUsed()));
        heap.setCommitted(formatBytes(heapUsage.getCommitted()));
        heap.setMax(formatBytes(heapUsage.getMax()));
        heap.setUsagePercent(calculateUsagePercent(heapUsage.getUsed(), heapUsage.getMax()));
        memory.setHeap(heap);

        MemoryUsage nonHeapUsage = memoryMxBean.getNonHeapMemoryUsage();
        MemoryDetailVO nonHeap = new MemoryDetailVO();
        nonHeap.setInit(formatBytes(nonHeapUsage.getInit()));
        nonHeap.setUsed(formatBytes(nonHeapUsage.getUsed()));
        nonHeap.setCommitted(formatBytes(nonHeapUsage.getCommitted()));
        nonHeap.setMax(formatBytes(nonHeapUsage.getMax()));
        memory.setNonHeap(nonHeap);

        return Result.success(memory);
    }

    /**
     * 获取线程信息
     */
    @GetMapping("/thread")
    public Result<ThreadMetricsVO> getThreadMetrics() {
        ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();

        ThreadMetricsVO thread = new ThreadMetricsVO();
        thread.setCount(threadMxBean.getThreadCount());
        thread.setPeak(threadMxBean.getPeakThreadCount());
        thread.setDaemonCount(threadMxBean.getDaemonThreadCount());
        thread.setTotalStarted(threadMxBean.getTotalStartedThreadCount());

        return Result.success(thread);
    }

    /**
     * 获取类加载信息
     */
    @GetMapping("/classloading")
    public Result<ClassLoadingMetricsVO> getClassLoadingMetrics() {
        ClassLoadingMetricsVO classLoading = new ClassLoadingMetricsVO();
        classLoading.setLoadedClassCount(ManagementFactory.getClassLoadingMXBean().getLoadedClassCount());
        classLoading.setTotalLoadedClassCount(ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount());
        classLoading.setUnloadedClassCount(ManagementFactory.getClassLoadingMXBean().getUnloadedClassCount());

        return Result.success(classLoading);
    }

    /**
     * 获取编译信息
     */
    @GetMapping("/compilation")
    public Result<CompilationMetricsVO> getCompilationMetrics() {
        CompilationMetricsVO compilation = new CompilationMetricsVO();
        compilation.setName(ManagementFactory.getCompilationMXBean().getName());
        compilation.setTotalCompilationTime(ManagementFactory.getCompilationMXBean().getTotalCompilationTime());

        return Result.success(compilation);
    }

    /**
     * 获取运行时信息
     */
    @GetMapping("/runtime")
    public Result<RuntimeMetricsVO> getRuntimeMetrics() {
        RuntimeMetricsVO runtime = new RuntimeMetricsVO();
        runtime.setStartTime(FORMATTER.format(java.time.LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime()),
            java.time.ZoneId.systemDefault())));
        runtime.setUptime(formatUptime(ManagementFactory.getRuntimeMXBean().getUptime()));
        runtime.setBootClassPath(ManagementFactory.getRuntimeMXBean().getBootClassPath());

        return Result.success(runtime);
    }

    /**
     * 健康状态检查
     */
    @GetMapping("/health")
    public Result<HealthCheckVO> healthCheck() {
        MemoryMXBean memoryMxBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMxBean.getHeapMemoryUsage();
        double memoryUsagePercent = calculateUsagePercent(heapUsage.getUsed(), heapUsage.getMax());

        HealthCheckVO health = new HealthCheckVO();
        health.setStatus(memoryUsagePercent < MEMORY_WARNING_THRESHOLD ? STATUS_HEALTHY : STATUS_WARNING);
        health.setMemoryUsage(String.format("%.2f%%", memoryUsagePercent));

        return Result.success(health);
    }

    // ==================== 私有方法 ====================

    private JvmInfoVO buildJvmInfo() {
        JvmInfoVO jvm = new JvmInfoVO();
        Runtime runtime = Runtime.getRuntime();

        jvm.setJavaVersion(System.getProperty("java.version"));
        jvm.setJavaVendor(System.getProperty("java.vendor"));
        jvm.setJavaHome(System.getProperty("java.home"));
        jvm.setOsName(System.getProperty("os.name"));
        jvm.setOsVersion(System.getProperty("os.version"));
        jvm.setOsArch(System.getProperty("os.arch"));
        jvm.setProcessors(runtime.availableProcessors());
        jvm.setUptime(formatUptime(ManagementFactory.getRuntimeMXBean().getUptime()));
        jvm.setInputArguments(ManagementFactory.getRuntimeMXBean().getInputArguments());

        return jvm;
    }

    private ThreadMetricsVO buildThreadInfo() {
        ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();

        ThreadMetricsVO thread = new ThreadMetricsVO();
        thread.setCount(threadMxBean.getThreadCount());
        thread.setPeak(threadMxBean.getPeakThreadCount());

        return thread;
    }

    private OverviewMemoryVO buildOverviewMemory() {
        Runtime runtime = Runtime.getRuntime();

        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        OverviewMemoryVO memory = new OverviewMemoryVO();
        memory.setTotal(formatBytes(totalMemory));
        memory.setUsed(formatBytes(usedMemory));
        memory.setFree(formatBytes(freeMemory));
        memory.setMax(formatBytes(maxMemory));
        memory.setUsagePercent(calculateUsagePercent(usedMemory, maxMemory));

        return memory;
    }

    /**
     * 计算使用率百分比
     */
    private double calculateUsagePercent(long used, long max) {
        if (max <= 0) {
            return 0;
        }
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
