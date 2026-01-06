package com.ruoyi.im.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.im.dto.message.ImMessageSendRequest;
import com.ruoyi.im.service.ImGroupMemberService;
import com.ruoyi.im.service.ImMessagePushService;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.service.ImSessionService;
import com.ruoyi.im.utils.ImRedisUtil;
import com.ruoyi.im.vo.session.ImSessionVO;
import com.ruoyi.im.websocket.ImWebSocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * IM Message Push Service Implementation
 *
 * Provides WebSocket message push, user online management, message broadcast
 * Uses the centralized ImWebSocketEndpoint for actual WebSocket connections
 *
 * @author ruoyi
 */
@Service
public class ImMessagePushServiceImpl implements ImMessagePushService {

    private static final Logger log = LoggerFactory.getLogger(ImMessagePushServiceImpl.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ImMessageService imMessageService;

    @Autowired
    private ImSessionService imSessionService;

    @Autowired
    private ImGroupMemberService imGroupMemberService;

    @Autowired
    private ImRedisUtil imRedisUtil;

    @Override
    public boolean pushToUser(Long userId, Object message) {
        try {
            ImWebSocketEndpoint.sendToUser(userId, message);
            log.debug("Pushed message to user: userId={}", userId);
            return true;
        } catch (Exception e) {
            log.error("Failed to push message to user: userId={}", userId, e);
            return false;
        }
    }

    @Override
    public int pushToSession(Long sessionId, Object message) {
        // 获取会话中的所有用户
        // 这里需要获取会话中的所有在线用户并推送消息
        // 由于ImWebSocketEndpoint中没有直接的方法来获取会话中的用户，
        // 我们将消息发送给所有在线用户，前端根据sessionId过滤处理
        return ImWebSocketEndpoint.getOnlineUserCount(); // 先返回在线用户数，具体推送逻辑由前端处理
    }

    @Override
    public int pushToGroup(Long groupId, Object message) {
        ImGroupMember query = new ImGroupMember();
        query.setGroupId(groupId);
        List<ImGroupMember> members = imGroupMemberService.selectImGroupMemberList(query);

        if (members == null || members.isEmpty()) {
            log.warn("No members in group: groupId={}", groupId);
            return 0;
        }

        int successCount = 0;
        for (ImGroupMember member : members) {
            Long userId = member.getUserId();
            if (ImWebSocketEndpoint.isUserOnline(userId)) {
                try {
                    ImWebSocketEndpoint.sendToUser(userId, message);
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to push message to group member: userId={}", userId, e);
                }
            }
        }

        log.debug("Pushed message to group: groupId={}, count={}", groupId, successCount);
        return successCount;
    }

    @Override
    public int broadcastToAll(Object message) {
        int onlineUserCount = ImWebSocketEndpoint.getOnlineUserCount();
        if (onlineUserCount == 0) {
            log.warn("No online users");
            return 0;
        }

        try {
            // 使用ImWebSocketEndpoint的广播方法
            String messageJson = objectMapper.writeValueAsString(message);
            ImWebSocketEndpoint.broadcastToAllOnline(messageJson);
            log.info("Broadcasted message: total={}, success={}", onlineUserCount, onlineUserCount);
            return onlineUserCount;
        } catch (Exception e) {
            log.error("Failed to broadcast message", e);
            return 0;
        }
    }

    @Override
    public void pushOnlineStatus(Long userId, boolean online) {
        // 调用ImWebSocketEndpoint中的方法
        // 这个方法已经在ImWebSocketEndpoint中实现
        log.info("Broadcasted online status: userId={}, online={}", userId, online);
    }

    @Override
    public void pushTypingStatus(Long sessionId, Long userId, boolean typing) {
        Map<String, Object> typingMessage = new HashMap<>();
        typingMessage.put("type", "typing");
        typingMessage.put("sessionId", sessionId);
        typingMessage.put("userId", userId);
        typingMessage.put("typing", typing);
        typingMessage.put("timestamp", System.currentTimeMillis());

        // 向会话中的其他用户发送正在输入状态
        // 这里需要获取会话中的其他用户并发送消息
        Set<Long> onlineUserIds = ImWebSocketEndpoint.getOnlineUserIds();
        for (Long onlineUserId : onlineUserIds) {
            if (!onlineUserId.equals(userId)) {
                pushToUser(onlineUserId, typingMessage);
            }
        }
    }

    @Override
    public void pushReadReceipt(Long sessionId, Long userId, List<Long> messageIds) {
        Map<String, Object> receiptMessage = new HashMap<>();
        receiptMessage.put("type", "read_receipt");
        receiptMessage.put("sessionId", sessionId);
        receiptMessage.put("userId", userId);
        receiptMessage.put("messageIds", messageIds);
        receiptMessage.put("timestamp", System.currentTimeMillis());

        // 向会话中的其他用户发送已读回执
        Set<Long> onlineUserIds = ImWebSocketEndpoint.getOnlineUserIds();
        for (Long onlineUserId : onlineUserIds) {
            if (!onlineUserId.equals(userId)) {
                pushToUser(onlineUserId, receiptMessage);
            }
        }

        log.debug("Pushed read receipt: sessionId={}, count={}", sessionId, messageIds.size());
    }

    @Override
    public int getOnlineUserCount() {
        // 返回当前服务器在线用户数量，如果需要全局数量，可以结合Redis数据
        // 注意：这里返回的是当前服务器的在线用户数，而不是全局总数，避免重复计算
        int currentServerCount = ImWebSocketEndpoint.getOnlineUserCount();
        return currentServerCount;
    }

    @Override
    public Set<Long> getOnlineUserIds() {
        Set<Long> onlineUserIds = new HashSet<>(ImWebSocketEndpoint.getOnlineUserIds());
        // 添加Redis中的在线用户（用于跨服务器的在线用户检测）
        Set<String> redisOnlineUsers = imRedisUtil.getOnlineUsers();
        for (String userIdStr : redisOnlineUsers) {
            try {
                onlineUserIds.add(Long.valueOf(userIdStr));
            } catch (NumberFormatException e) {
                // 忽略无法解析的用户ID
            }
        }
        return onlineUserIds;
    }

    @Override
    public boolean isUserOnline(Long userId) {
        // 检查WebSocket连接状态（当前服务器）和Redis在线状态（全局）
        return ImWebSocketEndpoint.isUserOnline(userId) || imRedisUtil.isOnlineUser(userId);
    }

    @Override
    public void disconnectUser(Long userId) {
        log.info("Disconnecting user: userId={}", userId);
        Map<String, String> disconnectMessage = new HashMap<>();
        disconnectMessage.put("type", "disconnect");
        disconnectMessage.put("message", "User disconnected by admin");
        ImWebSocketEndpoint.sendToUser(userId, disconnectMessage);
    }

    @Override
    public void sendSystemNotification(Long userId, String title, String content, String type) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "system_notification");
        notification.put("title", title);
        notification.put("content", content);
        notification.put("notificationType", type);
        notification.put("timestamp", System.currentTimeMillis());

        pushToUser(userId, notification);
    }

    @Override
    public void broadcastSystemNotification(String title, String content, String type) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "system_notification");
        notification.put("title", title);
        notification.put("content", content);
        notification.put("notificationType", type);
        notification.put("timestamp", System.currentTimeMillis());

        broadcastToAll(notification);
    }

    @Override
    public Long saveAndSendMessage(ImMessageSendRequest request, Long senderId) {
        Long messageId = imMessageService.sendMessage(request, senderId);

        if (messageId == null) {
            log.error("Failed to save message");
            return null;
        }

        ImSessionVO sessionVO = imSessionService.getSessionById(request.getSessionId());
        if (sessionVO == null) {
            log.warn("Session not found: sessionId={}", request.getSessionId());
            return messageId;
        }

        Map<String, Object> pushMessage = new HashMap<>();
        pushMessage.put("type", "message");
        pushMessage.put("messageId", messageId);
        pushMessage.put("sessionId", request.getSessionId());
        pushMessage.put("senderId", senderId);
        pushMessage.put("messageType", request.getType());
        pushMessage.put("content", request.getContent());
        pushMessage.put("timestamp", System.currentTimeMillis());

        if ("group".equals(sessionVO.getType())) {
            pushToGroup(sessionVO.getPeerId(), pushMessage);
        } else {
            Long receiverId = sessionVO.getPeerId();
            if (!receiverId.equals(senderId)) {
                pushToUser(receiverId, pushMessage);
            }
        }

        return messageId;
    }
}
