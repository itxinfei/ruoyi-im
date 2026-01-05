package com.ruoyi.im.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解
 * 
 * 用于限制API接口的访问频率，防止接口被恶意调用或过载。
 * 支持多种限流算法：令牌桶算法、固定窗口、滑动窗口等。
 * 
 * @author ruoyi
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ImRateLimit {
    
    /**
     * 限流key标识
     * 
     * @return 限流标识，用于区分不同的限流规则
     */
    String key() default "";
    
    /**
     * 限流数量（每秒钟允许的请求数）
     * 
     * @return 每秒最大请求数
     */
    int rate() default 10;
    
    /**
     * 限流窗口时间（秒）
     * 
     * @return 限流窗口时间，默认1秒
     */
    int window() default 1;
    
    /**
     * 限流算法类型
     * 
     * @return 限流算法：TOKEN_BUCKET（令牌桶）、FIXED_WINDOW（固定窗口）、SLIDING_WINDOW（滑动窗口）
     */
    LimitAlgorithm algorithm() default LimitAlgorithm.TOKEN_BUCKET;
    
    /**
     * 超限后的提示消息
     * 
     * @return 超限提示消息
     */
    String message() default "请求过于频繁，请稍后再试";
    
    /**
     * 是否启用限流
     * 
     * @限流，falsereturn true启用禁用限流
     */
    boolean enabled() default true;
    
    /**
     * 限流分组
     * 
     * @return 限流分组，用于区分不同模块的限流策略
     */
    String group() default "default";
    
    /**
     * 限流算法枚举
     */
    enum LimitAlgorithm {
        /**
         * 令牌桶算法
         */
        TOKEN_BUCKET,
        
        /**
         * 固定窗口算法
         */
        FIXED_WINDOW,
        
        /**
         * 滑动窗口算法
         */
        SLIDING_WINDOW
    }
}