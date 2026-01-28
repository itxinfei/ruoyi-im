package com.ruoyi.im.aspect;

import com.ruoyi.im.util.MetricsUtil;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;

/**
 * 性能监控切面
 * 自动记录Controller方法的执行时间
 *
 * @author ruoyi
 */
@Aspect
@Component
public class PerformanceMonitorAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceMonitorAspect.class);

    @Autowired
    private MetricsUtil metricsUtil;

    /**
     * 拦截所有Controller方法
     */
    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public Object monitorController(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        // 获取请求信息
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        String httpMethod = getHttpMethod(method);

        // 构建监控指标名称
        String metricName = "http.server.requests";
        String endpoint = getEndpoint(method);

        // 开始计时
        Timer.Sample sample = metricsUtil.startTimer(metricName);

        try {
            // 执行方法
            Object result = point.proceed();

            // 记录成功请求
            metricsUtil.stopTimer(sample, metricName,
                    "method", httpMethod,
                    "uri", endpoint,
                    "status", "200",
                    "exception", "none");

            log.debug("Controller方法执行成功: {}.{}", className, methodName);

            return result;
        } catch (Exception e) {
            // 记录失败请求
            metricsUtil.stopTimer(sample, metricName,
                    "method", httpMethod,
                    "uri", endpoint,
                    "status", "500",
                    "exception", e.getClass().getSimpleName());

            log.error("Controller方法执行异常: {}.{} - {}", className, methodName, e.getMessage());

            throw e;
        }
    }

    /**
     * 获取HTTP方法类型
     */
    private String getHttpMethod(Method method) {
        if (method.isAnnotationPresent(GetMapping.class)) {
            return "GET";
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            return "POST";
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            return "PUT";
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            return "DELETE";
        } else if (method.isAnnotationPresent(PatchMapping.class)) {
            return "PATCH";
        } else {
            return "UNKNOWN";
        }
    }

    /**
     * 获取请求端点
     */
    private String getEndpoint(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        RequestMapping classMapping = declaringClass.getAnnotation(RequestMapping.class);

        String basePath = "";
        if (classMapping != null && classMapping.value().length > 0) {
            basePath = classMapping.value()[0];
        }

        String methodPath = "";
        if (method.isAnnotationPresent(GetMapping.class)) {
            methodPath = getFirstPath(method.getAnnotation(GetMapping.class).value());
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            methodPath = getFirstPath(method.getAnnotation(PostMapping.class).value());
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            methodPath = getFirstPath(method.getAnnotation(PutMapping.class).value());
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            methodPath = getFirstPath(method.getAnnotation(DeleteMapping.class).value());
        } else if (method.isAnnotationPresent(PatchMapping.class)) {
            methodPath = getFirstPath(method.getAnnotation(PatchMapping.class).value());
        }

        return basePath + methodPath;
    }

    /**
     * 获取第一个路径值
     */
    private String getFirstPath(String[] paths) {
        if (paths != null && paths.length > 0) {
            return paths[0];
        }
        return "";
    }
}