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
 * 消息推送工具类
 * 
 * 提供消息推送相关的公共方法
 * 
 * @author ruoyi
 */
public class MessagePushUtils {
    
    private static final Logger log = LoggerFactory.getLogger(MessagePushUtils.class);
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 构建消息推送数据
     * 
     * @param type 消息类型
     * @param payload 负载数据
     * @param conversationId 会话ID（可选）
     * @param groupId 群组ID（可选）
     * @return 推送数据
     */
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
     * 构建消息推送数据（无会话或群组）
     * 
     * @param type 消息类型
     * @param payload 负载数据
     * @return 推送数据
     */
    public static Map<String, Object> buildMessagePushData(String type, Object payload) {
        return buildMessagePushData(type, payload, null, null);
    }
    
    /**
     * 将消息推送数据转换为JSON字符串
     * 
     * @param pushData 推送数据
     * @return JSON字符串
     */
    public static String toJsonString(Map<String, Object> pushData) {
        try {
            return objectMapper.writeValueAsString(pushData);
        } catch (Exception e) {
            log.error("消息推送数据转换为JSON失败", e);
            throw new BusinessException("消息推送数据格式错误", e);
        }
    }
    
    /**
     * 构建用户消息推送数据
     * 
     * @param userId 用户ID
     * @param message 消息对象
     * @return 推送数据
     */
    public static Map<String, Object> buildUserMessagePushData(Long userId, ImMessage message) {
        return buildMessagePushData("message", message, null, null);
    }
    
    /**
     * 构建会话消息推送数据
     * 
     * @param conversationId 会话ID
     * @param message 消息对象
     * @return 推送数据
     */
    public static Map<String, Object> buildConversationMessagePushData(Long conversationId, ImMessage message) {
        return buildMessagePushData("message", message, conversationId, null);
    }
    
    /**
     * 构建群组消息推送数据
     * 
     * @param groupId 群组ID
     * @param message 消息对象
     * @return 推送数据
     */
    public static Map<String, Object> buildGroupMessagePushData(Long groupId, ImMessage message) {
        return buildMessagePushData("message", null, null, groupId);
    }
    
    /**
     * 构建状态推送数据
     * 
     * @param type 状态类型
     * @param statusData 状态数据
     * @return 推送数据
     */
    public static Map<String, Object> buildStatusPushData(String type, Map<String, Object> statusData) {
        Map<String, Object> pushData = new HashMap<>();
        pushData.put("type", type);
        pushData.putAll(statusData);
        pushData.put("timestamp", System.currentTimeMillis());
        return pushData;
    }
    
    /**
     * 构建在线状态推送数据
     * 
     * @param userId 用户ID
     * @param online 是否在线
     * @return 推送数据
     */
    public static Map<String, Object> buildOnlineStatusPushData(Long userId, boolean online) {
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("userId", userId);
        statusData.put("online", online);
        
        return buildStatusPushData("online_status", statusData);
    }
    
    /**
     * 构建正在输入状态推送数据
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param isTyping 是否正在输入
     * @return 推送数据
     */
    public static Map<String, Object> buildTypingStatusPushData(Long conversationId, Long userId, boolean isTyping) {
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("conversationId", conversationId);
        statusData.put("userId", userId);
        statusData.put("isTyping", isTyping);
        
        return buildStatusPushData("typing_status", statusData);
    }
    
    /**
     * 构建已读回执推送数据
     * 
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 推送数据
     */
    public static Map<String, Object> buildReadReceiptPushData(Long messageId, Long userId) {
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("messageId", messageId);
        statusData.put("userId", userId);
        
        return buildStatusPushData("read_receipt", statusData);
    }
    
    /**
     * 构建消息撤回推送数据
     * 
     * @param messageId 消息ID
     * @param conversationId 会话ID
     * @return 推送数据
     */
    public static Map<String, Object> buildMessageRevokePushData(Long messageId, Long conversationId) {
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("messageId", messageId);
        statusData.put("conversationId", conversationId);
        
        return buildStatusPushData("message_revoke", statusData);
    }
    
    /**
     * 构建消息反应推送数据
     * 
     * @param messageId 消息ID
     * @param reaction 反应表情
     * @param userId 用户ID
     * @param added 是否添加
     * @return 推送数据
     */
    public static Map<String, Object> buildMessageReactionPushData(Long messageId, String reaction, Long userId, boolean added) {
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("messageId", messageId);
        statusData.put("reaction", reaction);
        statusData.put("userId", userId);
        statusData.put("added", added);
        
        return buildStatusPushData("message_reaction", statusData);
    }
    
    /**
     * 检查参数是否有效
     * 
     * @param userId 用户ID
     * @param message 消息对象
     * @param methodName 方法名
     */
    public static void validatePushParams(Long userId, ImMessage message, String methodName) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(methodName + "参数无效: 用户ID必须大于0");
        }
        if (message == null) {
            throw new BusinessException(methodName + "参数无效: 消息对象不能为空");
        }
        if (message.getId() == null) {
            log.warn(methodName + "警告: 消息ID为空，可能导致推送异常");
        }
    }
    
    /**
     * 检查状态参数是否有效
     * 
     * @param userId 用户ID
     * @param methodName 方法名
     */
    public static void validateStatusParams(Long userId, String methodName) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(methodName + "参数无效: 用户ID必须大于0");
        }
    }
    
    /**
     * 检查消息反应参数是否有效
     * 
     * @param messageId 消息ID
     * @param reaction 反应表情
     * @param userId 用户ID
     * @param methodName 方法名
     */
    public static void validateReactionParams(Long messageId, String reaction, Long userId, String methodName) {
        if (messageId == null || messageId <= 0) {
            throw new BusinessException(methodName + "参数无效: 消息ID必须大于0");
        }
        if (reaction == null || reaction.trim().isEmpty()) {
            throw new BusinessException(methodName + "参数无效: 反应表情不能为空");
        }
        if (userId == null || userId <= 0) {
            throw new BusinessException(methodName + "参数无效: 用户ID必须大于0");
        }
    }
    
    /**
     * 记录推送日志
     * 
     * @param methodName 方法名
     * @param userId 用户ID
     * @param messageId 消息ID（可选）
     * @param success 状态（成功/失败）
     * @param duration 耗时（毫秒，可选）
     */
    public static void logPushResult(String methodName, Long userId, Long messageId, boolean success, Long duration) {
        if (success) {
            if (duration != null) {
                log.info("{}成功: userId={}, messageId={}, 耗时={}ms", methodName, userId, messageId, duration);
            } else {
                log.info("{}成功: userId={}, messageId={}", methodName, userId, messageId);
            }
        } else {
            if (duration != null) {
                log.warn("{}失败: userId={}, messageId={}, 耗时={}ms", methodName, userId, messageId, duration);
            } else {
                log.warn("{}失败: userId={}, messageId={}", methodName, userId, messageId);
            }
        }
    }
}