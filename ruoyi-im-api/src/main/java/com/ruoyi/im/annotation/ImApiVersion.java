package com.ruoyi.im.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * IM API版本控制注解
 * 
 * 用于标识控制器或方法支持的API版本
 * 可以指定最小版本号、最大版本号，以及版本兼容性说明
 * 
 * @author ruoyi
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ImApiVersion {

    /**
     * 支持的版本列表
     * 
     * @return 支持的版本号数组，如 {"v1", "v2"}
     */
    String[] value() default {};

    /**
     * 最小支持版本
     * 
     * @return 最小版本号，如 "v1"
     */
    String min() default "";

    /**
     * 最大支持版本
     * 
     * @return 最大版本号，如 "v3"
     */
    String max() default "";

    /**
     * 版本兼容性说明
     * 
     * @return 版本兼容性描述
     */
    String description() default "";
}