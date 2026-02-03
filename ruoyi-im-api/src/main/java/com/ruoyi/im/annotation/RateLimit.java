package com.ruoyi.im.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 * 使用 Redis 实现分布式限流，基于令牌桶算法
 *
 * @author ruoyi
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流key的前缀，默认使用方法全路径名
     * 可根据业务定制，如："send_message"、"login"等
     */
    String key() default "";

    /**
     * 限流时间窗口（秒）
     * 默认60秒
     */
    int time() default 60;

    /**
     * 时间窗口内最大请求次数
     * 默认100次
     */
    int count() default 100;

    /**
     * 限流类型
     * DEFAULT: 默认，按IP+方法限流
     * USER: 按用户ID限流（需登录）
     * IP: 按IP限流
     * GLOBAL: 全局限流
     */
    LimitType limitType() default LimitType.DEFAULT;

    /**
     * 限流类型枚举
     */
    enum LimitType {
        /**
         * 默认策略，按IP+方法限流
         */
        DEFAULT,

        /**
         * 按用户ID限流
         */
        USER,

        /**
         * 按IP限流
         */
        IP,

        /**
         * 全局限流
         */
        GLOBAL
    }
}
