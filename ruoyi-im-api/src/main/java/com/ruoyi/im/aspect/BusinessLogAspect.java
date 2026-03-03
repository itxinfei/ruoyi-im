package com.ruoyi.im.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.ruoyi.im.annotation.BusinessLog;
import com.ruoyi.im.util.TraceIdUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class BusinessLogAspect
{
    private static final Logger BIZ_LOG = LoggerFactory.getLogger("com.ruoyi.im.business");
    private static final Logger PERF_LOG = LoggerFactory.getLogger("com.ruoyi.im.performance");

    @Pointcut("@annotation(com.ruoyi.im.annotation.BusinessLog)")
    public void businessLogPointcut()
    {
    }

    @Around("businessLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable
    {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        BusinessLog businessLog = method.getAnnotation(BusinessLog.class);

        long startTime = System.currentTimeMillis();
        String traceId = TraceIdUtil.getTraceId();
        String userId = TraceIdUtil.getUserId();
        String ipAddr = TraceIdUtil.getIpAddr();

        String module = businessLog.module();
        String operation = businessLog.operation();
        String action = businessLog.action();
        String content = businessLog.content();

        Map<String, Object> params = new HashMap<>();
        if (businessLog.saveParams())
        {
            params = extractParams(joinPoint, businessLog.excludeParams());
        }

        BIZ_LOG.info("[BIZ] traceId={} module={} operation={} action={} content={} userId={} ip={} params={}",
                traceId, module, operation, action, content, userId, ipAddr, params);

        Object result = null;
        boolean success = true;
        String errorMsg = null;

        try
        {
            result = joinPoint.proceed();
            return result;
        }
        catch (Exception e)
        {
            success = false;
            errorMsg = e.getMessage();
            throw e;
        }
        finally
        {
            long duration = System.currentTimeMillis() - startTime;
            long threshold = businessLog.slowThreshold();

            if (duration > threshold)
            {
                PERF_LOG.warn("[PERF-SLOW] module={} operation={} duration={}ms threshold={}ms",
                        module, operation, duration, threshold);
            }
            else
            {
                PERF_LOG.info("[PERF] module={} operation={} duration={}ms", module, operation, duration);
            }

            if (businessLog.saveResult())
            {
                BIZ_LOG.info("[BIZ-RESULT] traceId={} module={} operation={} success={} duration={}ms result={} error={}",
                        traceId, module, operation, success, duration,
                        success ? (result != null ? truncate(result.toString(), 500) : "null") : "null",
                        errorMsg);
            }
            else
            {
                BIZ_LOG.info("[BIZ] traceId={} module={} operation={} success={} duration={}ms",
                        traceId, module, operation, success, duration);
            }
        }
    }

    private Map<String, Object> extractParams(ProceedingJoinPoint joinPoint, String[] excludeParams)
    {
        Map<String, Object> params = new HashMap<>();

        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();

        if (paramNames != null && args != null)
        {
            for (int i = 0; i < paramNames.length; i++)
            {
                String paramName = paramNames[i];
                Object arg = args[i];

                if (isExcluded(paramName, excludeParams))
                {
                    continue;
                }

                if (arg == null)
                {
                    params.put(paramName, null);
                }
                else if (isBasicType(arg))
                {
                    params.put(paramName, arg);
                }
                else if (arg instanceof javax.servlet.http.HttpServletRequest)
                {
                    HttpServletRequest request = (HttpServletRequest) arg;
                    params.put("requestURI", request.getRequestURI());
                    params.put("queryString", request.getQueryString());
                }
                else if (arg instanceof javax.servlet.http.HttpServletResponse)
                {
                }
                else
                {
                    try
                    {
                        String str = arg.toString();
                        params.put(paramName, truncate(str, 200));
                    }
                    catch (Exception e)
                    {
                        params.put(paramName, arg.getClass().getSimpleName());
                    }
                }
            }
        }

        return params;
    }

    private boolean isExcluded(String paramName, String[] excludeParams)
    {
        if (excludeParams == null || excludeParams.length == 0)
        {
            return false;
        }
        return Arrays.asList(excludeParams).contains(paramName);
    }

    private boolean isBasicType(Object obj)
    {
        return obj instanceof String
                || obj instanceof Number
                || obj instanceof Boolean
                || obj instanceof Character
                || obj instanceof java.util.Date;
    }

    private String truncate(String str, int maxLength)
    {
        if (str == null)
        {
            return null;
        }
        return str.length() <= maxLength ? str : str.substring(0, maxLength) + "...";
    }
}
