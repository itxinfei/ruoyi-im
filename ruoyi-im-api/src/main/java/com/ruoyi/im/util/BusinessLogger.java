package com.ruoyi.im.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 业务日志工具类
 * 
 * 用于在业务层中记录关键操作的日志
 * 
 * @author ruoyi
 */
public class BusinessLogger {
    
    private static final Map<String, Logger> loggerMap = new ConcurrentHashMap<>();
    
    /**
     * 获取业务日志记录器
     * 
     * @param module 模块名称
     * @return 日志记录器
     */
    private static Logger getLogger(String module) {
        return loggerMap.computeIfAbsent("com.ruoyi.im." + module, LoggerFactory::getLogger);
    }
    
    /**
     * 记录用户操作日志
     * 
     * @param userId 用户ID
     * @param operation 操作名称
     * @param description 操作描述
     */
    public static void logUserOperation(Long userId, String operation, String description) {
        Logger logger = getLogger("user");
        logger.info("用户操作: [用户ID: {}, 操作: {}, 描述: {}]", userId, operation, description);
    }
    
    /**
     * 记录用户操作日志
     * 
     * @param userId 用户ID
     * @param operation 操作名称
     * @param description 操作描述
     * @param params 操作参数
     */
    public static void logUserOperation(Long userId, String operation, String description, String params) {
        Logger logger = getLogger("user");
        logger.info("用户操作: [用户ID: {}, 操作: {}, 描述: {}, 参数: {}]", userId, operation, description, params);
    }
    
    /**
     * 记录消息操作日志
     * 
     * @param userId 用户ID
     * @param messageId 消息ID
     * @param operation 操作名称
     * @param description 操作描述
     */
    public static void logMessageOperation(Long userId, Long messageId, String operation, String description) {
        Logger logger = getLogger("message");
        logger.info("消息操作: [用户ID: {}, 消息ID: {}, 操作: {}, 描述: {}]", userId, messageId, operation, description);
    }
    
    /**
     * 记录会话操作日志
     * 
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @param operation 操作名称
     * @param description 操作描述
     */
    public static void logConversationOperation(Long userId, Long conversationId, String operation, String description) {
        Logger logger = getLogger("conversation");
        logger.info("会话操作: [用户ID: {}, 会话ID: {}, 操作: {}, 描述: {}]", userId, conversationId, operation, description);
    }
    
    /**
     * 记录安全相关日志
     * 
     * @param userId 用户ID
     * @param operation 操作名称
     * @param description 操作描述
     * @param result 操作结果
     */
    public static void logSecurityOperation(Long userId, String operation, String description, String result) {
        Logger logger = getLogger("security");
        logger.warn("安全操作: [用户ID: {}, 操作: {}, 描述: {}, 结果: {}]", userId, operation, description, result);
    }
    
    /**
     * 记录业务异常日志
     * 
     * @param module 模块名称
     * @param operation 操作名称
     * @param description 操作描述
     * @param exception 异常信息
     */
    public static void logBusinessException(String module, String operation, String description, Exception exception) {
        Logger logger = getLogger("exception");
        logger.error("业务异常: [模块: {}, 操作: {}, 描述: {}, 异常: {}]", module, operation, description, exception.getMessage(), exception);
    }
    
    /**
     * 记录系统状态日志
     * 
     * @param operation 操作名称
     * @param description 操作描述
     */
    public static void logSystemStatus(String operation, String description) {
        Logger logger = getLogger("system");
        logger.info("系统状态: [操作: {}, 描述: {}]", operation, description);
    }
    
    /**
     * 记录系统状态日志
     * 
     * @param operation 操作名称
     * @param description 操作描述
     * @param details 详细信息
     */
    public static void logSystemStatus(String operation, String description, String details) {
        Logger logger = getLogger("system");
        logger.info("系统状态: [操作: {}, 描述: {}, 详细信息: {}]", operation, description, details);
    }
}