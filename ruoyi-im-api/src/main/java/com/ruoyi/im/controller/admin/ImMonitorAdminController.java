package com.ruoyi.im.controller.admin;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImOnlineUserService;
import com.ruoyi.im.vo.online.OnlineUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统监控控制器
 * 提供系统监控、在线用户管理等功能
 *
 * @author ruoyi
 */
@Tag(name = "系统监控", description = "系统监控接口")
@RestController
@RequestMapping("/api/admin/monitor")
public class ImMonitorAdminController {

    private final ImOnlineUserService imOnlineUserService;

    public ImMonitorAdminController(ImOnlineUserService imOnlineUserService) {
        this.imOnlineUserService = imOnlineUserService;
    }

    /**
     * 获取系统监控数据
     *
     * @return 系统监控数据
     */
    @Operation(summary = "获取系统监控数据", description = "获取CPU、内存、在线用户数等系统监控数据")
    @GetMapping("/system")
    public Result<Map<String, Object>> getSystemMonitor() {
        Map<String, Object> data = new HashMap<>();

        // JVM 信息
        Runtime runtime = Runtime.getRuntime();
        int processors = runtime.availableProcessors();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        Map<String, Object> jvm = new HashMap<>();
        jvm.put("processors", processors);
        jvm.put("totalMemory", totalMemory / 1024 / 1024); // MB
        jvm.put("usedMemory", usedMemory / 1024 / 1024); // MB
        jvm.put("freeMemory", freeMemory / 1024 / 1024); // MB
        jvm.put("maxMemory", maxMemory / 1024 / 1024); // MB
        jvm.put("memoryUsagePercent", (usedMemory * 100 / maxMemory));
        data.put("jvm", jvm);

        // 操作系统信息
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            Map<String, Object> os = new HashMap<>();
            os.put("name", osBean.getName());
            os.put("version", osBean.getVersion());
            os.put("arch", osBean.getArch());
            os.put("processors", osBean.getAvailableProcessors());
            data.put("os", os);
        } catch (Exception e) {
            // 忽略获取失败
        }

        // 在线用户统计
        int onlineCount = imOnlineUserService.getOnlineUserCount();
        data.put("onlineUserCount", onlineCount);

        // 系统时间
        data.put("currentTime", System.currentTimeMillis());

        return Result.success(data);
    }

    /**
     * 获取在线用户列表
     *
     * @return 在线用户列表
     */
    @Operation(summary = "获取在线用户列表", description = "获取当前所有在线用户信息")
    @GetMapping("/online-users")
    public Result<List<OnlineUserVO>> getOnlineUsers() {
        List<OnlineUserVO> onlineUsers = imOnlineUserService.getOnlineUsers();
        return Result.success(onlineUsers);
    }

    /**
     * 获取在线用户统计
     *
     * @return 在线用户统计信息
     */
    @Operation(summary = "获取在线用户统计", description = "获取在线用户数量等统计信息")
    @GetMapping("/online-stats")
    public Result<Map<String, Object>> getOnlineUserStats() {
        Map<String, Object> stats = new HashMap<>();
        int onlineCount = imOnlineUserService.getOnlineUserCount();
        stats.put("onlineCount", onlineCount);
        stats.put("timestamp", System.currentTimeMillis());
        return Result.success(stats);
    }

    /**
     * 踢出用户（强制下线）
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @Operation(summary = "踢出用户", description = "强制指定用户下线")
    @DeleteMapping("/kick/{userId}")
    @PreAuthorize("hasAuthority('system:monitor:kick')")
    public Result<Void> kickUser(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        boolean success = imOnlineUserService.kickUser(userId);
        if (success) {
            return Result.success("踢出成功");
        } else {
            return Result.error("踢出失败，用户可能已离线");
        }
    }

    /**
     * 根据会话ID踢出用户
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @Operation(summary = "根据会话ID踢出用户", description = "强制关闭指定会话")
    @DeleteMapping("/kick/session/{sessionId}")
    @PreAuthorize("hasAuthority('system:monitor:kick')")
    public Result<Void> kickBySessionId(
            @Parameter(description = "会话ID") @PathVariable String sessionId) {
        boolean success = imOnlineUserService.kickBySessionId(sessionId);
        if (success) {
            return Result.success("踢出成功");
        } else {
            return Result.error("踢出失败，会话可能已关闭");
        }
    }

    /**
     * 获取系统性能指标
     *
     * @return 性能指标
     */
    @Operation(summary = "获取系统性能指标", description = "获取系统性能相关指标")
    @GetMapping("/performance")
    public Result<Map<String, Object>> getSystemPerformance() {
        Map<String, Object> data = new HashMap<>();

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long heapMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long nonHeapMemory = memoryBean.getNonHeapMemoryUsage().getUsed();

        Map<String, Object> memory = new HashMap<>();
        memory.put("heap", heapMemory / 1024 / 1024); // MB
        memory.put("nonHeap", nonHeapMemory / 1024 / 1024); // MB
        data.put("memory", memory);

        // 线程信息
        int threadCount = ManagementFactory.getThreadMXBean().getThreadCount();
        data.put("threadCount", threadCount);

        // 运行时信息
        Runtime runtime = Runtime.getRuntime();
        data.put("availableProcessors", runtime.availableProcessors());
        data.put("freeMemory", runtime.freeMemory() / 1024 / 1024);
        data.put("totalMemory", runtime.totalMemory() / 1024 / 1024);
        data.put("maxMemory", runtime.maxMemory() / 1024 / 1024);

        return Result.success(data);
    }
}
