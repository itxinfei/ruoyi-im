package com.ruoyi.im.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API版本控制注解
 * 用于标识API接口的版本
 *
 * @author ruoyi
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVersion {

    /**
     * 版本号，默认为v1
     */
    String value() default "v1";
}