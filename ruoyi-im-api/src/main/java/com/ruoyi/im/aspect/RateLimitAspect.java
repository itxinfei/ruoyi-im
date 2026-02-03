package com.ruoyi.im.aspect;

import com.ruoyi.im.annotation.RateLimit;
import com.ruoyi.im.constant.RedisKeyConstants;
import com.ruoyi.im.exception.RateLimitException;
import com.ruoyi.im.util.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 限流切面
 * 基于 Redis + AOP 实现接口限流
 * 使用令牌桶算法的简化版本（固定窗口计数）
 *
 * @author ruoyi
 */
@Aspect
@Component
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 前置通知：拦截带 @RateLimit 注解的方法
     */
    @Before("@annotation(rateLimit)")
    public void doBefore(JoinPoint joinPoint, RateLimit rateLimit) {
        // 构建限流key
        String limitKey = buildLimitKey(joinPoint, rateLimit);

        // 获取限流配置
        int time = rateLimit.time();
        int count = rateLimit.count();

        // 执行限流检查
        checkRateLimit(limitKey, time, count);
    }

    /**
     * 构建限流key
     * 根据限流类型和注解配置生成唯一的Redis key
     *
     * @param joinPoint 切点
     * @param rateLimit 限流注解
     * @return 限流key
     */
    private String buildLimitKey(JoinPoint joinPoint, RateLimit rateLimit) {
        StringBuilder keyBuilder = new StringBuilder(RedisKeyConstants.RATE_LIMIT_PREFIX);

        // 如果指定了自定义key，使用自定义key
        if (rateLimit.key() != null && !rateLimit.key().isEmpty()) {
            keyBuilder.append(rateLimit.key());
        } else {
            // 默认使用类名+方法名
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            keyBuilder.append(className).append(":").append(methodName);
        }

        // 根据限流类型添加标识
        switch (rateLimit.limitType()) {
            case USER:
                // 按用户ID限流
                try {
                    Long userId = SecurityUtils.getCurrentUserId();
                    keyBuilder.append(":user:").append(userId);
                } catch (Exception e) {
                    // 未登录用户使用IP
                    String ip = getClientIp();
                    keyBuilder.append(":ip:").append(ip);
                }
                break;

            case IP:
                // 按IP限流
                String ip = getClientIp();
                keyBuilder.append(":ip:").append(ip);
                break;

            case GLOBAL:
                // 全局限流，不需要额外标识
                break;

            case DEFAULT:
            default:
                // 默认：按IP+方法限流
                String defaultIp = getClientIp();
                keyBuilder.append(":ip:").append(defaultIp);
                break;
        }

        return keyBuilder.toString();
    }

    /**
     * 获取客户端IP地址
     * 支持代理和负载均衡环境下的真实IP获取
     *
     * @return 客户端IP
     */
    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }

        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多级代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        // IPv6本地地址转换
        if (ip != null && "0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }

        return ip != null ? ip : "unknown";
    }

    /**
     * 检查并执行限流
     * 使用 Redis 的原子操作实现计数器
     *
     * @param key 限流key
     * @param time 时间窗口（秒）
     * @param count 最大请求次数
     */
    private void checkRateLimit(String key, int time, int count) {
        // 使用 RedisAtomicLong 进行原子操作
        RedisAtomicLong atomicLong = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());

        // 获取当前计数
        long current = atomicLong.get();

        if (current == 0) {
            // 首次访问，设置初始值和过期时间
            atomicLong.set(1);
            atomicLong.expire(time, TimeUnit.SECONDS);
        } else if (current < count) {
            // 未超限，计数+1
            atomicLong.incrementAndGet();
        } else {
            // 已超限，抛出限流异常
            log.warn("接口限流触发: key={}, limit={}, current={}", key, count, current);
            throw new RateLimitException(key, count);
        }
    }
}
