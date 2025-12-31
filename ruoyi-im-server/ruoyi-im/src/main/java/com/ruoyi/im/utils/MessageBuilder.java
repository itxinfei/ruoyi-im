package com.ruoyi.im.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息构建器
 * 
 * @author ruoyi
 */
@Component
public class MessageBuilder {
    
    /**
     * 构建WebSocket消息
     */
    public Object buildWebSocketMessage(Object message) {
        // 简单实现，返回消息对象本身
        return message;
    }
    
    /**
     * 构建在线状态消息
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
     * 构建消息撤回通知
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
     * 构建已读回执消息
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
     * 构建消息反应通知
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
     * 构建消息回执
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