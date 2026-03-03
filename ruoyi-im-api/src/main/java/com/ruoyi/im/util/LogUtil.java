package com.ruoyi.im.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public class LogUtil
{
    public static Logger getLogger(Class<?> clazz)
    {
        return LoggerFactory.getLogger(clazz);
    }

    public static Logger getLogger(String name)
    {
        return LoggerFactory.getLogger(name);
    }

    public static void trace(Logger logger, String msg, Object... args)
    {
        if (logger.isTraceEnabled())
        {
            logger.trace(msg, args);
        }
    }

    public static void trace(Logger logger, Marker marker, String msg, Object... args)
    {
        if (logger.isTraceEnabled(marker))
        {
            logger.trace(marker, msg, args);
        }
    }

    public static void debug(Logger logger, String msg, Object... args)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug(msg, args);
        }
    }

    public static void debug(Logger logger, Marker marker, String msg, Object... args)
    {
        if (logger.isDebugEnabled(marker))
        {
            logger.debug(marker, msg, args);
        }
    }

    public static void info(Logger logger, String msg, Object... args)
    {
        if (logger.isInfoEnabled())
        {
            logger.info(msg, args);
        }
    }

    public static void info(Logger logger, Marker marker, String msg, Object... args)
    {
        if (logger.isInfoEnabled(marker))
        {
            logger.info(marker, msg, args);
        }
    }

    public static void warn(Logger logger, String msg, Object... args)
    {
        if (logger.isWarnEnabled())
        {
            logger.warn(msg, args);
        }
    }

    public static void warn(Logger logger, Marker marker, String msg, Object... args)
    {
        if (logger.isWarnEnabled(marker))
        {
            logger.warn(marker, msg, args);
        }
    }

    public static void error(Logger logger, String msg, Object... args)
    {
        logger.error(msg, args);
    }

    public static void error(Logger logger, Marker marker, String msg, Object... args)
    {
        logger.error(marker, msg, args);
    }

    public static void error(Logger logger, String msg, Throwable throwable)
    {
        logger.error(msg, throwable);
    }

    public static void error(Logger logger, Throwable throwable, String msg, Object... args)
    {
        logger.error(msg, throwable);
    }

    public static class Performance
    {
        private final Logger logger;
        private final String operation;
        private final long startTime;
        private boolean logged = false;

        public Performance(Class<?> clazz, String operation)
        {
            this.logger = LoggerFactory.getLogger(clazz);
            this.operation = operation;
            this.startTime = System.currentTimeMillis();
        }

        public Performance(Logger logger, String operation)
        {
            this.logger = logger;
            this.operation = operation;
            this.startTime = System.currentTimeMillis();
        }

        public void stop()
        {
            if (!logged)
            {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("[PERF] {} - {}ms", operation, duration);
                logged = true;
            }
        }

        public void stop(String extraInfo)
        {
            if (!logged)
            {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("[PERF] {} - {}ms - {}", operation, duration, extraInfo);
                logged = true;
            }
        }

        public void stopIfSlow(long thresholdMs)
        {
            if (!logged)
            {
                long duration = System.currentTimeMillis() - startTime;
                if (duration > thresholdMs)
                {
                    logger.warn("[PERF-SLOW] {} - {}ms (threshold: {}ms)", operation, duration, thresholdMs);
                }
                logged = true;
            }
        }

        public long getDuration()
        {
            return System.currentTimeMillis() - startTime;
        }
    }

    public static Performance performance(Class<?> clazz, String operation)
    {
        return new Performance(clazz, operation);
    }

    public static Performance performance(Logger logger, String operation)
    {
        return new Performance(logger, operation);
    }

    public static class Business
    {
        private static final Logger BUSINESS_LOG = LoggerFactory.getLogger("com.ruoyi.im.business");

        public static void log(String action, String userId, String detail)
        {
            BUSINESS_LOG.info("[BIZ] action={} userId={} detail={}", action, userId, detail);
        }

        public static void log(String action, String userId, String detail, String result)
        {
            BUSINESS_LOG.info("[BIZ] action={} userId={} detail={} result={}", action, userId, detail, result);
        }

        public static void log(String action, String userId, String detail, boolean success)
        {
            if (success)
            {
                BUSINESS_LOG.info("[BIZ] action={} userId={} detail={} success=true", action, userId, detail);
            }
            else
            {
                BUSINESS_LOG.warn("[BIZ] action={} userId={} detail={} success=false", action, userId, detail);
            }
        }
    }

    public static class Security
    {
        private static final Logger SECURITY_LOG = LoggerFactory.getLogger("com.ruoyi.im.security");

        public static void logLogin(String userId, String ip, boolean success, String reason)
        {
            if (success)
            {
                SECURITY_LOG.info("[SEC] userId={} ip={} action=LOGIN success=true", userId, ip);
            }
            else
            {
                SECURITY_LOG.warn("[SEC] userId={} ip={} action=LOGIN success=false reason={}", userId, ip, reason);
            }
        }

        public static void logLogout(String userId, String ip)
        {
            SECURITY_LOG.info("[SEC] userId={} ip={} action=LOGOUT", userId, ip);
        }

        public static void logAccess(String userId, String resource, String ip, boolean allowed)
        {
            if (allowed)
            {
                SECURITY_LOG.info("[SEC] userId={} ip={} resource={} action=ACCESS allowed=true", userId, ip, resource);
            }
            else
            {
                SECURITY_LOG.warn("[SEC] userId={} ip={} resource={} action=ACCESS allowed=false", userId, ip, resource);
            }
        }

        public static void logSecurityEvent(String event, String detail)
        {
            SECURITY_LOG.warn("[SEC] event={} detail={}", event, detail);
        }
    }

    public static class Access
    {
        private static final Logger ACCESS_LOG = LoggerFactory.getLogger("com.ruoyi.im.access");

        public static void log(String method, String uri, int status, long duration, String ip, String userId)
        {
            ACCESS_LOG.info("{} {} {} {}ms ip={} userId={}", method, uri, status, duration, ip, userId);
        }

        public static void log(String method, String uri, int status, long duration)
        {
            ACCESS_LOG.info("{} {} {} {}ms", method, uri, status, duration);
        }
    }
}
