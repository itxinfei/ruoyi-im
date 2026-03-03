package com.ruoyi.im.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BusinessLog
{
    String module() default "";

    String operation() default "";

    String action() default "";

    String content() default "";

    boolean saveParams() default true;

    boolean saveResult() default false;

    String[] excludeParams() default {};

    long slowThreshold() default 1000;
}
