package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.websocket.ImWebSocketEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统健康检查控制器
 * <p>
 * 提供系统健康状态检查接口，用于监控和负载均衡器健康探测
 * </p>
 *
 * @author ruoyi
 */
@Tag(name = "健康检查", description = "系统健康状态检查接口")
@RestController
@RequestMapping("/api/im/health-check")
public class ImHealthCheckController {

    private static final Logger log = LoggerFactory.getLogger(ImHealthCheckController.class);

    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 构造器注入依赖
     *
     * @param dataSource 数据源
     * @param redisTemplate Redis模板
     */
    public ImHealthCheckController(DataSource dataSource, RedisTemplate<String, Object> redisTemplate) {
        this.dataSource = dataSource;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 基础健康检查
     * 用于负载均衡器探测，只返回200或503状态码
     */
    @GetMapping
    public Result<Void> health() {
        return Result.success("系统运行正常");
    }

    /**
     * 详细健康检查
     * 返回各组件的详细状态
     */
    @Operation(summary = "健康检查详情", description = "获取系统各组件的详细健康状态")
    @GetMapping("/detail")
    public Result<Map<String, Object>> healthDetail() {
        Map<String, Object> health = new HashMap<>();

        // 系统状态
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // 数据库健康检查
        health.put("database", checkDatabaseHealth());

        // Redis健康检查
        health.put("redis", checkRedisHealth());

        // WebSocket连接数
        health.put("websocket", checkWebSocketHealth());

        // JVM内存信息
        health.put("jvm", getJvmInfo());

        return Result.success(health);
    }

    /**
     * 数据库健康检查
     */
    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> db = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try (Connection conn = dataSource.getConnection()) {
            boolean isValid = conn.isValid(1); // 1秒超时
            long responseTime = System.currentTimeMillis() - startTime;

            db.put("status", isValid ? "UP" : "DOWN");
            db.put("responseTime", responseTime + "ms");

            if (!isValid) {
                db.put("error", "数据库连接无效");
            }
        } catch (Exception e) {
            db.put("status", "DOWN");
            db.put("error", e.getMessage());
            log.error("数据库健康检查失败", e);
        }

        return db;
    }

    /**
     * Redis健康检查
     */
    private Map<String, Object> checkRedisHealth() {
        Map<String, Object> redis = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            String testKey = "health:check:test";
            redisTemplate.opsForValue().set(testKey, "ping", 5, java.util.concurrent.TimeUnit.SECONDS);
            String result = redisTemplate.opsForValue().get(testKey).toString();
            redisTemplate.delete(testKey);

            long responseTime = System.currentTimeMillis() - startTime;
            redis.put("status", "ping".equals(result) ? "UP" : "DOWN");
            redis.put("responseTime", responseTime + "ms");
        } catch (Exception e) {
            redis.put("status", "DOWN");
            redis.put("error", e.getMessage());
            log.error("Redis健康检查失败", e);
        }

        return redis;
    }

    /**
     * WebSocket健康检查
     */
    private Map<String, Object> checkWebSocketHealth() {
        Map<String, Object> ws = new HashMap<>();

        try {
            int onlineCount = ImWebSocketEndpoint.getOnlineUserCount();
            ws.put("status", "UP");
            ws.put("onlineUsers", onlineCount);
        } catch (Exception e) {
            ws.put("status", "DOWN");
            ws.put("error", e.getMessage());
            log.error("WebSocket健康检查失败", e);
        }

        return ws;
    }

    /**
     * JVM信息
     */
    private Map<String, Object> getJvmInfo() {
        Map<String, Object> jvm = new HashMap<>();

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        jvm.put("status", "UP");
        jvm.put("totalMemory", formatBytes(totalMemory));
        jvm.put("usedMemory", formatBytes(usedMemory));
        jvm.put("freeMemory", formatBytes(freeMemory));
        jvm.put("maxMemory", formatBytes(maxMemory));
        jvm.put("memoryUsage", String.format("%.2f%%", (usedMemory * 100.0 / maxMemory)));
        jvm.put("processors", runtime.availableProcessors());

        return jvm;
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
     * 只读健康检查（不需要认证）
     */
    @GetMapping("/readiness")
    public Result<Map<String, Object>> readiness() {
        Map<String, Object> readiness = new HashMap<>();
        boolean ready = true;

        // 检查数据库
        try (Connection conn = dataSource.getConnection()) {
            readiness.put("database", conn.isValid(1) ? "UP" : "DOWN");
            if (!conn.isValid(1)) {
                ready = false;
            }
        } catch (Exception e) {
            readiness.put("database", "DOWN");
            ready = false;
        }

        // 检查Redis
        try {
            redisTemplate.opsForValue().get("health:check");
            readiness.put("redis", "UP");
        } catch (Exception e) {
            readiness.put("redis", "DOWN");
            ready = false;
        }

        readiness.put("status", ready ? "READY" : "NOT_READY");
        return ready ? Result.success(readiness) : Result.error(503, "系统未就绪", readiness);
    }

    /**
     * 存活检查（Kubernetes探针使用）
     */
    @GetMapping("/liveness")
    public Result<Map<String, Object>> liveness() {
        Map<String, Object> liveness = new HashMap<>();
        liveness.put("status", "ALIVE");
        liveness.put("timestamp", System.currentTimeMillis());
        return Result.success(liveness);
    }

    /**
     * 生成密码哈希（仅用于开发测试）
     */
    @Operation(summary = "生成密码哈希", description = "生成BCrypt密码哈希（仅开发环境使用）")
    @GetMapping("/hash")
    public Result<Map<String, String>> generateHash(@RequestParam(defaultValue = "123456") String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(password);
        boolean matches = encoder.matches(password, hash);

        Map<String, String> result = new HashMap<>();
        result.put("password", password);
        result.put("hash", hash);
        result.put("hashLength", String.valueOf(hash.length()));
        result.put("verified", String.valueOf(matches));

        return Result.success(result);
    }
}
