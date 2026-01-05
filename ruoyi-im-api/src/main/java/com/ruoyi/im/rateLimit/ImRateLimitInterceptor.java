package com.ruoyi.im.rateLimit;

import com.ruoyi.im.annotation.ImRateLimit;
import com.ruoyi.im.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 限流拦截器
 * 
 * 用于拦截API请求，执行限流检查，根据配置的限流策略和参数
 * 控制接口的访问频率，防止接口被恶意调用或过载。
 * 
 * @author ruoyi
 */
@Component
public class ImRateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ImRateLimitInterceptor.class);
    
    /**
     * 限流策略管理器
     */
    private final ImRateLimitManager rateLimitManager;
    
    /**
     * 限流统计数据缓存
     */
    private final ConcurrentHashMap<String, RateLimitStats> statsCache = new ConcurrentHashMap<>();

    public ImRateLimitInterceptor(ImRateLimitManager rateLimitManager) {
        this.rateLimitManager = rateLimitManager;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        ImRateLimit rateLimit = getRateLimit(handlerMethod);
        
        // 如果没有配置限流注解，直接放行
        if (rateLimit == null || !rateLimit.enabled()) {
            return true;
        }
        
        // 生成限流key
        String rateLimitKey = generateRateLimitKey(request, rateLimit);
        
        // 执行限流检查
        boolean allowed = rateLimitManager.checkLimit(rateLimitKey, rateLimit);
        
        if (!allowed) {
            // 限流触发，记录日志并返回错误响应
            log.warn("限流触发 - Key: {}, 接口: {}, 策略: {}, 速率: {}/秒, 窗口: {}秒", 
                rateLimitKey, getHandlerKey(handlerMethod), 
                rateLimit.algorithm().name(), rateLimit.rate(), rateLimit.window());
            
            // 更新统计数据
            updateStats(rateLimitKey, rateLimit, false);
            
            // 返回限流响应
            handleRateLimitExceeded(response, rateLimit);
            return false;
        }
        
        // 限流通过，更新统计数据
        updateStats(rateLimitKey, rateLimit, true);
        
        return true;
    }
    
    /**
     * 获取限流注解
     */
    private ImRateLimit getRateLimit(HandlerMethod handlerMethod) {
        // 先检查方法上的注解
        ImRateLimit rateLimit = handlerMethod.getMethodAnnotation(ImRateLimit.class);
        if (rateLimit != null) {
            return rateLimit;
        }
        
        // 再检查类上的注解
        return handlerMethod.getBeanType().getAnnotation(ImRateLimit.class);
    }
    
    /**
     * 生成限流key
     */
    private String generateRateLimitKey(HttpServletRequest request, ImRateLimit rateLimit) {
        StringBuilder keyBuilder = new StringBuilder();
        
        // 添加分组信息
        keyBuilder.append(rateLimit.group()).append(":");
        
        // 如果指定了key，使用指定的key
        if (!rateLimit.key().isEmpty()) {
            keyBuilder.append(rateLimit.key());
        } else {
            // 否则根据请求信息生成key
            keyBuilder.append(request.getRequestURI());
            
            // 可以根据需要添加更多标识信息，如用户ID、IP地址等
            String userId = getUserIdFromRequest(request);
            if (userId != null) {
                keyBuilder.append(":user:").append(userId);
            } else {
                String clientIp = getClientIpAddress(request);
                keyBuilder.append(":ip:").append(clientIp);
            }
        }
        
        return keyBuilder.toString();
    }
    
    /**
     * 从请求中获取用户ID
     */
    private String getUserIdFromRequest(HttpServletRequest request) {
        // 这里应该从JWT token或session中获取用户ID
        // 简化实现，返回null
        return null;
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * 获取接口标识
     */
    private String getHandlerKey(HandlerMethod handlerMethod) {
        return handlerMethod.getBeanType().getSimpleName() + "." + handlerMethod.getMethod().getName();
    }
    
    /**
     * 处理限流超限情况
     */
    private void handleRateLimitExceeded(HttpServletResponse response, ImRateLimit rateLimit) throws IOException {
        response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
        response.setContentType("application/json;charset=UTF-8");
        
        String errorMessage = String.format("{\"code\":429,\"message\":\"%s\",\"data\":null}", rateLimit.message());
        response.getWriter().write(errorMessage);
    }
    
    /**
     * 更新限流统计数据
     */
    private void updateStats(String rateLimitKey, ImRateLimit rateLimit, boolean allowed) {
        String statsKey = rateLimit.group() + ":" + rateLimitKey;
        
        RateLimitStats stats = statsCache.computeIfAbsent(statsKey, k -> new RateLimitStats());
        stats.update(allowed);
        
        // 可以在这里添加更详细的统计逻辑，比如按时间窗口统计等
    }
    
    /**
     * 获取限流统计数据
     */
    public RateLimitStats getStats(String key) {
        return statsCache.get(key);
    }
    
    /**
     * 获取所有统计数据
     */
    public ConcurrentHashMap<String, RateLimitStats> getAllStats() {
        return new ConcurrentHashMap<>(statsCache);
    }
    
    /**
     * 清空统计数据
     */
    public void clearStats() {
        statsCache.clear();
    }
    
    /**
     * 限流统计数据类
     */
    public static class RateLimitStats {
        private final AtomicLong totalRequests = new AtomicLong(0);
        private final AtomicLong allowedRequests = new AtomicLong(0);
        private final AtomicLong blockedRequests = new AtomicLong(0);
        
        public void update(boolean allowed) {
            totalRequests.incrementAndGet();
            if (allowed) {
                allowedRequests.incrementAndGet();
            } else {
                blockedRequests.incrementAndGet();
            }
        }
        
        // Getter方法
        public long getTotalRequests() {
            return totalRequests.get();
        }
        
        public long getAllowedRequests() {
            return allowedRequests.get();
        }
        
        public long getBlockedRequests() {
            return blockedRequests.get();
        }
        
        public double getBlockRate() {
            long total = getTotalRequests();
            return total > 0 ? (double) getBlockedRequests() / total * 100 : 0;
        }
        
        public double getAllowRate() {
            long total = getTotalRequests();
            return total > 0 ? (double) getAllowedRequests() / total * 100 : 0;
        }
    }
}