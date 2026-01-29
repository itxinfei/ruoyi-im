package com.ruoyi.im.util;

import com.ruoyi.im.exception.BusinessException;
import org.slf4j.Logger;

/**
 * 异常处理工具类
 * 统一异常处理日志记录和业务异常抛出
 *
 * @author ruoyi
 */
public final class ExceptionHandlerUtil {

    private ExceptionHandlerUtil() {
        // 防止实例化
    }

    // ========== 抛出业务异常 ==========

    /**
     * 记录错误日志并抛出业务异常
     *
     * @param logger  日志记录器
     * @param message 错误消息
     * @param e       异常
     */
    public static void throwBusinessException(Logger logger, String message, Exception e) {
        logger.error("{}: {}", message, e.getMessage(), e);
        throw new BusinessException(message);
    }

    /**
     * 记录错误日志并抛出业务异常（带错误码）
     *
     * @param logger  日志记录器
     * @param code    错误码
     * @param message 错误消息
     * @param e       异常
     */
    public static void throwBusinessException(Logger logger, String code, String message, Exception e) {
        logger.error("[{}] {}: {}", code, message, e.getMessage(), e);
        throw new BusinessException(code, message);
    }

    /**
     * 记录错误日志并抛出业务异常（使用错误码和错误消息）
     *
     * @param logger  日志记录器
     * @param code    错误码
     * @param message 错误消息
     * @param e       异常
     */
    public static void throwBusinessException(Logger logger, String code, String message, Exception e) {
        logger.error("[{}] {}: {}", code, message, e.getMessage(), e);
        throw new BusinessException(code, message);
    }

    /**
     * 抛出业务异常（不记录日志）
     *
     * @param message 错误消息
     */
    public static void throwBusinessException(String message) {
        throw new BusinessException(message);
    }

    /**
     * 抛出业务异常（带错误码，不记录日志）
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public static void throwBusinessException(String code, String message) {
        throw new BusinessException(code, message);
    }

    // ========== 日志记录 ==========

    /**
     * 记录警告日志（异常可恢复）
     *
     * @param logger   日志记录器
     * @param template 日志模板
     * @param args     参数
     */
    public static void logWarning(Logger logger, String template, Object... args) {
        logger.warn(template, args);
    }

    /**
     * 记录警告日志（带异常）
     *
     * @param logger   日志记录器
     * @param template 日志模板
     * @param e        异常
     * @param args     参数
     */
    public static void logWarning(Logger logger, String template, Exception e, Object... args) {
        logger.warn(template, args);
        if (e != null) {
            logger.warn("异常详情: {}", e.getMessage(), e);
        }
    }

    /**
     * 记录错误日志（不抛出异常）
     *
     * @param logger   日志记录器
     * @param template 日志模板
     * @param e        异常
     * @param args     参数
     */
    public static void logError(Logger logger, String template, Exception e, Object... args) {
        logger.error(template, args, e);
    }

    /**
     * 记录错误日志（简化版）
     *
     * @param logger  日志记录器
     * @param message 错误消息
     * @param e       异常
     */
    public static void logError(Logger logger, String message, Exception e) {
        logger.error("{}: {}", message, e.getMessage(), e);
    }

    // ========== 异常处理并返回默认值 ==========

    /**
     * 记录错误日志并返回默认值
     *
     * @param logger       日志记录器
     * @param message      错误消息
     * @param e            异常
     * @param defaultValue 默认值
     * @param <T>          返回类型
     * @return 默认值
     */
    public static <T> T logErrorAndReturnDefault(Logger logger, String message, Exception e, T defaultValue) {
        logger.error("{}: {}", message, e.getMessage(), e);
        return defaultValue;
    }

    /**
     * 记录警告日志并返回默认值
     *
     * @param logger       日志记录器
     * @param message      警告消息
     * @param e            异常
     * @param defaultValue 默认值
     * @param <T>          返回类型
     * @return 默认值
     */
    public static <T> T logWarningAndReturnDefault(Logger logger, String message, Exception e, T defaultValue) {
        logger.warn("{}: {}", message, e.getMessage());
        return defaultValue;
    }

    // ========== 条件抛出异常 ==========

    /**
     * 条件满足时抛出业务异常
     *
     * @param condition 条件
     * @param message   错误消息
     */
    public static void throwIf(boolean condition, String message) {
        if (condition) {
            throw new BusinessException(message);
        }
    }

    /**
     * 条件满足时抛出业务异常（带错误码）
     *
     * @param condition 条件
     * @param code      错误码
     * @param message   错误消息
     */
    public static void throwIf(boolean condition, String code, String message) {
        if (condition) {
            throw new BusinessException(code, message);
        }
    }

    /**
     * 对象为空时抛出异常
     *
     * @param obj     检查对象
     * @param message 错误消息
     */
    public static void throwIfNull(Object obj, String message) {
        if (obj == null) {
            throw new BusinessException(message);
        }
    }
}
