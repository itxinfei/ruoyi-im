package com.ruoyi.im.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限控制注解
 * 
 * @author ruoyi
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    /**
     * 权限值
     */
    String value() default "";
    
    /**
     * 权限描述
     */
    String desc() default "";
    
    /**
     * 是否需要所有权限（AND逻辑）
     */
    boolean allRequired() default false;
}