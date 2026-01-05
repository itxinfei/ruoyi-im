package com.ruoyi.im.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Swagger标签注解，用于标识API接口
 * 
 * @author ruoyi
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SwaggerTag {
    String value() default "";
    String description() default "";
}