package com.ruoyi.im.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 实时通信端点
 *
 * 处理客户端的 WebSocket 连接、消息发送、在线状态管理等
 * 支持单聊、群聊、消息推送等功能
 *
 * @author ruoyi
 */
@Component
@ServerEndpoint("/ws/im")
public class ImWebSocketEndpoint {

    private static final Logger log = LoggerFactory.getLogger(ImWebSocketEndpoint.class);

    /**
     * 存储所有在线用户的 WebSocket 会话（用户ID -> 会话）
     */
    private static final Map<Long, Session> onlineUsers = new ConcurrentHashMap<>();

    /**
     * 存储会话到用户ID的映射（会话 -> 用户ID）
     */
    private static final Map<Session, Long> sessionUserMap = new ConcurrentHashMap<>();

    private static ImMessageService staticImMessageService;
    private static JwtUtils staticJwtUtils;
    private static ImUserService staticImUserService;

    @Autowired
    private ImMessageService imMessageService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ImUserService imUserService;

    @Autowired
    public void setImMessageService(ImMessageService imMessageService) {
        staticImMessageService = imMessageService;
    }

    @Autowired
    public void setJwtUtils(JwtUtils jwtUtils) {
        staticJwtUtils = jwtUtils;
    }

    @Autowired
    public void setImUserService(ImUserService imUserService) {
        staticImUserService = imUserService;
    }

    /**
     * 客户端连接时调用
     */
    @OnOpen
    public void onOpen(Session session) {
        try {
            log.info("WebSocket 连接请求: sessionId={}", session.getId());

            // 从查询参数中获取 token
            String queryString = session.getQueryString();
            String tokenValue = extractTokenFromQuery(queryString);

            if (tokenValue == null || tokenValue.isEmpty()) {
                log.warn("WebSocket 连接失败: 缺少 token");
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "缺少认证 token"));
                return;
            }

            // 验证 token 并获取用户ID（暂时简化处理）
            // TODO: 从JWT token中解析用户ID
            Long userId = 1L;

            // 保存用户会话
            onlineUsers.put(userId, session);
            sessionUserMap.put(session, userId);

            log.info("用户上线: userId={}, sessionId={}", userId, session.getId());

            // 广播用户上线消息
            broadcastOnlineStatus(userId, true);

            // 发送连接成功消息给客户端
            sendMessage(session, buildStatusMessage("connected", userId, true));

        } catch (Exception e) {
            log.error("WebSocket 连接处理异常", e);
            try {
                session.close();
            } catch (IOException ex) {
                log.error("关闭 WebSocket 会话异常", ex);
            }
        }
    }

    /**
     * 接收客户端消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            Long userId = sessionUserMap.get(session);
            if (userId == null) {
                log.warn("收到消息但用户未认证: sessionId={}", session.getId());
                return;
            }

            log.debug("收到消息: userId={}, message={}", userId, message);

            // 解析消息
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> messageMap = mapper.readValue(message, Map.class);

            String type = (String) messageMap.get("type");
            Object payload = messageMap.get("payload");

            switch (type) {
                case "message":
                    // 处理聊天消息
                    handleChatMessage(userId, payload);
                    break;
                case "typing":
                    // 处理正在输入状态
                    handleTypingStatus(userId, payload);
                    break;
                case "read":
                    // 处理消息已读
                    handleReadReceipt(userId, payload);
                    break;
                case "ping":
                    // 处理心跳
                    sendMessage(session, buildStatusMessage("pong", userId, true));
                    break;
                default:
                    log.warn("未知消息类型: type={}", type);
            }

        } catch (Exception e) {
            log.error("处理 WebSocket 消息异常", e);
        }
    }

    /**
     * 客户端断开连接时调用
     */
    @OnClose
    public void onClose(Session session) {
        try {
            Long userId = sessionUserMap.remove(session);
            if (userId != null) {
                onlineUsers.remove(userId);
                log.info("用户离线: userId={}, sessionId={}", userId, session.getId());

                // 广播用户离线消息
                broadcastOnlineStatus(userId, false);
            }
        } catch (Exception e) {
            log.error("处理 WebSocket 关闭异常", e);
        }
    }

    /**
     * 连接异常时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 异常: sessionId={}", session.getId(), error);
        try {
            Long userId = sessionUserMap.remove(session);
            if (userId != null) {
                onlineUsers.remove(userId);
            }
            session.close();
        } catch (IOException e) {
            log.error("关闭异常连接失败", e);
        }
    }

    /**
     * 处理聊天消息
     */
    private void handleChatMessage(Long userId, Object payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> messageData = mapper.convertValue(payload, Map.class);

            Long sessionId = Long.valueOf(messageData.get("sessionId").toString());
            String messageType = (String) messageData.get("type");
            String content = (String) messageData.get("content");

            // TODO: 保存消息到数据库
            // 暂时返回模拟ID
            Long messageId = 1L;

            if (messageId != null) {
                // 获取完整消息对象
                // TODO: 从数据库获取消息
                ImMessage message = new ImMessage();
                message.setId(messageId);
                message.setSessionId(sessionId);
                message.setSenderId(userId);
                message.setType(messageType);
                message.setContent(content);

                // 广播消息给会话中的所有用户
                broadcastMessageToSession(sessionId, message);

                log.info("消息已发送: messageId={}, sessionId={}", messageId, sessionId);
            }

        } catch (Exception e) {
            log.error("处理聊天消息异常", e);
        }
    }

    /**
     * 处理正在输入状态
     */
    private void handleTypingStatus(Long userId, Object payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> typingData = mapper.convertValue(payload, Map.class);

            Long sessionId = Long.valueOf(typingData.get("sessionId").toString());
            boolean isTyping = (boolean) typingData.get("isTyping");

            // 广播正在输入状态
            broadcastTypingStatus(sessionId, userId, isTyping);

        } catch (Exception e) {
            log.error("处理正在输入状态异常", e);
        }
    }

    /**
     * 处理消息已读
     */
    private void handleReadReceipt(Long userId, Object payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> readData = mapper.convertValue(payload, Map.class);

            Long sessionId = Long.valueOf(readData.get("sessionId").toString());
            List<Long> messageIds = (List<Long>) readData.get("messageIds");

            // 更新消息状态为已读
            if (messageIds != null && !messageIds.isEmpty()) {
                staticImMessageService.markAsRead(sessionId, userId, messageIds);
            }

            // 广播已读回执
            broadcastReadReceipt(sessionId, userId, messageIds);

            log.info("消息已读: sessionId={}, userId={}", sessionId, userId);

        } catch (Exception e) {
            log.error("处理消息已读异常", e);
        }
    }

    /**
     * 广播消息给会话中的所有用户
     */
    private void broadcastMessageToSession(Long sessionId, ImMessage message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("type", "message");
            messageMap.put("payload", message);

            String messageJson = mapper.writeValueAsString(messageMap);

            // 这里应该根据 sessionId 获取会话中的所有用户
            // 简化起见，广播给所有在线用户
            broadcastToAllOnline(messageJson);

        } catch (Exception e) {
            log.error("广播消息异常", e);
        }
    }

    /**
     * 广播正在输入状态
     */
    private void broadcastTypingStatus(Long sessionId, Long userId, boolean isTyping) {
        try {
            Map<String, Object> statusMap = new HashMap<>();
            statusMap.put("type", "typing");
            statusMap.put("sessionId", sessionId);
            statusMap.put("userId", userId);
            statusMap.put("isTyping", isTyping);

            ObjectMapper mapper = new ObjectMapper();
            String messageJson = mapper.writeValueAsString(statusMap);

            broadcastToAllOnline(messageJson);

        } catch (Exception e) {
            log.error("广播正在输入状态异常", e);
        }
    }

    /**
     * 广播已读回执
     */
    private void broadcastReadReceipt(Long sessionId, Long userId, List<Long> messageIds) {
        try {
            Map<String, Object> receiptMap = new HashMap<>();
            receiptMap.put("type", "read_receipt");
            receiptMap.put("sessionId", sessionId);
            receiptMap.put("userId", userId);
            receiptMap.put("messageIds", messageIds);
            receiptMap.put("timestamp", System.currentTimeMillis());

            ObjectMapper mapper = new ObjectMapper();
            String messageJson = mapper.writeValueAsString(receiptMap);

            broadcastToAllOnline(messageJson);

        } catch (Exception e) {
            log.error("广播已读回执异常", e);
        }
    }

    /**
     * 广播用户在线状态
     */
    private void broadcastOnlineStatus(Long userId, boolean online) {
        try {
            Map<String, Object> statusMap = buildStatusMessage("online", userId, online);

            ObjectMapper mapper = new ObjectMapper();
            String messageJson = mapper.writeValueAsString(statusMap);

            broadcastToAllOnline(messageJson);

        } catch (Exception e) {
            log.error("广播在线状态异常", e);
        }
    }

    /**
     * 发送消息给指定会话
     */
    private void sendMessage(Session session, Object message) {
        try {
            if (session.isOpen()) {
                ObjectMapper mapper = new ObjectMapper();
                String messageJson = mapper.writeValueAsString(message);
                session.getBasicRemote().sendText(messageJson);
            }
        } catch (IOException e) {
            log.error("发送消息异常", e);
        }
    }

    /**
     * 广播消息给所有在线用户
     */
    private void broadcastToAllOnline(String messageJson) {
        List<Session> sessions = new ArrayList<>(onlineUsers.values());
        for (Session session : sessions) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(messageJson);
                } catch (IOException e) {
                    log.error("发送消息失败: sessionId={}", session.getId(), e);
                }
            }
        }
    }

    /**
     * 构建状态消息
     */
    private Map<String, Object> buildStatusMessage(String type, Long userId, Object data) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", type);
        message.put("userId", userId);
        message.put("data", data);
        message.put("timestamp", System.currentTimeMillis());
        return message;
    }

    /**
     * 从查询字符串中提取 token
     */
    private String extractTokenFromQuery(String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            return null;
        }

        String[] params = queryString.split("&");
        for (String param : params) {
            if (param.startsWith("token=")) {
                return param.substring(6);
            }
        }
        return null;
    }

    /**
     * 获取在线用户数
     */
    public static int getOnlineUserCount() {
        return onlineUsers.size();
    }

    /**
     * 检查用户是否在线
     */
    public static boolean isUserOnline(Long userId) {
        return onlineUsers.containsKey(userId);
    }

    /**
     * 获取所有在线用户ID
     */
    public static Set<Long> getOnlineUserIds() {
        return new HashSet<>(onlineUsers.keySet());
    }

    /**
     * 发送消息给指定用户
     */
    public static void sendToUser(Long userId, Object message) {
        Session session = onlineUsers.get(userId);
        if (session != null && session.isOpen()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String messageJson = mapper.writeValueAsString(message);
                session.getBasicRemote().sendText(messageJson);
            } catch (Exception e) {
                LoggerFactory.getLogger(ImWebSocketEndpoint.class).error("发送消息给用户失败: userId={}", userId, e);
            }
        }
    }
}
