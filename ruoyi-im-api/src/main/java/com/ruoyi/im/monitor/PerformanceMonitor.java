package com.ruoyi.im.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * 性能监控工具类
 * 
 * 监控API接口的性能指标，包括响应时间、请求次数、成功率等
 * 
 * @author ruoyi
 */
public class PerformanceMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger("com.ruoyi.im.performance");
    
    /**
     * 存储各接口的性能数据
     */
    private static final Map<String, PerformanceData> PERFORMANCE_DATA_MAP = new ConcurrentHashMap<>();
    
    /**
     * 统计的开始时间
     */
    private static final long START_TIME = System.currentTimeMillis();
    
    /**
     * 性能数据
     */
    private static class PerformanceData {
        private final AtomicLong totalRequests = new AtomicLong();
        private final AtomicLong successRequests = new AtomicLong();
        private final AtomicLong errorRequests = new AtomicLong();
        private final AtomicLongArray responseTimes = new AtomicLongArray(4); // 0:avg, 1:min, 2:max, 3:last
        
        public PerformanceData() {
            responseTimes.set(0, 0); // 平均响应时间
            responseTimes.set(1, Long.MAX_VALUE); // 最小响应时间
            responseTimes.set(2, 0); // 最大响应时间
            responseTimes.set(3, 0); // 最后一次响应时间
        }
        
        public void addRequest(boolean success, long responseTime) {
            totalRequests.incrementAndGet();
            if (success) {
                successRequests.incrementAndGet();
            } else {
                errorRequests.incrementAndGet();
            }
            
            // 更新平均响应时间
            long total = totalRequests.get();
            long currentAvg = responseTimes.get(0);
            long newAvg = (currentAvg * (total - 1) + responseTime) / total;
            responseTimes.set(0, newAvg);
            
            // 更新最小响应时间
            if (responseTime < responseTimes.get(1)) {
                responseTimes.set(1, responseTime);
            }
            
            // 更新最大响应时间
            if (responseTime > responseTimes.get(2)) {
                responseTimes.set(2, responseTime);
            }
            
            // 设置最后一次响应时间
            responseTimes.set(3, responseTime);
        }
        
        public long getTotalRequests() {
            return totalRequests.get();
        }
        
        public long getSuccessRequests() {
            return successRequests.get();
        }
        
        public long getErrorRequests() {
            return errorRequests.get();
        }
        
        public long getSuccessRate() {
            long total = totalRequests.get();
            if (total == 0) {
                return 0;
            }
            return successRequests.get() * 100 / total;
        }
        
        public long getAvgResponseTime() {
            return responseTimes.get(0);
        }
        
        public long getMinResponseTime() {
            long min = responseTimes.get(1);
            return min == Long.MAX_VALUE ? 0 : min;
        }
        
        public long getMaxResponseTime() {
            return responseTimes.get(2);
        }
        
        public long getLastResponseTime() {
            return responseTimes.get(3);
        }
    }
    
    /**
     * 开始监控
     * 
     * @param uri 请求URI
     * @param method 请求方法
     * @return 监控对象
     */
    public static MonitorContext startMonitor(String uri, String method) {
        return new MonitorContext(uri, method);
    }
    
    /**
     * 监控上下文
     */
    public static class MonitorContext {
        private final String key;
        private final long startTime;
        
        private MonitorContext(String uri, String method) {
            this.key = uri + ":" + method;
            this.startTime = System.currentTimeMillis();
        }
        
        /**
         * 记录成功请求
         */
        public void success() {
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("请求完成: {}, 方法: {}, 耗时: {}ms, 状态: 成功", key.split(":")[0], key.split(":")[1], duration);
            
            PERFORMANCE_DATA_MAP.computeIfAbsent(key, k -> new PerformanceData()).addRequest(true, duration);
        }
        
        /**
         * 记录失败请求
         */
        public void error() {
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("请求完成: {}, 方法: {}, 耗时: {}ms, 状态: 失败", key.split(":")[0], key.split(":")[1], duration);
            
            PERFORMANCE_DATA_MAP.computeIfAbsent(key, k -> new PerformanceData()).addRequest(false, duration);
        }
    }
    
    /**
     * 获取指定接口的性能数据
     * 
     * @param uri 请求URI
     * @param method 请求方法
     * @return 性能数据
     */
    public static Map<String, Object> getPerformanceData(String uri, String method) {
        String key = uri + ":" + method;
        PerformanceData data = PERFORMANCE_DATA_MAP.get(key);
        
        if (data == null) {
            return new ConcurrentHashMap<>();
        }
        
        Map<String, Object> result = new ConcurrentHashMap<>();
        result.put("uri", uri);
        result.put("method", method);
        result.put("totalRequests", data.getTotalRequests());
        result.put("successRequests", data.getSuccessRequests());
        result.put("errorRequests", data.getErrorRequests());
        result.put("successRate", data.getSuccessRate() + "%");
        result.put("avgResponseTime", data.getAvgResponseTime() + "ms");
        result.put("minResponseTime", data.getMinResponseTime() + "ms");
        result.put("maxResponseTime", data.getMaxResponseTime() + "ms");
        result.put("lastResponseTime", data.getLastResponseTime() + "ms");
        
        return result;
    }
    
    /**
     * 获取所有接口的性能数据
     * 
     * @return 所有性能数据
     */
    public static Map<String, Map<String, Object>> getAllPerformanceData() {
        Map<String, Map<String, Object>> result = new ConcurrentHashMap<>();
        
        for (String key : PERFORMANCE_DATA_MAP.keySet()) {
            String[] parts = key.split(":");
            String uri = parts[0];
            String method = parts[1];
            
            result.put(key, getPerformanceData(uri, method));
        }
        
        return result;
    }
    
    /**
     * 清理所有性能数据
     */
    public static void clearPerformanceData() {
        PERFORMANCE_DATA_MAP.clear();
    }
}