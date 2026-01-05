package com.ruoyi.im.rateLimit;

import com.ruoyi.im.utils.ServletUtils;
import com.ruoyi.im.annotation.ImRateLimit;
import com.ruoyi.im.util.PerformanceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 限流拦截器
 * 
 * @author ruoyi
 */
@Component
public class ImRateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ImRateLimitInterceptor.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            
            // 检查方法上是否有@ImRateLimit注解
            if (method.isAnnotationPresent(ImRateLimit.class)) {
                ImRateLimit rateLimit = method.getAnnotation(ImRateLimit.class);
                
                // 构建限流键
                String key = buildRateLimitKey(request, rateLimit);
                
                // 获取当前请求次数
                Long currentCount = (Long) redisTemplate.opsForValue().get(key);
                if (currentCount == null) {
                    // 首次请求，设置计数和过期时间
                    redisTemplate.opsForValue().set(key, 1L, rateLimit.time(), TimeUnit.SECONDS);
                } else if (currentCount < rateLimit.count()) {
                    // 增加计数
                    redisTemplate.opsForValue().increment(key);
                } else {
                    // 超过限流次数，返回429状态码
                    response.setStatus(429); // 使用硬编码状态码，因为ServletResponse.SC_TOO_MANY_REQUESTS可能不存在
                    response.getWriter().write("请求过于频繁，请稍后重试");
                    return false;
                }
            }
        }
        
        return true;
    }

    /**
     * 构建限流键
     * 
     * @param request HTTP请求
     * @param rateLimit 限流注解
     * @return 限流键
     */
    private String buildRateLimitKey(HttpServletRequest request, ImRateLimit rateLimit) {
        String ip = ServletUtils.getClientIpAddress(request);
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // 使用IP+URI+方法名作为限流键
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("rate_limit:")
                  .append(rateLimit.key().isEmpty() ? (method + ":" + uri) : rateLimit.key())
                  .append(":")
                  .append(ip);
                  
        return keyBuilder.toString();
    }
    
    /**
     * 清除统计信息
     */
    public void clearStats() {
        // 实现清除统计信息的逻辑
        // 可以清除相关的Redis统计键
        log.info("清除限流统计信息");
    }
}