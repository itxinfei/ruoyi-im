package com.ruoyi.im.util;

import org.slf4j.MDC;
import cn.hutool.core.lang.UUID;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TraceIdUtil
{
    public static final String TRACE_ID = "traceId";
    public static final String REQUEST_ID = "requestId";
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";
    public static final String IP_ADDR = "ipAddr";

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    public static String getTraceId()
    {
        return MDC.get(TRACE_ID);
    }

    public static String getRequestId()
    {
        return MDC.get(REQUEST_ID);
    }

    public static String getUserId()
    {
        return MDC.get(USER_ID);
    }

    public static String getUserName()
    {
        return MDC.get(USER_NAME);
    }

    public static String getIpAddr()
    {
        return MDC.get(IP_ADDR);
    }

    public static void setTraceId(String traceId)
    {
        if (traceId != null && !traceId.isEmpty())
        {
            MDC.put(TRACE_ID, traceId);
        }
        else
        {
            MDC.put(TRACE_ID, UUID.fastUUID().toString(true));
        }
    }

    public static void setRequestId(String requestId)
    {
        if (requestId != null && !requestId.isEmpty())
        {
            MDC.put(REQUEST_ID, requestId);
        }
        else
        {
            MDC.put(REQUEST_ID, UUID.fastUUID().toString(true));
        }
    }

    public static void setUserId(String userId)
    {
        if (userId != null)
        {
            MDC.put(USER_ID, userId);
        }
    }

    public static void setUserName(String userName)
    {
        if (userName != null)
        {
            MDC.put(USER_NAME, userName);
        }
    }

    public static void setIpAddr(String ipAddr)
    {
        if (ipAddr != null)
        {
            MDC.put(IP_ADDR, ipAddr);
        }
    }

    public static void set(Map<String, String> context)
    {
        if (context != null)
        {
            for (Map.Entry<String, String> entry : context.entrySet())
            {
                if (entry.getValue() != null)
                {
                    MDC.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public static void clear()
    {
        MDC.remove(TRACE_ID);
        MDC.remove(REQUEST_ID);
        MDC.remove(USER_ID);
        MDC.remove(USER_NAME);
        MDC.remove(IP_ADDR);
    }

    public static void clearAll()
    {
        MDC.clear();
    }

    public static <T> Callable<T> wrap(Callable<T> task)
    {
        String traceId = getTraceId();
        String requestId = getRequestId();
        String userId = getUserId();
        String userName = getUserName();
        String ipAddr = MDC.get(IP_ADDR);

        return () ->
        {
            try
            {
                if (traceId != null) MDC.put(TRACE_ID, traceId);
                if (requestId != null) MDC.put(REQUEST_ID, requestId);
                if (userId != null) MDC.put(USER_ID, userId);
                if (userName != null) MDC.put(USER_NAME, userName);
                if (ipAddr != null) MDC.put(IP_ADDR, ipAddr);
                return task.call();
            }
            finally
            {
                clear();
            }
        };
    }

    public static Runnable wrap(Runnable task)
    {
        String traceId = getTraceId();
        String requestId = getRequestId();
        String userId = getUserId();
        String userName = getUserName();
        String ipAddr = MDC.get(IP_ADDR);

        return () ->
        {
            try
            {
                if (traceId != null) MDC.put(TRACE_ID, traceId);
                if (requestId != null) MDC.put(REQUEST_ID, requestId);
                if (userId != null) MDC.put(USER_ID, userId);
                if (userName != null) MDC.put(USER_NAME, userName);
                if (ipAddr != null) MDC.put(IP_ADDR, ipAddr);
                task.run();
            }
            finally
            {
                clear();
            }
        };
    }

    public static ExecutorService getExecutorService()
    {
        return EXECUTOR;
    }
}
