package com.ruoyi.im.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 娑堟伅鎺ㄩ€佸伐鍏风被
 * 
 * 鎻愪緵娑堟伅鎺ㄩ€佺浉鍏崇殑鍏叡鏂规硶
 * 
 * @author ruoyi
 */
public class MessagePushUtils {
    
    private static final Logger log = LoggerFactory.getLogger(MessagePushUtils.class);
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 鏋勫缓娑堟伅鎺ㄩ€佹暟鎹?     * 
     * @param type 娑堟伅绫诲瀷
     * @param payload 璐熻浇鏁版嵁
     * @param conversationId 浼氳瘽ID锛堝彲閫夛級
     * @param groupId 缇ょ粍ID锛堝彲閫夛級
     * @return 鎺ㄩ€佹暟鎹?     */
    public static Map<String, Object> buildMessagePushData(String type, Object payload, Long conversationId, Long groupId) {
        Map<String, Object> pushData = new HashMap<>();
        pushData.put("type", type);
        pushData.put("payload", payload);
        pushData.put("timestamp", System.currentTimeMillis());
        
        if (conversationId != null) {
            pushData.put("conversationId", conversationId);
        }
        
        if (groupId != null) {
            pushData.put("groupId", groupId);
        }
        
        return pushData;
    }
    
    /**
     * 鏋勫缓娑堟伅鎺ㄩ€佹暟鎹紙鏃犱細璇濇垨缇ょ粍锛?     * 
     * @param type 娑堟伅绫诲瀷
     * @param payload 璐熻浇鏁版嵁
     * @return 鎺ㄩ€佹暟鎹?     */
    public static Map<String, Object> buildMessagePushData(String type, Object payload) {
        return buildMessagePushData(type, payload, null, null);
    }
    
    /**
     * 灏嗘秷鎭帹閫佹暟鎹浆鎹负JSON瀛楃涓?     * 
     * @param pushData 鎺ㄩ€佹暟鎹?     * @return JSON瀛楃涓?     */
    public static String toJsonString(Map<String, Object> pushData) {
        try {
            return objectMapper.writeValueAsString(pushData);
        } catch (Exception e) {
            log.error("娑堟伅鎺ㄩ€佹暟鎹浆鎹负JSON澶辫触", e);
            throw new BusinessException("娑堟伅鎺ㄩ€佹暟鎹牸寮忛敊璇?, e);
        }
    }
    
    /**
     * 鏋勫缓鐢ㄦ埛娑堟伅鎺ㄩ€佹暟鎹?     * 
     * @param userId 鐢ㄦ埛ID
     * @param message 娑堟伅瀵硅薄
     * @return 鎺ㄩ€佹暟鎹?     */
    public static Map<String, Object> buildUserMessagePushData(Long userId, ImMessage message) {
        return buildMessagePushData("message", message, null, null);
    }
    
    /**
     * 鏋勫缓浼氳瘽娑堟伅鎺ㄩ€佹暟鎹?     * 
     * @param conversationId 浼氳瘽ID
     * @param message 娑堟伅瀵硅薄
     * @return 鎺ㄩ€佹暟鎹?     */
    public static Map<String, Object> buildConversationMessagePushData(Long conversationId, ImMessage message) {
        return buildMessagePushData("message", message, conversationId, null);
    }
    
    /**
     * 鏋勫缓缇ょ粍娑堟伅鎺ㄩ€佹暟鎹?     * 
     * @param groupId 缇ょ粍ID
     * @param message 娑堟伅瀵硅薄
     * @return 鎺ㄩ€佹暟鎹?     */
    public static Map<String, Object> buildGroupMessagePushData(Long groupId, ImMessage message) {
        return buildMessagePushData("message", null, null, groupId);
    }
    
    /**
     * 鏋勫缓鐘舵€佹帹閫佹暟鎹?     * 
     * @param type 鐘舵€佺被鍨?     * @param statusData 鐘舵€佹暟鎹?     * @return 鎺ㄩ€佹暟鎹?     */
    public static Map<String, Object> buildStatusPushData(String type, Map<String, Object> statusData) {
        Map<String, Object> pushData = new HashMap<>();
        pushData.put("type", type);
        pushData.putAll(statusData);
        pushData.put("timestamp", System.currentTimeMillis());
        return pushData;
    }
    
    /**
     * 鏋勫缓鍦ㄧ嚎鐘舵€佹帹閫佹暟鎹?     * 
     * @param userId 鐢ㄦ埛ID
     * @param online 鏄惁鍦ㄧ嚎
     * @return 鎺ㄩ€佹暟鎹?     */
    public static Map<String, Object> buildOnlineStatusPushData(Long userId, boolean online) {
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("userId", userId);
        statusData.put("online", online);
        
        return buildStatusPushData("online_status", statusData);
    }
    
    /**
     * 鏋勫缓姝ｅ湪杈撳叆鐘舵€佹帹閫佹暟鎹?     * 
     * @param conversationId 浼氳瘽ID
     * @param userId 鐢ㄦ埛ID
     * @param isTyping 鏄惁姝ｅ湪杈撳叆
     * @return 鎺ㄩ€佹暟鎹?     */
    public static Map<String, Object> buildTypingStatusPushData(Long conversationId, Long userId, boolean isTyping) {
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("conversationId", conversationId);
        statusData.put("userId", userId);
        statusData.put("isTyping", isTyping);
        
        return buildStatusPushData("typing_status", statusData);
    }
    
    /**
     * 鏋勫缓宸茶鍥炴墽鎺ㄩ€佹暟鎹?     * 
     * @param messageId 娑堟伅ID
     * @param userId 鐢ㄦ埛ID
     * @return 鎺ㄩ€佹暟鎹?     */
    public static Map<String, Object> buildReadReceiptPushData(Long messageId, Long userId) {
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("messageId", messageId);
        statusData.put("userId", userId);
        
        return buildStatusPushData("read_receipt", statusData);
    }
    
    /**
     * 鏋勫缓娑堟伅鎾ゅ洖鎺ㄩ€佹暟鎹?     * 
     * @param messageId 娑堟伅ID
     * @param conversationId 浼氳瘽ID
     * @return 鎺ㄩ€佹暟鎹?     */
    public static Map<String, Object> buildMessageRevokePushData(Long messageId, Long conversationId) {
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("messageId", messageId);
        statusData.put("conversationId", conversationId);
        
        return buildStatusPushData("message_revoke", statusData);
    }
    
    /**
     * 鏋勫缓娑堟伅鍙嶅簲鎺ㄩ€佹暟鎹?     * 
     * @param messageId 娑堟伅ID
     * @param reaction 鍙嶅簲琛ㄦ儏
     * @param userId 鐢ㄦ埛ID
     * @param added 鏄惁娣诲姞
     * @return 鎺ㄩ€佹暟鎹?     */
    public static Map<String, Object> buildMessageReactionPushData(Long messageId, String reaction, Long userId, boolean added) {
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("messageId", messageId);
        statusData.put("reaction", reaction);
        statusData.put("userId", userId);
        statusData.put("added", added);
        
        return buildStatusPushData("message_reaction", statusData);
    }
    
    /**
     * 妫€鏌ュ弬鏁版槸鍚︽湁鏁?     * 
     * @param userId 鐢ㄦ埛ID
     * @param message 娑堟伅瀵硅薄
     * @param methodName 鏂规硶鍚?     */
    public static void validatePushParams(Long userId, ImMessage message, String methodName) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 鐢ㄦ埛ID蹇呴』澶т簬0");
        }
        if (message == null) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 娑堟伅瀵硅薄涓嶈兘涓虹┖");
        }
        if (message.getId() == null) {
            log.warn(methodName + "璀﹀憡: 娑堟伅ID涓虹┖锛屽彲鑳藉鑷存帹閫佸紓甯?);
        }
    }
    
    /**
     * 妫€鏌ョ姸鎬佸弬鏁版槸鍚︽湁鏁?     * 
     * @param userId 鐢ㄦ埛ID
     * @param methodName 鏂规硶鍚?     */
    public static void validateStatusParams(Long userId, String methodName) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 鐢ㄦ埛ID蹇呴』澶т簬0");
        }
    }
    
    /**
     * 妫€鏌ユ秷鎭弽搴斿弬鏁版槸鍚︽湁鏁?     * 
     * @param messageId 娑堟伅ID
     * @param reaction 鍙嶅簲琛ㄦ儏
     * @param userId 鐢ㄦ埛ID
     * @param methodName 鏂规硶鍚?     */
    public static void validateReactionParams(Long messageId, String reaction, Long userId, String methodName) {
        if (messageId == null || messageId <= 0) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 娑堟伅ID蹇呴』澶т簬0");
        }
        if (reaction == null || reaction.trim().isEmpty()) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 鍙嶅簲琛ㄦ儏涓嶈兘涓虹┖");
        }
        if (userId == null || userId <= 0) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 鐢ㄦ埛ID蹇呴』澶т簬0");
        }
    }
    
    /**
     * 璁板綍鎺ㄩ€佹棩蹇?     * 
     * @param methodName 鏂规硶鍚?     * @param userId 鐢ㄦ埛ID
     * @param messageId 娑堟伅ID锛堝彲閫夛級
     * @param success 鐘舵€侊紙鎴愬姛/澶辫触锛?     * @param duration 鑰楁椂锛堟绉掞紝鍙€夛級
     */
    public static void logPushResult(String methodName, Long userId, Long messageId, boolean success, Long duration) {
        if (success) {
            if (duration != null) {
                log.info("{}鎴愬姛: userId={}, messageId={}, 鑰楁椂={}ms", methodName, userId, messageId, duration);
            } else {
                log.info("{}鎴愬姛: userId={}, messageId={}", methodName, userId, messageId);
            }
        } else {
            if (duration != null) {
                log.warn("{}澶辫触: userId={}, messageId={}, 鑰楁椂={}ms", methodName, userId, messageId, duration);
            } else {
                log.warn("{}澶辫触: userId={}, messageId={}", methodName, userId, messageId);
            }
        }
    }
}
