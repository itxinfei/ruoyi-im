package com.ruoyi.im.utils;

import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 娑堟伅鏋勫缓鍣?
 * 
 * @author ruoyi
 */
@Component
public class MessageBuilder {
    
    /**
     * 鏋勫缓WebSocket娑堟伅
     */
    public Object buildWebSocketMessage(Object message) {
        // 绠€鍗曞疄鐜帮紝杩斿洖娑堟伅瀵硅薄鏈韩
        return message;
    }
    
    /**
     * 鏋勫缓鍦ㄧ嚎鐘舵€佹秷鎭?
     */
    public Object buildOnlineStatusMessage(Long userId, boolean online) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "status");
        message.put("userId", userId);
        message.put("online", online);
        message.put("timestamp", new Date());
        return message;
    }
    
    /**
     * 鏋勫缓娑堟伅鎾ゅ洖閫氱煡
     */
    public Object buildRecallMessage(Long messageId, Long userId) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "recall");
        message.put("messageId", messageId);
        message.put("userId", userId);
        message.put("timestamp", new Date());
        return message;
    }
    
    /**
     * 鏋勫缓宸茶鍥炴墽娑堟伅
     */
    public Object buildReadReceiptMessage(Long messageId, Long userId) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "read_receipt");
        message.put("messageId", messageId);
        message.put("userId", userId);
        message.put("timestamp", new Date());
        return message;
    }
    
    /**
     * 鏋勫缓娑堟伅鍙嶅簲閫氱煡
     */
    public Object buildReactionMessage(Long messageId, String reaction, Long userId, boolean added) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "reaction");
        message.put("messageId", messageId);
        message.put("reaction", reaction);
        message.put("userId", userId);
        message.put("added", added);
        message.put("timestamp", new Date());
        return message;
    }
    
    /**
     * 鏋勫缓娑堟伅鍥炴墽
     */
    public Object buildReceiptMessage(Long messageId, Integer status, Long userId) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "receipt");
        message.put("messageId", messageId);
        message.put("status", status);
        message.put("userId", userId);
        message.put("timestamp", new Date());
        return message;
    }
}
