package com.ruoyi.im.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 慢查询检测切面
 * 监控方法执行时间，对超过阈值的方法进行记录
 *
 * @author ruoyi
 */
@Aspect
@Component
public class SlowQueryAspect {

    private static final Logger logger = LoggerFactory.getLogger(SlowQueryAspect.class);
    
    // 慢查询阈值（毫秒）
    private static final long SLOW_QUERY_THRESHOLD = 500L;

    /**
     * 拦截Mapper层方法，检测慢查询
     */
    @Around("execution(* com.ruoyi.im.mapper..*(..))")
    public Object detectSlowQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // 如果执行时间超过阈值，记录警告日志
            if (duration > SLOW_QUERY_THRESHOLD) {
                logger.warn("慢查询检测: 方法={}, 执行时间={}ms", methodName, duration);
            }
            
            return result;
        } catch (Throwable throwable) {
            long duration = System.currentTimeMillis() - startTime;
            if (duration > SLOW_QUERY_THRESHOLD) {
                logger.warn("慢查询检测(异常): 方法={}, 执行时间={}ms, 异常={}", 
                           methodName, duration, throwable.getMessage());
            }
            throw throwable;
        }
    }
}