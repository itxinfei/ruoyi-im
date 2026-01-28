package com.ruoyi.im.util;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 性能监控工具类
 * 提供统一的性能指标收集接口
 *
 * @author ruoyi
 */
@Component
public class MetricsUtil {

    private static final Logger log = LoggerFactory.getLogger(MetricsUtil.class);

    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * 记录计数器指标
     * 用于统计事件发生次数
     *
     * @param name   指标名称
     * @param tags   标签（可选）
     */
    public void incrementCounter(String name, String... tags) {
        try {
            Counter counter = Counter.builder(name)
                    .tags(tags)
                    .description("Custom counter metric")
                    .register(meterRegistry);
            counter.increment();
        } catch (Exception e) {
            log.warn("记录计数器指标失败: name={}, error={}", name, e.getMessage());
        }
    }

    /**
     * 记录计时器指标
     * 用于统计方法执行时间
     *
     * @param name   指标名称
     * @param tags   标签（可选）
     * @param runnable 要执行的代码块
     */
    public void recordTime(String name, Runnable runnable, String... tags) {
        try {
            Timer timer = Timer.builder(name)
                    .tags(tags)
                    .description("Custom timer metric")
                    .register(meterRegistry);
            timer.record(runnable);
        } catch (Exception e) {
            log.warn("记录计时器指标失败: name={}, error={}", name, e.getMessage());
            // 即使监控失败，也要执行业务逻辑
            runnable.run();
        }
    }

    /**
     * 记录指定时间的计时器指标
     *
     * @param name       指标名称
     * @param duration   持续时间
     * @param unit       时间单位
     * @param tags       标签（可选）
     */
    public void recordTime(String name, long duration, TimeUnit unit, String... tags) {
        try {
            Timer timer = Timer.builder(name)
                    .tags(tags)
                    .description("Custom timer metric")
                    .register(meterRegistry);
            timer.record(duration, unit);
        } catch (Exception e) {
            log.warn("记录计时器指标失败: name={}, error={}", name, e.getMessage());
        }
    }

    /**
     * 创建计时器样本
     * 用于手动记录开始和结束时间
     *
     * @param name 指标名称
     * @param tags 标签（可选）
     * @return Timer.Sample对象
     */
    public Timer.Sample startTimer(String name, String... tags) {
        return Timer.start(meterRegistry);
    }

    /**
     * 停止计时器样本
     *
     * @param sample Timer.Sample对象
     * @param name   指标名称
     * @param tags   标签（可选）
     */
    public void stopTimer(Timer.Sample sample, String name, String... tags) {
        try {
            Timer timer = Timer.builder(name)
                    .tags(tags)
                    .description("Custom timer metric")
                    .register(meterRegistry);
            sample.stop(timer);
        } catch (Exception e) {
            log.warn("停止计时器失败: name={}, error={}", name, e.getMessage());
        }
    }

    /**
     * 记录消息发送指标
     *
     * @param messageType 消息类型
     * @param status      发送状态
     */
    public void recordMessageSend(String messageType, String status) {
        incrementCounter("im.message.send",
                "type", messageType,
                "status", status);
    }

    /**
     * 记录WebSocket连接指标
     *
     * @param action  动作类型（connect/disconnect）
     * @param userId  用户ID
     */
    public void recordWebSocketConnection(String action, Long userId) {
        incrementCounter("im.websocket.connection",
                "action", action,
                "user_id", userId != null ? userId.toString() : "unknown");
    }

    /**
     * 记录API请求指标
     *
     * @param endpoint   API端点
     * @param method     请求方法
     * @param statusCode 响应状态码
     */
    public void recordApiRequest(String endpoint, String method, int statusCode) {
        incrementCounter("im.api.request",
                "endpoint", endpoint,
                "method", method,
                "status", String.valueOf(statusCode));
    }

    /**
     * 记录数据库操作指标
     *
     * @param operation 操作类型（insert/update/delete/select）
     * @param table     表名
     */
    public void recordDatabaseOperation(String operation, String table) {
        incrementCounter("im.database.operation",
                "operation", operation,
                "table", table);
    }

    /**
     * 记录缓存操作指标
     *
     * @param operation 操作类型（hit/miss/set/delete）
     * @param key       缓存键
     */
    public void recordCacheOperation(String operation, String key) {
        incrementCounter("im.cache.operation",
                "operation", operation,
                "key", key);
    }
}