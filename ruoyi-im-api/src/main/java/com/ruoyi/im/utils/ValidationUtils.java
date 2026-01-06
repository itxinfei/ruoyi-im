package com.ruoyi.im.utils;

import com.ruoyi.im.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * 鍙傛暟楠岃瘉宸ュ叿绫? * 
 * 鎻愪緵缁熶竴鐨勫弬鏁伴獙璇佹柟娉曪紝娑堥櫎閲嶅楠岃瘉閫昏緫
 * 
 * @author ruoyi
 */
public class ValidationUtils {
    
    private static final Logger log = LoggerFactory.getLogger(ValidationUtils.class);
    
    // 甯哥敤姝ｅ垯琛ㄨ揪寮?    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^1[3-9]\\d{9}$"
    );
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_]{4,20}$"
    );
    
    /**
     * 楠岃瘉鐢ㄦ埛ID鍙傛暟
     * 
     * @param userId 鐢ㄦ埛ID
     * @param methodName 鏂规硶鍚?     * @throws BusinessException 鍙傛暟鏃犳晥鏃舵姏鍑哄紓甯?     */
    public static void validateUserId(Long userId, String methodName) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 鐢ㄦ埛ID蹇呴』澶т簬0");
        }
    }
    
    /**
     * 楠岃瘉浼氳瘽ID鍙傛暟
     * 
     * @param conversationId 浼氳瘽ID
     * @param methodName 鏂规硶鍚?     * @throws BusinessException 鍙傛暟鏃犳晥鏃舵姏鍑哄紓甯?     */
    public static void validateConversationId(Long conversationId, String methodName) {
        if (conversationId == null || conversationId <= 0) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 浼氳瘽ID蹇呴』澶т簬0");
        }
    }
    
    /**
     * 楠岃瘉閫氱敤ID鍙傛暟
     * 
     * @param id ID
     * @param methodName 鏂规硶鍚?     * @throws BusinessException 鍙傛暟鏃犳晥鏃舵姏鍑哄紓甯?     */
    public static void validateId(Long id, String methodName) {
        if (id == null || id <= 0) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: ID蹇呴』澶т簬0");
        }
    }
    
    /**
     * 楠岃瘉娑堟伅ID鍙傛暟
     * 
     * @param messageId 娑堟伅ID
     * @param methodName 鏂规硶鍚?     * @throws BusinessException 鍙傛暟鏃犳晥鏃舵姏鍑哄紓甯?     */
    public static void validateMessageId(Long messageId, String methodName) {
        if (messageId == null || messageId <= 0) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 娑堟伅ID蹇呴』澶т簬0");
        }
    }
    
    /**
     * 楠岃瘉缇ょ粍ID鍙傛暟
     * 
     * @param groupId 缇ょ粍ID
     * @param methodName 鏂规硶鍚?     * @throws BusinessException 鍙傛暟鏃犳晥鏃舵姏鍑哄紓甯?     */
    public static void validateGroupId(Long groupId, String methodName) {
        if (groupId == null || groupId <= 0) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 缇ょ粍ID蹇呴』澶т簬0");
        }
    }
    
    /**
     * 楠岃瘉鍒嗛〉鍙傛暟
     * 
     * @param pageNum 椤电爜
     * @param pageSize 姣忛〉澶у皬
     * @param methodName 鏂规硶鍚?     * @throws BusinessException 鍙傛暟鏃犳晥鏃舵姏鍑哄紓甯?     */
    public static void validatePaginationParams(Integer pageNum, Integer pageSize, String methodName) {
        if (pageNum == null || pageNum <= 0) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 椤电爜蹇呴』澶т簬0");
        }
        if (pageSize == null || pageSize <= 0 || pageSize > 100) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 姣忛〉澶у皬蹇呴』澶т簬0涓斾笉瓒呰繃100");
        }
    }
    
    /**
     * 楠岃瘉瀛楃涓插弬鏁?     * 
     * @param str 瀛楃涓?     * @param paramName 鍙傛暟鍚?     * @param methodName 鏂规硶鍚?     * @throws BusinessException 鍙傛暟鏃犳晥鏃舵姏鍑哄紓甯?     */
    public static void validateString(String str, String paramName, String methodName) {
        if (str == null || str.trim().isEmpty()) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: " + paramName + "涓嶈兘涓虹┖");
        }
    }
    
    /**
     * 楠岃瘉瀛楃涓查暱搴?     * 
     * @param str 瀛楃涓?     * @param paramName 鍙傛暟鍚?     * @param maxLength 鏈€澶ч暱搴?     * @param methodName 鏂规硶鍚?     * @throws BusinessException 鍙傛暟鏃犳晥鏃舵姏鍑哄紓甯?     */
    public static void validateStringLength(String str, String paramName, int maxLength, String methodName) {
        validateString(str, paramName, methodName);
        if (str.length() > maxLength) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: " + paramName + "闀垮害涓嶈兘瓒呰繃" + maxLength + "涓瓧绗?);
        }
    }
    
    /**
     * 楠岃瘉闆嗗悎鍙傛暟
     * 
     * @param collection 闆嗗悎
     * @param paramName 鍙傛暟鍚?     * @param methodName 鏂规硶鍚?     * @throws BusinessException 鍙傛暟鏃犳晥鏃舵姏鍑哄紓甯?     */
    public static void validateCollection(Collection<?> collection, String paramName, String methodName) {
        if (collection == null || collection.isEmpty()) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: " + paramName + "涓嶈兘涓虹┖");
        }
    }
    
    /**
     * 楠岃瘉ID鏁扮粍鍙傛暟
     * 
     * @param ids ID鏁扮粍
     * @param methodName 鏂规硶鍚?     * @throws BusinessException 鏁扮粍涓虹┖鎴栨棤鏁堟椂鎶涘嚭寮傚父
     */
    public static void validateIdArray(Long[] ids, String methodName) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: ID鏁扮粍涓嶈兘涓虹┖");
        }
        for (Long id : ids) {
            validateId(id, methodName);
        }
    }
    
    /**
     * 楠岃瘉鐢ㄦ埛鍚嶆牸寮?     * 
     * @param username 鐢ㄦ埛鍚?     * @param methodName 鏂规硶鍚?     * @throws BusinessException 鍙傛暟鏃犳晥鏃舵姏鍑哄紓甯?     */
    public static void validateUsername(String username, String methodName) {
        validateString(username, "鐢ㄦ埛鍚?, methodName);
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 鐢ㄦ埛鍚嶅彧鑳藉寘鍚瓧姣嶃€佹暟瀛楀拰涓嬪垝绾匡紝闀垮害4-20浣?);
        }
    }
    
    /**
     * 楠岃瘉閭鏍煎紡
     * 
     * @param email 閭
     * @param methodName 鏂规硶鍚?     * @throws BusinessException 鍙傛暟鏃犳晥鏃舵姏鍑哄紓甯?     */
    public static void validateEmail(String email, String methodName) {
        validateString(email, "閭", methodName);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 閭鏍煎紡涓嶆纭?);
        }
    }
    
    /**
     * 楠岃瘉鎵嬫満鍙锋牸寮?     * 
     * @param phone 鎵嬫満鍙?     * @param methodName 鏂规硶鍚?     * @throws BusinessException 鍙傛暟鏃犳晥鏃舵姏鍑哄紓甯?     */
    public static void validatePhone(String phone, String methodName) {
        validateString(phone, "鎵嬫満鍙?, methodName);
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 鎵嬫満鍙锋牸寮忎笉姝ｇ‘");
        }
    }
    
    /**
     * 楠岃瘉瀵嗙爜寮哄害
     * 
     * @param password 瀵嗙爜
     * @param methodName 鏂规硶鍚?     * @throws BusinessException 鍙傛暟鏃犳晥鏃舵姏鍑哄紓甯?     */
    public static void validatePassword(String password, String methodName) {
        validateString(password, "瀵嗙爜", methodName);
        if (password.length() < 6) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 瀵嗙爜闀垮害鑷冲皯6浣?);
        }
        if (password.length() > 20) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 瀵嗙爜闀垮害涓嶈兘瓒呰繃20浣?);
        }
    }
    
    /**
     * 楠岃瘉娑堟伅绫诲瀷
     * 
     * @param type 娑堟伅绫诲瀷
     * @param methodName 鏂规硶鍚?     * @throws BusinessException 鍙傛暟鏃犳晥鏃舵姏鍑哄紓甯?     */
    public static void validateMessageType(String type, String methodName) {
        validateString(type, "娑堟伅绫诲瀷", methodName);
        if (!isValidMessageType(type)) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 涓嶆敮鎸佺殑娑堟伅绫诲瀷: " + type);
        }
    }
    
    /**
     * 楠岃瘉娑堟伅鍐呭闀垮害
     * 
     * @param content 娑堟伅鍐呭
     * @param maxLength 鏈€澶ч暱搴?     * @param methodName 鏂规硶鍚?     * @throws BusinessException 鍙傛暟鏃犳晥鏃舵姏鍑哄紓甯?     */
    public static void validateMessageContent(String content, int maxLength, String methodName) {
        validateString(content, "娑堟伅鍐呭", methodName);
        if (content.length() > maxLength) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 娑堟伅鍐呭闀垮害涓嶈兘瓒呰繃" + maxLength + "涓瓧绗?);
        }
    }
    
    /**
     * 楠岃瘉鏄惁鏈夋晥鐨勬秷鎭被鍨?     * 
     * @param type 娑堟伅绫诲瀷
     * @return 鏄惁鏈夋晥
     */
    public static boolean isValidMessageType(String type) {
        return "TEXT".equals(type) || 
               "IMAGE".equals(type) || 
               "FILE".equals(type) || 
               "VOICE".equals(type) || 
               "VIDEO".equals(type) ||
               "LOCATION".equals(type) ||
               "SYSTEM".equals(type);
    }
    
    /**
     * 璁板綍楠岃瘉鏃ュ織
     * 
     * @param methodName 鏂规硶鍚?     * @param paramName 鍙傛暟鍚?     * @param paramValue 鍙傛暟鍊?     * @param success 鏄惁鎴愬姛
     */
    public static void logValidationResult(String methodName, String paramName, Object paramValue, boolean success) {
        if (success) {
            log.debug("鍙傛暟楠岃瘉鎴愬姛: {}.{}={}", methodName, paramName, paramValue);
        } else {
            log.warn("鍙傛暟楠岃瘉澶辫触: {}.{}={}", methodName, paramName, paramValue);
        }
    }
}
