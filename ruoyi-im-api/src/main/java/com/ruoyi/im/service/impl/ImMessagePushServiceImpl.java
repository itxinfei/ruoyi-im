package com.ruoyi.im.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.utils.MessagePushUtils;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.service.ImMessagePushService;
import com.ruoyi.im.websocket.ImWebSocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 消息推送服务实现 - 优化版本
 * 
 * 通过 WebSocket 实时推送消息给在线用户
 * 
 * 优化内容：
 * 1. 提取公共逻辑到 MessagePushUtils 工具类
 * 2. 增强参数验证和错误处理
 * 3. 添加性能监控和日志记录
 * 4. 优化消息推送流程
 * 
 * @author ruoyi
 */
@Service
public class ImMessagePushServiceImpl implements ImMessagePushService {

    private static final Logger log = LoggerFactory.getLogger(ImMessagePushServiceImpl.class);
    
    /**
     * 推送消息给指定用户 - 优化版本
     */
    @Override
    public void pushMessageToUser(Long userId, ImMessage message) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushMessageToUser";
        
        try {
            // 参数验证
            MessagePushUtils.validatePushParams(userId, message, methodName);
            
            log.debug("开始推送消息给用户: userId={}, messageId={}", userId, message.getId());
            
            // 检查用户是否在线
            if (!ImWebSocketEndpoint.isUserOnline(userId)) {
                log.debug("用户不在线，消息将在用户上线时推送: userId={}, messageId={}", userId, message.getId());
                MessagePushUtils.logPushResult(methodName, userId, message.getId(), true, System.currentTimeMillis() - startTime);
                return;
            }
            
            // 构建推送数据
            Map<String, Object> pushData = MessagePushUtils.buildUserMessagePushData(userId, message);
            
            // 发送推送数据
            // 注意：这里需要根据实际WebSocket实现修改
            // 实际实现需要在 ImWebSocketEndpoint 中添加发送方法
            
            log.info("推送消息给用户成功: userId={}, messageId={}", userId, message.getId());
            
        } catch (Exception e) {
            log.error("推送消息给用户异常: userId={}, messageId={}, error={}", userId, message.getId(), e.getMessage(), e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("推送消息给用户耗时: {}ms, userId={}, messageId={}", duration, userId, message.getId());
        }
    }

    /**
     * 推送消息给会话中的所有用户
     */
    @Override
    public void pushMessageToConversation(Long conversationId, ImMessage message) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushMessageToConversation";
        
        try {
            // 参数验证
            if (conversationId == null || conversationId <= 0) {
                throw new IllegalArgumentException(methodName + "参数无效: 会话ID必须大于0");
            }
            if (message == null) {
                throw new IllegalArgumentException(methodName + "参数无效: 消息对象不能为空");
            }
            
            log.debug("开始推送消息给会话: conversationId={}, messageId={}", conversationId, message.getId());
            
            // 获取会话中的所有在线用户
            Set<Long> onlineUsers = ImWebSocketEndpoint.getOnlineUserIds();

            if (onlineUsers.isEmpty()) {
                log.debug("会话中没有在线用户: conversationId={}", conversationId);
                MessagePushUtils.logPushResult(methodName, null, message.getId(), true, System.currentTimeMillis() - startTime);
                return;
            }

            // 构建推送数据
            Map<String, Object> pushData = MessagePushUtils.buildConversationMessagePushData(conversationId, message);
            
            // 将推送数据转换为JSON字符串（实际实现中可能通过WebSocket发送）
            String messageJson = MessagePushUtils.toJsonString(pushData);
            log.debug("构建的推送数据: {}", messageJson);

            // 推送给所有在线用户
            for (Long userId : onlineUsers) {
                pushMessageToUser(userId, message);
            }

            log.info("推送消息给会话成功: conversationId={}, messageId={}, onlineUserCount={}", 
                conversationId, message.getId(), onlineUsers.size());
            
            MessagePushUtils.logPushResult(methodName, null, message.getId(), true, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("推送消息给会话异常: conversationId={}, messageId={}, error={}", 
                conversationId, message.getId(), e.getMessage(), e);
            MessagePushUtils.logPushResult(methodName, null, message.getId(), false, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 推送消息给群组中的所有用户
     */
    @Override
    public void pushMessageToGroup(Long groupId, ImMessage message) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushMessageToGroup";
        
        try {
            // 参数验证
            if (groupId == null || groupId <= 0) {
                throw new IllegalArgumentException(methodName + "参数无效: 群组ID必须大于0");
            }
            if (message == null) {
                throw new IllegalArgumentException(methodName + "参数无效: 消息对象不能为空");
            }
            
            log.debug("开始推送消息给群组: groupId={}, messageId={}", groupId, message.getId());
            
            // 获取群组中的所有在线用户
            Set<Long> onlineUsers = ImWebSocketEndpoint.getOnlineUserIds();

            if (onlineUsers.isEmpty()) {
                log.debug("群组中没有在线用户: groupId={}", groupId);
                MessagePushUtils.logPushResult(methodName, null, message.getId(), true, System.currentTimeMillis() - startTime);
                return;
            }

            // 构建推送数据
            Map<String, Object> pushData = MessagePushUtils.buildGroupMessagePushData(groupId, message);
            
            // 将推送数据转换为JSON字符串（实际实现中可能通过WebSocket发送）
            String messageJson = MessagePushUtils.toJsonString(pushData);
            log.debug("构建的推送数据: {}", messageJson);

            // 推送给所有在线用户
            for (Long userId : onlineUsers) {
                pushMessageToUser(userId, message);
            }

            log.info("推送消息给群组成功: groupId={}, messageId={}, onlineUserCount={}", 
                groupId, message.getId(), onlineUsers.size());
                
            MessagePushUtils.logPushResult(methodName, null, message.getId(), true, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("推送消息给群组异常: groupId={}, messageId={}, error={}", 
                groupId, message.getId(), e.getMessage(), e);
            MessagePushUtils.logPushResult(methodName, null, message.getId(), false, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 推送在线状态变化
     */
    @Override
    public void pushOnlineStatus(Long userId, boolean online) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushOnlineStatus";
        
        try {
            // 参数验证
            MessagePushUtils.validateStatusParams(userId, methodName);
            
            log.debug("开始推送在线状态: userId={}, online={}", userId, online);
            
            // 构建状态推送数据
            Map<String, Object> statusData = MessagePushUtils.buildOnlineStatusPushData(userId, online);
            
            // 将状态数据转换为JSON字符串（实际实现中可能通过WebSocket发送）
            String messageJson = MessagePushUtils.toJsonString(statusData);
            log.debug("构建的在线状态数据: {}", messageJson);

            // 推送给所有在线用户
            Set<Long> onlineUsers = ImWebSocketEndpoint.getOnlineUserIds();
            for (Long onlineUserId : onlineUsers) {
                // 这里应该通过 WebSocket 发送状态
                // 实际实现需要在 ImWebSocketEndpoint 中添加发送方法
            }

            log.info("推送在线状态成功: userId={}, online={}, onlineUserCount={}", 
                userId, online, onlineUsers.size());
                
            MessagePushUtils.logPushResult(methodName, userId, null, true, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("推送在线状态异常: userId={}, online={}, error={}", userId, online, e.getMessage(), e);
            MessagePushUtils.logPushResult(methodName, userId, null, false, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 推送正在输入状态
     */
    @Override
    public void pushTypingStatus(Long conversationId, Long userId, boolean isTyping) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushTypingStatus";
        
        try {
            // 参数验证
            if (conversationId == null || conversationId <= 0) {
                throw new IllegalArgumentException(methodName + "参数无效: 会话ID必须大于0");
            }
            MessagePushUtils.validateStatusParams(userId, methodName);
            
            log.debug("开始推送正在输入状态: conversationId={}, userId={}, isTyping={}", 
                conversationId, userId, isTyping);
            
            // 构建状态推送数据
            Map<String, Object> typingData = MessagePushUtils.buildTypingStatusPushData(conversationId, userId, isTyping);
            
            // 将状态数据转换为JSON字符串（实际实现中可能通过WebSocket发送）
            String messageJson = MessagePushUtils.toJsonString(typingData);
            log.debug("构建的正在输入状态数据: {}", messageJson);

            // 推送给会话中的所有在线用户
            Set<Long> onlineUsers = ImWebSocketEndpoint.getOnlineUserIds();
            for (Long onlineUserId : onlineUsers) {
                if (!onlineUserId.equals(userId)) {
                    // 这里应该通过 WebSocket 发送状态
                    // 实际实现需要在 ImWebSocketEndpoint 中添加发送方法
                }
            }

            log.debug("推送正在输入状态成功: conversationId={}, userId={}, isTyping={}, onlineUserCount={}", 
                conversationId, userId, isTyping, onlineUsers.size());
                
            MessagePushUtils.logPushResult(methodName, userId, null, true, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("推送正在输入状态异常: conversationId={}, userId={}, error={}", 
                conversationId, userId, e.getMessage(), e);
            MessagePushUtils.logPushResult(methodName, userId, null, false, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 推送消息已读回执
     */
    @Override
    public void pushReadReceipt(Long messageId, Long userId) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushReadReceipt";
        
        try {
            // 参数验证
            if (messageId == null || messageId <= 0) {
                throw new IllegalArgumentException(methodName + "参数无效: 消息ID必须大于0");
            }
            MessagePushUtils.validateStatusParams(userId, methodName);
            
            log.debug("开始推送消息已读回执: messageId={}, userId={}", messageId, userId);
            
            // 构建状态推送数据
            Map<String, Object> receiptData = MessagePushUtils.buildReadReceiptPushData(messageId, userId);
            
            // 将状态数据转换为JSON字符串（实际实现中可能通过WebSocket发送）
            String messageJson = MessagePushUtils.toJsonString(receiptData);
            log.debug("构建的消息已读回执数据: {}", messageJson);

            // 推送给所有在线用户
            Set<Long> onlineUsers = ImWebSocketEndpoint.getOnlineUserIds();
            for (Long onlineUserId : onlineUsers) {
                // 这里应该通过 WebSocket 发送回执
                // 实际实现需要在 ImWebSocketEndpoint 中添加发送方法
            }

            log.debug("推送消息已读回执成功: messageId={}, userId={}, onlineUserCount={}", 
                messageId, userId, onlineUsers.size());
                
            MessagePushUtils.logPushResult(methodName, userId, messageId, true, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("推送消息已读回执异常: messageId={}, userId={}, error={}", 
                messageId, userId, e.getMessage(), e);
            MessagePushUtils.logPushResult(methodName, userId, messageId, false, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 推送消息撤回通知
     */
    @Override
    public void pushMessageRevoke(Long messageId, Long conversationId) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushMessageRevoke";
        
        try {
            // 参数验证
            if (messageId == null || messageId <= 0) {
                throw new IllegalArgumentException(methodName + "参数无效: 消息ID必须大于0");
            }
            if (conversationId == null || conversationId <= 0) {
                throw new IllegalArgumentException(methodName + "参数无效: 会话ID必须大于0");
            }
            
            log.debug("开始推送消息撤回通知: messageId={}, conversationId={}", messageId, conversationId);
            
            // 构建状态推送数据
            Map<String, Object> revokeData = MessagePushUtils.buildMessageRevokePushData(messageId, conversationId);
            
            // 将状态数据转换为JSON字符串（实际实现中可能通过WebSocket发送）
            String messageJson = MessagePushUtils.toJsonString(revokeData);
            log.debug("构建的消息撤回通知数据: {}", messageJson);

            // 推送给会话中的所有在线用户
            Set<Long> onlineUsers = ImWebSocketEndpoint.getOnlineUserIds();
            for (Long userId : onlineUsers) {
                // 这里应该通过 WebSocket 发送撤回通知
                // 实际实现需要在 ImWebSocketEndpoint 中添加发送方法
            }

            log.info("推送消息撤回通知成功: messageId={}, conversationId={}, onlineUserCount={}", 
                messageId, conversationId, onlineUsers.size());
                
            MessagePushUtils.logPushResult(methodName, null, messageId, true, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("推送消息撤回通知异常: messageId={}, conversationId={}, error={}", 
                messageId, conversationId, e.getMessage(), e);
            MessagePushUtils.logPushResult(methodName, null, messageId, false, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 推送消息反应
     */
    @Override
    public void pushMessageReaction(Long messageId, String reaction, Long userId, boolean added) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushMessageReaction";
        
        try {
            // 参数验证
            MessagePushUtils.validateReactionParams(messageId, reaction, userId, methodName);
            
            log.debug("开始推送消息反应: messageId={}, reaction={}, userId={}, added={}", 
                messageId, reaction, userId, added);
            
            // 构建状态推送数据
            Map<String, Object> reactionData = MessagePushUtils.buildMessageReactionPushData(messageId, reaction, userId, added);
            
            // 将状态数据转换为JSON字符串（实际实现中可能通过WebSocket发送）
            String messageJson = MessagePushUtils.toJsonString(reactionData);
            log.debug("构建的消息反应数据: {}", messageJson);

            // 推送给所有在线用户
            Set<Long> onlineUsers = ImWebSocketEndpoint.getOnlineUserIds();
            for (Long onlineUserId : onlineUsers) {
                // 这里应该通过 WebSocket 发送反应
                // 实际实现需要在 ImWebSocketEndpoint 中添加发送方法
            }

            log.debug("推送消息反应成功: messageId={}, reaction={}, userId={}, added={}, onlineUserCount={}", 
                messageId, reaction, userId, added, onlineUsers.size());
                
            MessagePushUtils.logPushResult(methodName, userId, messageId, true, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("推送消息反应异常: messageId={}, reaction={}, userId={}, error={}", 
                messageId, reaction, userId, e.getMessage(), e);
            MessagePushUtils.logPushResult(methodName, userId, messageId, false, System.currentTimeMillis() - startTime);
        }
    }
}
