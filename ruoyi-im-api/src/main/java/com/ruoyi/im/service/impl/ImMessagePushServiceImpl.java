package com.ruoyi.im.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.im.dto.message.ImMessageSendRequest;
import com.ruoyi.im.service.ImGroupMemberService;
import com.ruoyi.im.service.ImMessagePushService;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.service.ImSessionService;
import com.ruoyi.im.vo.session.ImSessionVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import javax.websocket.Session;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IM Message Push Service Implementation
 *
 * Provides WebSocket message push, user online management, message broadcast
 *
 * @author ruoyi
 */
@Service
public class ImMessagePushServiceImpl implements ImMessagePushService {

    private static final Logger log = LoggerFactory.getLogger(ImMessagePushServiceImpl.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<Long, Session> onlineUsers = new ConcurrentHashMap<>();

    private final Map<Long, Set<Long>> sessionUsers = new ConcurrentHashMap<>();

    private final Map<Session, Long> sessionUserMap = new ConcurrentHashMap<>();

    @Autowired
    private ImMessageService imMessageService;

    @Autowired
    private ImSessionService imSessionService;

    @Autowired
    private ImGroupMemberService imGroupMemberService;

    @Override
    public boolean pushToUser(Long userId, Object message) {
        Session session = onlineUsers.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String messageJson = objectMapper.writeValueAsString(message);
                session.getBasicRemote().sendText(messageJson);
                log.debug("Pushed message to user: userId={}", userId);
                return true;
            } catch (IOException e) {
                log.error("Failed to push message to user: userId={}", userId, e);
            }
        }
        return false;
    }

    @Override
    public int pushToSession(Long sessionId, Object message) {
        Set<Long> userIds = sessionUsers.get(sessionId);
        if (userIds == null || userIds.isEmpty()) {
            log.warn("No online users in session: sessionId={}", sessionId);
            return 0;
        }

        int successCount = 0;
        String messageJson;
        try {
            messageJson = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Failed to serialize message", e);
            return 0;
        }

        for (Long userId : userIds) {
            Session session = onlineUsers.get(userId);
            if (session != null && session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(messageJson);
                    successCount++;
                } catch (IOException e) {
                    log.error("Failed to push message to user: userId={}", userId, e);
                }
            }
        }

        log.debug("Pushed message to session: sessionId={}, count={}", sessionId, successCount);
        return successCount;
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
        String messageJson;
        try {
            messageJson = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Failed to serialize message", e);
            return 0;
        }

        for (ImGroupMember member : members) {
            Long userId = member.getUserId();
            Session session = onlineUsers.get(userId);
            if (session != null && session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(messageJson);
                    successCount++;
                } catch (IOException e) {
                    log.error("Failed to push message to group member: userId={}", userId, e);
                }
            }
        }

        log.debug("Pushed message to group: groupId={}, count={}", groupId, successCount);
        return successCount;
    }

    @Override
    public int broadcastToAll(Object message) {
        if (onlineUsers.isEmpty()) {
            log.warn("No online users");
            return 0;
        }

        int successCount = 0;
        String messageJson;
        try {
            messageJson = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Failed to serialize message", e);
            return 0;
        }

        for (Map.Entry<Long, Session> entry : onlineUsers.entrySet()) {
            Session session = entry.getValue();
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(messageJson);
                    successCount++;
                } catch (IOException e) {
                    log.error("Failed to broadcast to user: userId={}", entry.getKey(), e);
                }
            }
        }

        log.info("Broadcasted message: total={}, success={}", onlineUsers.size(), successCount);
        return successCount;
    }

    @Override
    public void pushOnlineStatus(Long userId, boolean online) {
        Map<String, Object> statusMessage = new HashMap<>();
        statusMessage.put("type", "online_status");
        statusMessage.put("userId", userId);
        statusMessage.put("online", online);
        statusMessage.put("timestamp", System.currentTimeMillis());

        for (Map.Entry<Long, Session> entry : onlineUsers.entrySet()) {
            if (!entry.getKey().equals(userId)) {
                Session session = entry.getValue();
                if (session.isOpen()) {
                    try {
                        String messageJson = objectMapper.writeValueAsString(statusMessage);
                        session.getBasicRemote().sendText(messageJson);
                    } catch (Exception e) {
                        log.error("Failed to push online status", e);
                    }
                }
            }
        }

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

        Set<Long> userIds = sessionUsers.get(sessionId);
        if (userIds == null) {
            return;
        }

        String messageJson;
        try {
            messageJson = objectMapper.writeValueAsString(typingMessage);
        } catch (Exception e) {
            log.error("Failed to serialize typing status", e);
            return;
        }

        for (Long id : userIds) {
            if (!id.equals(userId)) {
                Session session = onlineUsers.get(id);
                if (session != null && session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(messageJson);
                    } catch (IOException e) {
                        log.error("Failed to push typing status", e);
                    }
                }
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

        Set<Long> userIds = sessionUsers.get(sessionId);
        if (userIds == null) {
            return;
        }

        String messageJson;
        try {
            messageJson = objectMapper.writeValueAsString(receiptMessage);
        } catch (Exception e) {
            log.error("Failed to serialize read receipt", e);
            return;
        }

        for (Long id : userIds) {
            if (!id.equals(userId)) {
                Session session = onlineUsers.get(id);
                if (session != null && session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(messageJson);
                    } catch (IOException e) {
                        log.error("Failed to push read receipt", e);
                    }
                }
            }
        }

        log.debug("Pushed read receipt: sessionId={}, count={}", sessionId, messageIds.size());
    }

    @Override
    public int getOnlineUserCount() {
        return onlineUsers.size();
    }

    @Override
    public Set<Long> getOnlineUserIds() {
        return new HashSet<>(onlineUsers.keySet());
    }

    @Override
    public boolean isUserOnline(Long userId) {
        return onlineUsers.containsKey(userId);
    }

    @Override
    public void disconnectUser(Long userId) {
        Session session = onlineUsers.remove(userId);
        if (session == null) {
            log.warn("User session not found: userId={}", userId);
            return;
        }

        for (Map.Entry<Long, Set<Long>> entry : sessionUsers.entrySet()) {
            Set<Long> userIds = entry.getValue();
            if (userIds != null) {
                userIds.remove(userId);
            }
        }

        sessionUserMap.remove(session);

        try {
            session.close(new javax.websocket.CloseReason(
                javax.websocket.CloseReason.CloseCodes.NORMAL_CLOSURE,
                "User disconnected"
            ));
        } catch (IOException e) {
            log.error("Failed to close session: userId={}", userId, e);
        }

        log.info("User disconnected: userId={}, online={}", userId, onlineUsers.size());
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

    public void addUserSession(Long userId, Session session) {
        onlineUsers.put(userId, session);
        sessionUserMap.put(session, userId);
    }

    public void addSessionUser(Long sessionId, Long userId) {
        sessionUsers.computeIfAbsent(sessionId, k -> new HashSet<>()).add(userId);
    }

    public Long removeUserSession(Session session) {
        Long userId = sessionUserMap.remove(session);
        if (userId != null) {
            onlineUsers.remove(userId);
        }
        return userId;
    }

    public Session getUserSession(Long userId) {
        return onlineUsers.get(userId);
    }
}
