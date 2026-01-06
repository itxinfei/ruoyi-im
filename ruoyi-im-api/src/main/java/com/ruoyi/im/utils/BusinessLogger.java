package com.ruoyi.im.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 涓氬姟鏃ュ織宸ュ叿绫? * 
 * 鐢ㄤ簬鍦ㄤ笟鍔″眰涓褰曞叧閿搷浣滅殑鏃ュ織
 * 
 * @author ruoyi
 */
public class BusinessLogger {
    
    private static final Map<String, Logger> loggerMap = new ConcurrentHashMap<>();
    
    /**
     * 鑾峰彇涓氬姟鏃ュ織璁板綍鍣?     * 
     * @param module 妯″潡鍚嶇О
     * @return 鏃ュ織璁板綍鍣?     */
    private static Logger getLogger(String module) {
        return loggerMap.computeIfAbsent("com.ruoyi.im." + module, LoggerFactory::getLogger);
    }
    
    /**
     * 璁板綍鐢ㄦ埛鎿嶄綔鏃ュ織
     * 
     * @param userId 鐢ㄦ埛ID
     * @param operation 鎿嶄綔鍚嶇О
     * @param description 鎿嶄綔鎻忚堪
     */
    public static void logUserOperation(Long userId, String operation, String description) {
        Logger logger = getLogger("user");
        logger.info("鐢ㄦ埛鎿嶄綔: [鐢ㄦ埛ID: {}, 鎿嶄綔: {}, 鎻忚堪: {}]", userId, operation, description);
    }
    
    /**
     * 璁板綍鐢ㄦ埛鎿嶄綔鏃ュ織
     * 
     * @param userId 鐢ㄦ埛ID
     * @param operation 鎿嶄綔鍚嶇О
     * @param description 鎿嶄綔鎻忚堪
     * @param params 鎿嶄綔鍙傛暟
     */
    public static void logUserOperation(Long userId, String operation, String description, String params) {
        Logger logger = getLogger("user");
        logger.info("鐢ㄦ埛鎿嶄綔: [鐢ㄦ埛ID: {}, 鎿嶄綔: {}, 鎻忚堪: {}, 鍙傛暟: {}]", userId, operation, description, params);
    }
    
    /**
     * 璁板綍娑堟伅鎿嶄綔鏃ュ織
     * 
     * @param userId 鐢ㄦ埛ID
     * @param messageId 娑堟伅ID
     * @param operation 鎿嶄綔鍚嶇О
     * @param description 鎿嶄綔鎻忚堪
     */
    public static void logMessageOperation(Long userId, Long messageId, String operation, String description) {
        Logger logger = getLogger("message");
        logger.info("娑堟伅鎿嶄綔: [鐢ㄦ埛ID: {}, 娑堟伅ID: {}, 鎿嶄綔: {}, 鎻忚堪: {}]", userId, messageId, operation, description);
    }
    
    /**
     * 璁板綍浼氳瘽鎿嶄綔鏃ュ織
     * 
     * @param userId 鐢ㄦ埛ID
     * @param conversationId 浼氳瘽ID
     * @param operation 鎿嶄綔鍚嶇О
     * @param description 鎿嶄綔鎻忚堪
     */
    public static void logConversationOperation(Long userId, Long conversationId, String operation, String description) {
        Logger logger = getLogger("conversation");
        logger.info("浼氳瘽鎿嶄綔: [鐢ㄦ埛ID: {}, 浼氳瘽ID: {}, 鎿嶄綔: {}, 鎻忚堪: {}]", userId, conversationId, operation, description);
    }
    
    /**
     * 璁板綍瀹夊叏鐩稿叧鏃ュ織
     * 
     * @param userId 鐢ㄦ埛ID
     * @param operation 鎿嶄綔鍚嶇О
     * @param description 鎿嶄綔鎻忚堪
     * @param result 鎿嶄綔缁撴灉
     */
    public static void logSecurityOperation(Long userId, String operation, String description, String result) {
        Logger logger = getLogger("security");
        logger.warn("瀹夊叏鎿嶄綔: [鐢ㄦ埛ID: {}, 鎿嶄綔: {}, 鎻忚堪: {}, 缁撴灉: {}]", userId, operation, description, result);
    }
    
    /**
     * 璁板綍涓氬姟寮傚父鏃ュ織
     * 
     * @param module 妯″潡鍚嶇О
     * @param operation 鎿嶄綔鍚嶇О
     * @param description 鎿嶄綔鎻忚堪
     * @param exception 寮傚父淇℃伅
     */
    public static void logBusinessException(String module, String operation, String description, Exception exception) {
        Logger logger = getLogger("exception");
        logger.error("涓氬姟寮傚父: [妯″潡: {}, 鎿嶄綔: {}, 鎻忚堪: {}, 寮傚父: {}]", module, operation, description, exception.getMessage(), exception);
    }
    
    /**
     * 璁板綍绯荤粺鐘舵€佹棩蹇?     * 
     * @param operation 鎿嶄綔鍚嶇О
     * @param description 鎿嶄綔鎻忚堪
     */
    public static void logSystemStatus(String operation, String description) {
        Logger logger = getLogger("system");
        logger.info("绯荤粺鐘舵€? [鎿嶄綔: {}, 鎻忚堪: {}]", operation, description);
    }
    
    /**
     * 璁板綍绯荤粺鐘舵€佹棩蹇?     * 
     * @param operation 鎿嶄綔鍚嶇О
     * @param description 鎿嶄綔鎻忚堪
     * @param details 璇︾粏淇℃伅
     */
    public static void logSystemStatus(String operation, String description, String details) {
        Logger logger = getLogger("system");
        logger.info("绯荤粺鐘舵€? [鎿嶄綔: {}, 鎻忚堪: {}, 璇︾粏淇℃伅: {}]", operation, description, details);
    }
}
