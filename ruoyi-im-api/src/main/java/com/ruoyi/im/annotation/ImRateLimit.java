package com.ruoyi.im.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解
 * 
 * 用于标记需要进行限流控制的方法
 * 
 * @author ruoyi
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ImRateLimit {
    
    /**
     * 限流的key，用于标识不同的限流策略
     */
    String key() default "";
    
    /**
     * 单位时间内的请求次数限制
     */
    int count() default 10;
    
    /**
     * 限流时间窗口，单位秒
     */
    int time() default 60;
    
    /**
     * 限流算法类型
     */
    LimitAlgorithm algorithm() default LimitAlgorithm.FIXED_WINDOW;
    
    /**
     * 限流算法枚举
     */
    enum LimitAlgorithm {
        FIXED_WINDOW,     // 固定窗口
        SLIDING_WINDOW,   // 滑动窗口
        TOKEN_BUCKET,     // 令牌桶
        LEAKY_BUCKET      // 漏桶
    }
    
    /**
     * 限流失败时的处理策略
     */
    LimitStrategy strategy() default LimitStrategy.REJECT;
    
    /**
     * 限流失败处理策略枚举
     */
    enum LimitStrategy {
        REJECT,     // 拒绝请求
        QUEUE,      // 排队等待
        DEGRADE     // 降级处理
    }
    
    /**
     * 限流速率（每秒请求数）
     */
    double rate() default 1.0;
    
    /**
     * 限流窗口大小（秒）
     */
    int window() default 60;
    
    /**
     * 限流失败时的消息
     */
    String message() default "请求过于频繁，请稍后重试";
    
    /**
     * 限流组
     */
    String group() default "";
    
    /**
     * 延迟时间（毫秒）
     */
    int delay() default 0;
    
    /**
     * 是否启用限流
     */
    boolean enabled() default true;
}