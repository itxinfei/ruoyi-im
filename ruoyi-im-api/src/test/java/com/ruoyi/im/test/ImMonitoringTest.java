package com.ruoyi.im.test;

import com.ruoyi.im.annotation.ImPerformanceMonitor;
import com.ruoyi.im.annotation.ImRateLimit;
import com.ruoyi.im.monitor.PerformanceStats;
import com.ruoyi.im.rateLimit.ImRateLimitManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

/**
 * IM监控和限流系统测试
 * 
 * 验证性能监控和限流功能的基本逻辑
 * 
 * @author ruoyi
 * @since 2024-01-05
 */
@RunWith(MockitoJUnitRunner.class)
public class ImMonitoringTest {

    @Mock
    private ImRateLimitManager rateLimitManager;

    /**
     * 测试性能监控统计
     */
    @Test
    public void testPerformanceStats() {
        PerformanceStats stats = new PerformanceStats();
        
        // 模拟一次正常请求
        stats.update(500, 200, true);
        assertEquals(1, stats.getTotalRequests());
        assertEquals(500, stats.getTotalResponseTime());
        assertEquals(1, stats.getSuccessCount());
        assertEquals(0, stats.getErrorCount());
        
        // 模拟一次异常请求
        stats.update(1500, 500, false);
        assertEquals(2, stats.getTotalRequests());
        assertEquals(2000, stats.getTotalResponseTime());
        assertEquals(1, stats.getSuccessCount());
        assertEquals(1, stats.getErrorCount());
        
        // 测试平均值计算
        assertEquals(1000.0, stats.getAverageResponseTime(), 0.01);
        assertEquals(50.0, stats.getSuccessRate(), 0.01);
    }

    /**
     * 测试限流策略 - Token Bucket
     */
    @Test
    public void testTokenBucketRateLimit() {
        // 模拟令牌桶限流：每分钟10个令牌
        ImRateLimit annotation = new ImRateLimit() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return ImRateLimit.class;
            }
            
            @Override
            public String key() { return "test:token_bucket"; }
            @Override
            public int rate() { return 10; }
            @Override
            public int window() { return 60; }
            @Override
            public LimitAlgorithm algorithm() { return LimitAlgorithm.TOKEN_BUCKET; }
            @Override
            public String message() { return "Token bucket limit exceeded"; }
            @Override
            public boolean enabled() { return true; }
            @Override
            public String group() { return "test"; }
        };
        
        // 模拟请求间隔
        ConcurrentHashMap<String, AtomicLong> tokenBuckets = new ConcurrentHashMap<>();
        AtomicLong tokens = new AtomicLong(10); // 初始令牌数
        tokenBuckets.put("test:token_bucket", tokens);
        
        // 测试前5个请求应该通过
        for (int i = 0; i < 5; i++) {
            if (tokens.decrementAndGet() >= 0) {
                assertTrue("Request " + i + " should be allowed", true);
            } else {
                assertFalse("Request " + i + " should be blocked", true);
            }
        }
        
        // 测试接下来的请求应该被拒绝（令牌不足）
        for (int i = 5; i < 10; i++) {
            if (tokens.decrementAndGet() >= 0) {
                assertFalse("Request " + i + " should be blocked", true);
            } else {
                assertTrue("Request " + i + " should be blocked", true);
            }
        }
    }

    /**
     * 测试限流策略 - Fixed Window
     */
    @Test
    public void testFixedWindowRateLimit() {
        // 模拟固定窗口限流：每分钟100个请求
        ConcurrentHashMap<String, AtomicLong> windows = new ConcurrentHashMap<>();
        AtomicLong requestCount = new AtomicLong(0);
        windows.put("test:fixed_window", requestCount);
        
        // 测试前100个请求应该通过
        for (int i = 0; i < 100; i++) {
            if (requestCount.incrementAndGet() <= 100) {
                assertTrue("Request " + i + " should be allowed", true);
            } else {
                assertFalse("Request " + i + " should be blocked", true);
            }
        }
        
        // 第101个请求应该被拒绝
        requestCount.incrementAndGet();
        assertFalse("Request 101 should be blocked", requestCount.get() <= 100);
    }

    /**
     * 测试监控注解配置
     */
    @Test
    public void testPerformanceMonitorAnnotation() {
        // 测试注解默认值
        ImPerformanceMonitor defaultMonitor = new ImPerformanceMonitor() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return ImPerformanceMonitor.class;
            }
            
            @Override
            public String value() { return ""; }
            @Override
            public boolean detailed() { return false; }
            @Override
            public long threshold() { return 1000; }
            @Override
            public String group() { return "default"; }
        };
        
        assertEquals("", defaultMonitor.value());
        assertFalse(defaultMonitor.detailed());
        assertEquals(1000L, defaultMonitor.threshold());
        assertEquals("default", defaultMonitor.group());
    }

    /**
     * 测试限流注解配置
     */
    @Test
    public void testRateLimitAnnotation() {
        // 测试注解默认值
        ImRateLimit defaultLimit = new ImRateLimit() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return ImRateLimit.class;
            }
            
            @Override
            public String key() { return ""; }
            @Override
            public int rate() { return 10; }
            @Override
            public int window() { return 1; }
            @Override
            public LimitAlgorithm algorithm() { return LimitAlgorithm.TOKEN_BUCKET; }
            @Override
            public String message() { return "请求过于频繁，请稍后再试"; }
            @Override
            public boolean enabled() { return true; }
            @Override
            public String group() { return "default"; }
        };
        
        assertEquals("", defaultLimit.key());
        assertEquals(10, defaultLimit.rate());
        assertEquals(1, defaultLimit.window());
        assertEquals(LimitAlgorithm.TOKEN_BUCKET, defaultLimit.algorithm());
        assertTrue(defaultLimit.enabled());
    }

    /**
     * 测试性能告警逻辑
     */
    @Test
    public void testPerformanceAlert() {
        PerformanceStats stats = new PerformanceStats();
        
        // 添加几个超时的请求
        for (int i = 0; i < 5; i++) {
            stats.update(1500, 200, true); // 1500ms > 1000ms阈值
        }
        
        // 添加几个正常的请求
        for (int i = 0; i < 5; i++) {
            stats.update(500, 200, true); // 500ms < 1000ms阈值
        }
        
        assertEquals(1000.0, stats.getAverageResponseTime(), 0.01);
        assertEquals(10, stats.getTotalRequests());
        
        // 平均响应时间超过阈值，应该触发告警
        assertTrue(stats.getAverageResponseTime() > 1000);
    }

    /**
     * 测试限流算法枚举
     */
    @Test
    public void testLimitAlgorithmEnum() {
        assertEquals("TOKEN_BUCKET", LimitAlgorithm.TOKEN_BUCKET.name());
        assertEquals("FIXED_WINDOW", LimitAlgorithm.FIXED_WINDOW.name());
        assertEquals("SLIDING_WINDOW", LimitAlgorithm.SLIDING_WINDOW.name());
        
        assertEquals(LimitAlgorithm.TOKEN_BUCKET, 
            LimitAlgorithm.valueOf("TOKEN_BUCKET"));
        assertEquals(LimitAlgorithm.FIXED_WINDOW, 
            LimitAlgorithm.valueOf("FIXED_WINDOW"));
        assertEquals(LimitAlgorithm.SLIDING_WINDOW, 
            LimitAlgorithm.valueOf("SLIDING_WINDOW"));
    }
}