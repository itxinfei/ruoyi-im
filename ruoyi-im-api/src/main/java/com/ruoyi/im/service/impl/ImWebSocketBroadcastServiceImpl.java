package com.ruoyi.im.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.service.ImWebSocketBroadcastService;
import com.ruoyi.im.util.MessageEncryptionUtil;
import com.ruoyi.im.vo.ding.DingMessageVO;
import com.ruoyi.im.websocket.ImWebSocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 分布式 WebSocket 广播服务实现
 * 通过 Redis Pub/Sub 实现跨节点消息转发
 */
@Service
public class ImWebSocketBroadcastServiceImpl implements ImWebSocketBroadcastService {

    private static final Logger log = LoggerFactory.getLogger(ImWebSocketBroadcastServiceImpl.class);
    private static final String IM_WS_CHANNEL = "im:ws:broadcast";

    private final ImConversationMemberMapper conversationMemberMapper;
    private final ImMessageMapper messageMapper;
    private final com.ruoyi.im.mapper.ImUserMapper imUserMapper;
    private final MessageEncryptionUtil encryptionUtil;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public ImWebSocketBroadcastServiceImpl(
            ImConversationMemberMapper conversationMemberMapper,
            ImMessageMapper messageMapper,
            com.ruoyi.im.mapper.ImUserMapper imUserMapper,
            MessageEncryptionUtil encryptionUtil,
            ObjectMapper objectMapper,
            RedisTemplate<String, Object> redisTemplate) {
        this.conversationMemberMapper = conversationMemberMapper;
        this.messageMapper = messageMapper;
        this.imUserMapper = imUserMapper;
        this.encryptionUtil = encryptionUtil;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void broadcastMessageToConversation(Long conversationId, Long messageId, Long senderId) {
        try {
            List<ImConversationMember> members = conversationMemberMapper.selectByConversationId(conversationId);
            if (members == null || members.isEmpty()) return;

            ImMessage message = messageMapper.selectImMessageById(messageId);
            if (message == null) return;

            Map<String, Object> wsMessage = new HashMap<>();
            wsMessage.put("type", "message");
            wsMessage.put("data", createMessageData(message));

            publishToMembers(members, wsMessage, senderId);
        } catch (Exception e) {
            log.error("广播消息异常: conversationId={}, messageId={}", conversationId, messageId, e);
        }
    }

    @Override
    public void broadcastReactionUpdate(Long conversationId, Long messageId, Long userId, String emoji, String action) {
        try {
            List<ImConversationMember> members = conversationMemberMapper.selectByConversationId(conversationId);
            if (members == null || members.isEmpty()) return;

            Map<String, Object> msg = createReactionMessage(conversationId, messageId, userId, emoji, action);
            publishToMembers(members, msg, userId);
        } catch (Exception e) {
            log.error("广播反应更新异常", e);
        }
    }

    @Override
    public void broadcastReadReceipt(Long conversationId, Long lastReadMessageId, Long userId) {
        try {
            List<ImConversationMember> members = conversationMemberMapper.selectByConversationId(conversationId);
            if (members == null || members.isEmpty()) return;

            Map<String, Object> msg = createReadReceiptMessage(conversationId, lastReadMessageId, userId);
            publishToMembers(members, msg, userId);
        } catch (Exception e) {
            log.error("广播已读回执异常", e);
        }
    }

    @Override
    public void broadcastRecallNotification(Long conversationId, Long messageId, Long userId) {
        try {
            List<ImConversationMember> members = conversationMemberMapper.selectByConversationId(conversationId);
            if (members == null || members.isEmpty()) return;

            Map<String, Object> recallMap = new HashMap<>();
            recallMap.put("type", "message_recall");
            Map<String, Object> data = new HashMap<>();
            data.put("conversationId", conversationId);
            data.put("messageId", messageId);
            data.put("userId", userId);
            data.put("timestamp", System.currentTimeMillis());
            recallMap.put("data", data);

            publishToMembers(members, recallMap, userId);
        } catch (Exception e) {
            log.error("广播消息撤回通知异常", e);
        }
    }

    @Override
    public void broadcastTypingStatus(Long conversationId, Long userId, boolean isTyping) {
        try {
            List<ImConversationMember> members = conversationMemberMapper.selectByConversationId(conversationId);
            if (members == null || members.isEmpty()) return;

            Map<String, Object> statusMap = new HashMap<>();
            statusMap.put("type", "typing");
            Map<String, Object> data = new HashMap<>();
            data.put("conversationId", conversationId);
            data.put("userId", userId);
            data.put("isTyping", isTyping);
            data.put("timestamp", System.currentTimeMillis());
            statusMap.put("data", data);

            publishToMembers(members, statusMap, userId);
        } catch (Exception e) {
            log.error("广播输入状态异常", e);
        }
    }

    @Override
    public void broadcastOnlineStatus(Long userId, boolean online) {
        try {
            Map<String, Object> statusMap = new HashMap<>();
            statusMap.put("type", online ? "online" : "offline");
            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("online", online);
            data.put("timestamp", System.currentTimeMillis());
            statusMap.put("data", data);

            // 在线状态属于全局广播
            String messageJson = objectMapper.writeValueAsString(statusMap);
            
            // 1. 本地全量发送
            ImWebSocketEndpoint.broadcastToAllOnline(messageJson);
            
            // 2. Redis 全量广播 (让其他节点也执行本地全量发送)
            Map<String, Object> redisPayload = new HashMap<>();
            redisPayload.put("broadcastAll", true);
            redisPayload.put("messageJson", messageJson);
            redisTemplate.convertAndSend(IM_WS_CHANNEL, redisPayload);
        } catch (Exception e) {
            log.error("广播在线状态异常: userId={}", userId, e);
        }
    }

    @Override
    public void sendCallSignal(Long toUserId, Object signal) {
        Map<String, Object> wsMessage = new HashMap<>();
        wsMessage.put("type", "call");
        wsMessage.put("data", signal);
        wsMessage.put("timestamp", System.currentTimeMillis());
        sendToUser(toUserId, wsMessage);
    }

    @Override
    public void sendToUser(Long userId, Object message) {
        if (userId == null) return;
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            // 优先本地发送
            boolean localSent = sendToLocalUser(userId, messageJson);
            
            // 广播到 Redis 供分布式节点消费
            Map<String, Object> redisPayload = new HashMap<>();
            redisPayload.put("targetUserId", userId);
            redisPayload.put("messageJson", messageJson);
            redisTemplate.convertAndSend(IM_WS_CHANNEL, redisPayload);
            
            if (localSent) log.debug("消息已成功在本节点发送给用户: {}", userId);
        } catch (Exception e) {
            log.error("分布式发送消息失败: userId={}", userId, e);
        }
    }

    @Override
    public void broadcastDingMessage(DingMessageVO dingVO, Set<Long> targetUserIds) {
        if (targetUserIds == null || targetUserIds.isEmpty()) return;
        Map<String, Object> wsMessage = new HashMap<>();
        wsMessage.put("type", "ding");
        wsMessage.put("data", dingVO);
        
        for (Long targetUserId : targetUserIds) {
            sendToUser(targetUserId, wsMessage);
        }
    }

    private void publishToMembers(List<ImConversationMember> members, Object message, Long excludeUserId) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            for (ImConversationMember member : members) {
                Long tid = member.getUserId();
                if (tid == null || tid.equals(excludeUserId)) continue;
                
                // 本地发送
                sendToLocalUser(tid, messageJson);
                
                // 转发给 Redis 供其他节点消费
                Map<String, Object> redisPayload = new HashMap<>();
                redisPayload.put("targetUserId", tid);
                redisPayload.put("messageJson", messageJson);
                redisTemplate.convertAndSend(IM_WS_CHANNEL, redisPayload);
            }
        } catch (Exception e) {
            log.error("Redis 消息发布失败", e);
        }
    }

    private boolean sendToLocalUser(Long userId, String messageJson) {
        javax.websocket.Session session = ImWebSocketEndpoint.getOnlineUsers().get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(messageJson);
                return true;
            } catch (Exception e) {
                log.warn("本地 Session 发送失败: userId={}", userId);
            }
        }
        return false;
    }

    private Map<String, Object> createMessageData(ImMessage message) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", message.getId());
        data.put("conversationId", message.getConversationId());
        data.put("senderId", message.getSenderId());
        data.put("type", message.getMessageType() != null ? message.getMessageType().toUpperCase() : "TEXT");
        data.put("content", encryptionUtil.decryptMessage(message.getContent()));
        
        try {
            com.ruoyi.im.domain.ImUser sender = imUserMapper.selectImUserById(message.getSenderId());
            if (sender != null) {
                data.put("senderName", sender.getNickname());
                data.put("senderAvatar", sender.getAvatar());
            }
        } catch (Exception e) {}

        long timestamp = message.getCreateTime() != null 
            ? message.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() 
            : System.currentTimeMillis();
        data.put("timestamp", timestamp);
        return data;
    }

    private Map<String, Object> createReactionMessage(Long conversationId, Long messageId, Long userId, String emoji, String action) {
        Map<String, Object> m = new HashMap<>();
        m.put("type", "reaction");
        m.put("action", action);
        m.put("conversationId", conversationId);
        m.put("messageId", messageId);
        m.put("userId", userId);
        m.put("emoji", emoji);
        m.put("timestamp", System.currentTimeMillis());
        return m;
    }

    private Map<String, Object> createReadReceiptMessage(Long conversationId, Long lastReadMessageId, Long userId) {
        Map<String, Object> m = new HashMap<>();
        m.put("type", "read");
        m.put("conversationId", conversationId);
        m.put("lastReadMessageId", lastReadMessageId);
        m.put("userId", userId);
        m.put("timestamp", System.currentTimeMillis());
        return m;
    }
}
