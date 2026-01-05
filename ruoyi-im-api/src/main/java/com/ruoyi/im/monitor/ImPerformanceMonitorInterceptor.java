package com.ruoyi.im.monitor;

import com.ruoyi.im.annotation.ImPerformanceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能监控拦截器
 * 
 * 用于拦截API请求，自动记录性能指标数据，包括响应时间、
 * 吞吐量、错误率等关键性能数据。
 * 
 * @author ruoyi
 */
@Component
public class ImPerformanceMonitorInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ImPerformanceMonitorInterceptor.class);
    
    /**
     * 性能统计数据缓存
     * key: 接口标识，value: 性能统计数据
     */
    private final ConcurrentHashMap<String, PerformanceStats> statsCache = new ConcurrentHashMap<>();
    
    /**
     * 请求开始时间线程本地变量
     */
    private static final ThreadLocal<Long> REQUEST_START_TIME = new ThreadLocal<>();
    
    /**
     * 请求标识线程本地变量
     */
    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        ImPerformanceMonitor monitor = getPerformanceMonitor(handlerMethod);
        
        if (monitor == null) {
            return true;
        }
        
        // 记录请求开始时间
        REQUEST_START_TIME.set(System.currentTimeMillis());
        REQUEST_ID.set(generateRequestId());
        
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        ImPerformanceMonitor monitor = getPerformanceMonitor(handlerMethod);
        
        if (monitor == null) {
            return;
        }
        
        // 计算响应时间
        long startTime = REQUEST_START_TIME.get();
        if (startTime > 0) {
            long responseTime = System.currentTimeMillis() - startTime;
            
            // 更新统计数据
            updateStats(monitor, responseTime, response.getStatus(), true, handlerMethod);
            
            // 记录详细日志
            if (monitor.detailed() || responseTime > monitor.threshold()) {
                logPerformanceLog(monitor, responseTime, response.getStatus(), request, handlerMethod);
            }
        }
        
        // 清理线程本地变量
        REQUEST_START_TIME.remove();
        REQUEST_ID.remove();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        ImPerformanceMonitor monitor = getPerformanceMonitor(handlerMethod);
        
        if (monitor == null) {
            return;
        }
        
        // 如果有异常，记录异常信息
        if (ex != null) {
            long startTime = REQUEST_START_TIME.get();
            if (startTime > 0) {
                long responseTime = System.currentTimeMillis() - startTime;
                updateStats(monitor, responseTime, 500, false, handlerMethod);
                
                log.error("接口性能监控 - 发生异常: {}, 接口: {}, 响应时间: {}ms, 异常信息: {}", 
                    REQUEST_ID.get(), getHandlerKey(handlerMethod), responseTime, ex.getMessage(), ex);
            }
        }
    }
    
    /**
     * 获取性能监控注解
     */
    private ImPerformanceMonitor getPerformanceMonitor(HandlerMethod handlerMethod) {
        // 先检查方法上的注解
        ImPerformanceMonitor monitor = handlerMethod.getMethodAnnotation(ImPerformanceMonitor.class);
        if (monitor != null) {
            return monitor;
        }
        
        // 再检查类上的注解
        return handlerMethod.getBeanType().getAnnotation(ImPerformanceMonitor.class);
    }
    
    /**
     * 生成请求ID
     */
    private String generateRequestId() {
        return "req_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    /**
     * 获取接口标识
     */
    private String getHandlerKey(HandlerMethod handlerMethod) {
        return handlerMethod.getBeanType().getSimpleName() + "." + handlerMethod.getMethod().getName();
    }
    
    /**
     * 更新性能统计数据
     */
    private void updateStats(ImPerformanceMonitor monitor, long responseTime, int statusCode, boolean success, HandlerMethod handlerMethod) {
        String key = monitor.group() + ":" + getHandlerKey(handlerMethod);
        
        PerformanceStats stats = statsCache.computeIfAbsent(key, k -> new PerformanceStats());
        stats.update(responseTime, statusCode, success);
        
        // 检查是否需要告警
        if (responseTime > monitor.threshold()) {
            log.warn("性能监控告警 - 接口: {}, 响应时间: {}ms, 阈值: {}ms", 
                key, responseTime, monitor.threshold());
        }
    }
    
    /**
     * 记录性能日志
     */
    private void logPerformanceLog(ImPerformanceMonitor monitor, long responseTime, int statusCode, HttpServletRequest request, HandlerMethod handlerMethod) {
        log.info("性能监控日志 - 请求ID: {}, 接口: {}, 响应时间: {}ms, 状态码: {}, URI: {}, User-Agent: {}", 
            REQUEST_ID.get(), getHandlerKey(handlerMethod), responseTime, statusCode, request.getRequestURI(), request.getHeader("User-Agent"));
    }
    
    /**
     * 获取性能统计数据
     */
    public PerformanceStats getStats(String key) {
        return statsCache.get(key);
    }
    
    /**
     * 获取所有统计数据
     */
    public ConcurrentHashMap<String, PerformanceStats> getAllStats() {
        return new ConcurrentHashMap<>(statsCache);
    }
    
    /**
     * 清空统计数据
     */
    public void clearStats() {
        statsCache.clear();
    }
    
    /**
     * 性能统计数据类
     */
    public static class PerformanceStats {
        private final AtomicLong totalRequests = new AtomicLong(0);
        private final AtomicLong successRequests = new AtomicLong(0);
        private final AtomicLong errorRequests = new AtomicLong(0);
        private final AtomicLong totalResponseTime = new AtomicLong(0);
        private final AtomicLong maxResponseTime = new AtomicLong(0);
        private final AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
        
        public void update(long responseTime, int statusCode, boolean success) {
            totalRequests.incrementAndGet();
            totalResponseTime.addAndGet(responseTime);
            
            if (success) {
                successRequests.incrementAndGet();
            } else {
                errorRequests.incrementAndGet();
            }
            
            // 更新最大响应时间
            maxResponseTime.updateAndGet(current -> Math.max(current, responseTime));
            
            // 更新最小响应时间
            minResponseTime.updateAndGet(current -> Math.min(current, responseTime));
        }
        
        // Getter方法
        public long getTotalRequests() {
            return totalRequests.get();
        }
        
        public long getSuccessRequests() {
            return successRequests.get();
        }
        
        public long getErrorRequests() {
            return errorRequests.get();
        }
        
        public long getTotalResponseTime() {
            return totalResponseTime.get();
        }
        
        public long getMaxResponseTime() {
            return maxResponseTime.get();
        }
        
        public long getMinResponseTime() {
            return minResponseTime.get() == Long.MAX_VALUE ? 0 : minResponseTime.get();
        }
        
        public double getAverageResponseTime() {
            long total = getTotalRequests();
            return total > 0 ? (double) getTotalResponseTime() / total : 0;
        }
        
        public double getSuccessRate() {
            long total = getTotalRequests();
            return total > 0 ? (double) getSuccessRequests() / total * 100 : 0;
        }
        
        public double getErrorRate() {
            long total = getTotalRequests();
            return total > 0 ? (double) getErrorRequests() / total * 100 : 0;
        }
    }
}