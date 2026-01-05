package com.ruoyi.im.test;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能测试基类
 * 
 * 提供性能测试的通用方法和工具，包括：
 * - HTTP请求配置和客户端
 * - 性能数据收集和统计
 * - 线程池和并发控制
 * - 测试报告生成辅助工具
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class PerformanceTestBase {

    // HTTP客户端配置
    protected static final int CONNECTION_TIMEOUT = 10000; // 连接超时时间（毫秒）
    protected static final int READ_TIMEOUT = 30000; // 读取超时时间（毫秒）
    protected static final int REQUEST_TIMEOUT = 30000; // 请求超时时间（毫秒）
    
    // 默认并发线程数
    protected static final int DEFAULT_CONCURRENT_USERS = 10;
    
    // 默认请求次数
    protected static final int DEFAULT_REQUEST_COUNT = 100;
    
    // HTTP客户端
    protected static HttpClient httpClient;
    
    // 性能统计数据
    protected Map<String, PerformanceMetric> performanceMetrics = new HashMap<>();
    
    // 线程池
    protected ExecutorService executorService;
    
    // 随机数生成器
    protected Random random = new Random();
    
    // 测试开始时间
    protected long testStartTime;
    
    // 错误计数器
    protected AtomicInteger errorCount = new AtomicInteger();
    
    /**
     * 初始化测试环境
     */
    protected void initTestEnvironment() {
        // 创建HTTP客户端配置
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(CONNECTION_TIMEOUT)
                .setSocketTimeout(READ_TIMEOUT)
                .setConnectionRequestTimeout(REQUEST_TIMEOUT)
                .build();
        
        // 创建HTTP客户端
        httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .build();
        
        // 初始化线程池
        executorService = Executors.newFixedThreadPool(DEFAULT_CONCURRENT_USERS * 2);
        
        // 重置性能指标
        performanceMetrics.clear();
        errorCount.set(0);
    }
    
    /**
     * 清理测试环境
     */
    protected void cleanupTestEnvironment() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    /**
     * 执行并发测试
     * 
     * @param threadCount 线程数
     * @param requestCount 每个线程的请求次数
     * @param testRunner 测试执行器
     * @return 测试结果
     */
    protected PerformanceTestResult runConcurrentTest(int threadCount, int requestCount, TestRunner testRunner) {
        testStartTime = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicLong totalResponseTime = new AtomicLong();
        AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
        AtomicLong maxResponseTime = new AtomicLong(0);
        
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                for (int j = 0; j < requestCount; j++) {
                    long startTime = System.currentTimeMillis();
                    try {
                        testRunner.run();
                        long endTime = System.currentTimeMillis();
                        long responseTime = endTime - startTime;
                        
                        // 更新统计信息
                        totalResponseTime.addAndGet(responseTime);
                        updateMinMaxResponseTime(responseTime, minResponseTime, maxResponseTime);
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        System.err.println("测试请求执行失败: " + e.getMessage());
                    }
                }
                latch.countDown();
            });
        }
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("测试执行被中断: " + e.getMessage());
        }
        
        long testEndTime = System.currentTimeMillis();
        long totalTime = testEndTime - testStartTime;
        long totalRequests = (long) threadCount * requestCount;
        long totalSuccessRequests = totalRequests - errorCount.get();
        
        // 计算平均响应时间
        double avgResponseTime = totalRequests > 0 ? (double) totalResponseTime.get() / totalRequests : 0;
        
        // 计算成功率
        double successRate = totalRequests > 0 ? (double) totalSuccessRequests / totalRequests * 100 : 0;
        
        // 计算吞吐量（每秒请求数）
        double throughput = totalTime > 0 ? (double) totalRequests / (totalTime / 1000.0) : 0;
        
        return new PerformanceTestResult(
            threadCount,
            requestCount,
            totalRequests,
            totalSuccessRequests,
            errorCount.get(),
            totalTime,
            minResponseTime.get() == Long.MAX_VALUE ? 0 : minResponseTime.get(),
            maxResponseTime.get(),
            avgResponseTime,
            successRate,
            throughput
        );
    }
    
    /**
     * 更新最小和最大响应时间
     */
    private void updateMinMaxResponseTime(long responseTime, AtomicLong minResponseTime, AtomicLong maxResponseTime) {
        long currentMin = minResponseTime.get();
        while (responseTime < currentMin && !minResponseTime.compareAndSet(currentMin, responseTime)) {
            currentMin = minResponseTime.get();
        }
        
        long currentMax = maxResponseTime.get();
        while (responseTime > currentMax && !maxResponseTime.compareAndSet(currentMax, responseTime)) {
            currentMax = maxResponseTime.get();
        }
    }
    
    /**
     * 生成随机字符串
     */
    protected String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + random.nextInt(26)));
        }
        return sb.toString();
    }
    
    /**
     * 生成随机整数
     */
    protected int generateRandomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
    
    /**
     * 测试运行器接口
     */
    @FunctionalInterface
    public interface TestRunner {
        void run() throws Exception;
    }
    
    /**
     * 性能测试结果
     */
    public static class PerformanceTestResult {
        private int threadCount;
        private int requestCountPerThread;
        private long totalRequests;
        private long successRequests;
        private long errorRequests;
        private long totalTime;
        private long minResponseTime;
        private long maxResponseTime;
        private double avgResponseTime;
        private double successRate;
        private double throughput;
        
        public PerformanceTestResult(int threadCount, int requestCountPerThread, long totalRequests, 
                                   long successRequests, long errorRequests, long totalTime,
                                   long minResponseTime, long maxResponseTime, double avgResponseTime,
                                   double successRate, double throughput) {
            this.threadCount = threadCount;
            this.requestCountPerThread = requestCountPerThread;
            this.totalRequests = totalRequests;
            this.successRequests = successRequests;
            this.errorRequests = errorRequests;
            this.totalTime = totalTime;
            this.minResponseTime = minResponseTime;
            this.maxResponseTime = maxResponseTime;
            this.avgResponseTime = avgResponseTime;
            this.successRate = successRate;
            this.throughput = throughput;
        }
        
        public int getThreadCount() {
            return threadCount;
        }
        
        public int getRequestCountPerThread() {
            return requestCountPerThread;
        }
        
        public long getTotalRequests() {
            return totalRequests;
        }
        
        public long getSuccessRequests() {
            return successRequests;
        }
        
        public long getErrorRequests() {
            return errorRequests;
        }
        
        public long getTotalTime() {
            return totalTime;
        }
        
        public long getMinResponseTime() {
            return minResponseTime;
        }
        
        public long getMaxResponseTime() {
            return maxResponseTime;
        }
        
        public double getAvgResponseTime() {
            return avgResponseTime;
        }
        
        public double getSuccessRate() {
            return successRate;
        }
        
        public double getThroughput() {
            return throughput;
        }
        
        /**
         * 生成JSON格式的结果报告
         */
        public String toJson() {
            return String.format(
                "{\"threadCount\":%d,\"requestCountPerThread\":%d,\"totalRequests\":%d,\"successRequests\":%d," +
                "\"errorRequests\":%d,\"totalTime\":%d,\"minResponseTime\":%d,\"maxResponseTime\":%d," +
                "\"avgResponseTime\":%.2f,\"successRate\":%.2f,\"throughput\":%.2f}",
                threadCount, requestCountPerThread, totalRequests, successRequests, errorRequests,
                totalTime, minResponseTime, maxResponseTime, avgResponseTime, successRate, throughput
            );
        }
        
        /**
         * 生成性能报告文本
         */
        public String toReportString() {
            StringBuilder sb = new StringBuilder();
            sb.append("线程数: ").append(threadCount).append("\n");
            sb.append("每个线程请求数: ").append(requestCountPerThread).append("\n");
            sb.append("总请求数: ").append(totalRequests).append("\n");
            sb.append("成功请求数: ").append(successRequests).append("\n");
            sb.append("错误请求数: ").append(errorRequests).append("\n");
            sb.append("总耗时: ").append(totalTime).append("ms\n");
            sb.append("最小响应时间: ").append(minResponseTime).append("ms\n");
            sb.append("最大响应时间: ").append(maxResponseTime).append("ms\n");
            sb.append("平均响应时间: ").append(String.format("%.2f", avgResponseTime)).append("ms\n");
            sb.append("成功率: ").append(String.format("%.2f", successRate)).append("%\n");
            sb.append("吞吐量: ").append(String.format("%.2f", throughput)).append(" req/s\n");
            return sb.toString();
        }
    }
    
    /**
     * 性能指标类
     */
    public static class PerformanceMetric {
        private AtomicInteger requestCount = new AtomicInteger(0);
        private AtomicInteger successCount = new AtomicInteger(0);
        private AtomicInteger errorCount = new AtomicInteger(0);
        private AtomicLong totalResponseTime = new AtomicLong(0);
        private AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
        private AtomicLong maxResponseTime = new AtomicLong(0);
        
        public void recordRequest(boolean success, long responseTime) {
            requestCount.incrementAndGet();
            
            if (success) {
                successCount.incrementAndGet();
            } else {
                errorCount.incrementAndGet();
            }
            
            totalResponseTime.addAndGet(responseTime);
            
            // 更新最小响应时间
            long currentMin = minResponseTime.get();
            while (responseTime < currentMin && !minResponseTime.compareAndSet(currentMin, responseTime)) {
                currentMin = minResponseTime.get();
            }
            
            // 更新最大响应时间
            long currentMax = maxResponseTime.get();
            while (responseTime > currentMax && !maxResponseTime.compareAndSet(currentMax, responseTime)) {
                currentMax = maxResponseTime.get();
            }
        }
        
        public void reset() {
            requestCount.set(0);
            successCount.set(0);
            errorCount.set(0);
            totalResponseTime.set(0);
            minResponseTime.set(Long.MAX_VALUE);
            maxResponseTime.set(0);
        }
        
        public int getRequestCount() {
            return requestCount.get();
        }
        
        public int getSuccessCount() {
            return successCount.get();
        }
        
        public int getErrorCount() {
            return errorCount.get();
        }
        
        public double getAvgResponseTime() {
            int count = requestCount.get();
            return count > 0 ? (double) totalResponseTime.get() / count : 0;
        }
        
        public long getMinResponseTime() {
            return minResponseTime.get() == Long.MAX_VALUE ? 0 : minResponseTime.get();
        }
        
        public long getMaxResponseTime() {
            return maxResponseTime.get();
        }
    }
}