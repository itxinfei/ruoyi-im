package com.ruoyi.im.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.service.ImWebSocketBroadcastService;
import com.ruoyi.im.utils.MessageEncryptionUtil;
import com.ruoyi.im.websocket.ImWebSocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImWebSocketBroadcastServiceImpl implements ImWebSocketBroadcastService {

    private static final Logger log = LoggerFactory.getLogger(ImWebSocketBroadcastServiceImpl.class);

    private final ImConversationMemberMapper conversationMemberMapper;
    private final ImMessageMapper messageMapper;
    private final MessageEncryptionUtil encryptionUtil;
    private final ObjectMapper objectMapper;

    public ImWebSocketBroadcastServiceImpl(
            ImConversationMemberMapper conversationMemberMapper,
            ImMessageMapper messageMapper,
            MessageEncryptionUtil encryptionUtil,
            ObjectMapper objectMapper) {
        this.conversationMemberMapper = conversationMemberMapper;
        this.messageMapper = messageMapper;
        this.encryptionUtil = encryptionUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public void broadcastMessageToConversation(Long conversationId, Long messageId, Long senderId) {
        try {
            List<ImConversationMember> members = conversationMemberMapper.selectByConversationId(conversationId);
            if (members == null || members.isEmpty()) {
                return;
            }

            ImMessage message = messageMapper.selectImMessageById(messageId);
            if (message == null) {
                return;
            }

            Map<String, Object> wsMessage = createWebSocketMessage(message);
            String messageJson = objectMapper.writeValueAsString(wsMessage);

            broadcastToMembers(members, messageJson, senderId);
        } catch (Exception e) {
            log.error("广播消息异常: conversationId={}, messageId={}", conversationId, messageId, e);
        }
    }

    @Override
    public void broadcastReactionUpdate(Long conversationId, Long messageId, Long userId, String emoji, String action) {
        try {
            List<ImConversationMember> members = conversationMemberMapper.selectByConversationId(conversationId);
            if (members == null || members.isEmpty()) {
                return;
            }

            Map<String, Object> reactionMessage = createReactionMessage(conversationId, messageId, userId, emoji, action);
            String messageJson = objectMapper.writeValueAsString(reactionMessage);

            broadcastToMembers(members, messageJson, userId);
        } catch (Exception e) {
            log.error("广播反应更新异常: conversationId={}, messageId={}", conversationId, messageId, e);
        }
    }

    @Override
    public void broadcastReadReceipt(Long conversationId, Long lastReadMessageId, Long userId) {
        try {
            List<ImConversationMember> members = conversationMemberMapper.selectByConversationId(conversationId);
            if (members == null || members.isEmpty()) {
                return;
            }

            Map<String, Object> readReceipt = createReadReceiptMessage(conversationId, lastReadMessageId, userId);
            String messageJson = objectMapper.writeValueAsString(readReceipt);

            broadcastToMembers(members, messageJson, userId);
        } catch (Exception e) {
            log.error("广播已读回执异常: conversationId={}", conversationId, e);
        }
    }

    private Map<String, Object> createWebSocketMessage(ImMessage message) {
        Map<String, Object> wsMessage = new HashMap<>();
        wsMessage.put("type", message.getMessageType() != null ? message.getMessageType().toLowerCase() : "text");
        wsMessage.put("conversationId", message.getConversationId());
        wsMessage.put("sessionId", message.getConversationId());
        wsMessage.put("id", message.getId());
        wsMessage.put("content", encryptionUtil.decryptMessage(message.getContent()));
        wsMessage.put("senderId", message.getSenderId());
        wsMessage.put("timestamp", message.getCreateTime() != null ? 
            message.getCreateTime().toString() : String.valueOf(System.currentTimeMillis()));

        if (message.getFileUrl() != null) {
            wsMessage.put("fileUrl", message.getFileUrl());
            wsMessage.put("fileName", message.getFileName());
            wsMessage.put("fileSize", message.getFileSize());
        }

        if (message.getReplyToMessageId() != null) {
            wsMessage.put("replyTo", message.getReplyToMessageId());
        }

        if (message.getIsRevoked() != null && message.getIsRevoked() == 1) {
            wsMessage.put("revoked", true);
            wsMessage.put("content", "[消息已撤回]");
        }

        return wsMessage;
    }

    private Map<String, Object> createReactionMessage(Long conversationId, Long messageId, Long userId, String emoji, String action) {
        Map<String, Object> reactionMessage = new HashMap<>();
        reactionMessage.put("type", "reaction");
        reactionMessage.put("action", action);
        reactionMessage.put("conversationId", conversationId);
        reactionMessage.put("messageId", messageId);
        reactionMessage.put("userId", userId);
        if (emoji != null) {
            reactionMessage.put("emoji", emoji);
        }
        reactionMessage.put("timestamp", System.currentTimeMillis());
        return reactionMessage;
    }

    private Map<String, Object> createReadReceiptMessage(Long conversationId, Long lastReadMessageId, Long userId) {
        Map<String, Object> readReceipt = new HashMap<>();
        readReceipt.put("type", "read");
        readReceipt.put("conversationId", conversationId);
        readReceipt.put("lastReadMessageId", lastReadMessageId);
        readReceipt.put("userId", userId);
        readReceipt.put("timestamp", System.currentTimeMillis());
        return readReceipt;
    }

    private void broadcastToMembers(List<ImConversationMember> members, String messageJson, Long excludeUserId) {
        Map<Long, javax.websocket.Session> onlineUsers = ImWebSocketEndpoint.getOnlineUsers();

        for (ImConversationMember member : members) {
            Long targetUserId = member.getUserId();
            if (targetUserId.equals(excludeUserId)) {
                continue;
            }

            javax.websocket.Session targetSession = onlineUsers.get(targetUserId);
            if (targetSession != null && targetSession.isOpen()) {
                try {
                    targetSession.getBasicRemote().sendText(messageJson);
                } catch (Exception e) {
                    log.error("发送消息给用户失败: userId={}", targetUserId, e);
                }
            }
        }
    }
}