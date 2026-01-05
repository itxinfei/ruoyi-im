package com.ruoyi.im.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 性能监控注解
 * 
 * 用于标记需要监控性能的API接口，自动记录接口的响应时间、
 * 吞吐量、错误率等关键性能指标。
 * 
 * @author ruoyi
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ImPerformanceMonitor {
    
    /**
     * 监控标签
     * 
     * @return 监控标签，用于区分不同的监控场景
     */
    String value() default "";
    
    /**
     * 是否记录详细日志
     * 
     * @return true表示记录详细性能日志，false表示只记录关键指标
     */
    boolean detailed() default false;
    
    /**
     * 性能阈值（毫秒）
     * 
     * @return 性能阈值，当响应时间超过此值时记录告警日志
     */
    long threshold() default 1000;
    
    /**
     * 监控分组
     * 
     * @return 监控分组，用于分类管理不同模块的性能数据
     */
    String group() default "default";
}