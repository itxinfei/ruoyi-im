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
    private static com.ruoyi.im.service.ImSessionService staticImSessionService;

    @Autowired
    private ImMessageService imMessageService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ImUserService imUserService;

    @Autowired
    private com.ruoyi.im.service.ImSessionService imSessionService;

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

    @Autowired
    public void setImSessionService(com.ruoyi.im.service.ImSessionService imSessionService) {
        staticImSessionService = imSessionService;
    }

    /**
     * 客户端连接时调用
     * 验证 token 并建立 WebSocket 连接
     *
     * @param session WebSocket 会话
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

            // 验证 token 有效性
            if (!staticJwtUtils.validateToken(tokenValue)) {
                log.warn("WebSocket 连接失败: token 无效");
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "token 无效或已过期"));
                return;
            }

            // 从 JWT token 中解析用户ID
            Long userId = staticJwtUtils.getUserIdFromToken(tokenValue);
            if (userId == null) {
                log.warn("WebSocket 连接失败: 无法从 token 中解析用户ID");
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "token 解析失败"));
                return;
            }

            // 检查用户是否已存在在线连接，如果存在则关闭旧连接
            Session oldSession = onlineUsers.get(userId);
            if (oldSession != null && oldSession.isOpen()) {
                log.info("用户已存在连接，关闭旧连接: userId={}", userId);
                try {
                    oldSession.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "新连接建立"));
                } catch (IOException e) {
                    log.error("关闭旧连接异常", e);
                }
            }

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
                session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "服务器内部错误"));
            } catch (IOException ex) {
                log.error("关闭 WebSocket 会话异常", ex);
            }
        }
    }

    /**
     * 接收客户端消息
     * 根据消息类型分发到不同的处理方法
     *
     * @param message 消息内容
     * @param session WebSocket 会话
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
     * 清理用户会话信息并广播离线状态
     *
     * @param session WebSocket 会话
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
     * 清理异常连接并记录错误日志
     *
     * @param session WebSocket 会话
     * @param error 异常信息
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
     * 保存消息到数据库并推送给会话中的其他用户
     *
     * @param userId 发送者用户ID
     * @param payload 消息数据
     */
    private void handleChatMessage(Long userId, Object payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> messageData = mapper.convertValue(payload, Map.class);

            Long sessionId = Long.valueOf(messageData.get("sessionId").toString());
            String messageType = (String) messageData.get("type");
            String content = (String) messageData.get("content");

            // 构建消息发送请求
            com.ruoyi.im.dto.message.ImMessageSendRequest request = new com.ruoyi.im.dto.message.ImMessageSendRequest();
            request.setSessionId(sessionId);
            request.setType(messageType);
            request.setContent(content);

            // 保存消息到数据库
            Long messageId = staticImMessageService.sendMessage(request, userId);

            if (messageId != null) {
                // 获取完整消息对象
                com.ruoyi.im.domain.ImMessage message = new com.ruoyi.im.domain.ImMessage();
                message.setId(messageId);
                message.setSessionId(sessionId);
                message.setSenderId(userId);
                message.setType(messageType);
                message.setContent(content);

                // 广播消息给会话中的所有用户
                broadcastMessageToSession(sessionId, message);

                log.info("消息已发送: messageId={}, sessionId={}, senderId={}", messageId, sessionId, userId);
            }

        } catch (Exception e) {
            log.error("处理聊天消息异常", e);
        }
    }

    /**
     * 处理正在输入状态
     * 广播用户的输入状态给会话中的其他用户
     *
     * @param userId 用户ID
     * @param payload 输入状态数据
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
     * 更新消息的已读状态并广播已读回执
     *
     * @param userId 用户ID
     * @param payload 已读数据
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
     * 根据会话类型（私聊/群聊）获取相关用户并推送消息
     *
     * @param sessionId 会话ID
     * @param message 消息对象
     */
    private void broadcastMessageToSession(Long sessionId, com.ruoyi.im.domain.ImMessage message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("type", "message");
            messageMap.put("payload", message);

            String messageJson = mapper.writeValueAsString(messageMap);

            // 获取会话信息，确定会话类型和参与者
            com.ruoyi.im.vo.session.ImSessionVO sessionVO = staticImSessionService.getSessionById(sessionId);
            if (sessionVO == null) {
                log.warn("会话不存在，无法广播消息: sessionId={}", sessionId);
                return;
            }

            // 根据会话类型获取目标用户列表
            List<Long> targetUserIds = new ArrayList<>();
            if ("private".equals(sessionVO.getType())) {
                // 私聊会话：发送者和接收者
                targetUserIds.add(message.getSenderId());
                if (message.getReceiverId() != null) {
                    targetUserIds.add(message.getReceiverId());
                }
            } else if ("group".equals(sessionVO.getType())) {
                // 群聊会话：获取群组成员列表
                // TODO: 从群组服务获取群组成员列表
                // 暂时使用会话中的 peerId 作为群组ID
                Long groupId = sessionVO.getPeerId();
                if (groupId != null) {
                    // 这里应该调用群组服务获取成员列表
                    // 暂时简化处理
                    targetUserIds.add(message.getSenderId());
                }
            }

            // 向目标用户发送消息
            for (Long targetUserId : targetUserIds) {
                Session targetSession = onlineUsers.get(targetUserId);
                if (targetSession != null && targetSession.isOpen()) {
                    try {
                        targetSession.getBasicRemote().sendText(messageJson);
                        log.debug("消息已发送给用户: userId={}, messageId={}", targetUserId, message.getId());
                    } catch (IOException e) {
                        log.error("发送消息给用户失败: userId={}", targetUserId, e);
                    }
                }
            }

        } catch (Exception e) {
            log.error("广播消息异常", e);
        }
    }

    /**
     * 广播正在输入状态
     * 向会话中的其他用户发送输入状态通知
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param isTyping 是否正在输入
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
     * 向会话中的其他用户发送消息已读通知
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param messageIds 消息ID列表
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
     * 向所有在线用户发送用户上线/离线通知
     *
     * @param userId 用户ID
     * @param online 是否在线
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
     * 将消息对象序列化为 JSON 并发送给指定的 WebSocket 会话
     *
     * @param session WebSocket 会话
     * @param message 消息对象
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
     * 遍历所有在线用户的 WebSocket 会话并发送消息
     *
     * @param messageJson JSON 格式的消息内容
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
     * 创建标准格式的状态消息对象
     *
     * @param type 消息类型
     * @param userId 用户ID
     * @param data 消息数据
     * @return 状态消息 Map
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
     * 解析 WebSocket 连接 URL 的查询参数，提取认证 token
     *
     * @param queryString 查询字符串（格式：token=xxx&param2=yyy）
     * @return token 值，如果未找到则返回 null
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
     * 统计当前在线用户的总数
     *
     * @return 在线用户数量
     */
    public static int getOnlineUserCount() {
        return onlineUsers.size();
    }

    /**
     * 检查用户是否在线
     * 判断指定用户是否建立了 WebSocket 连接
     *
     * @param userId 用户ID
     * @return true 如果用户在线，否则 false
     */
    public static boolean isUserOnline(Long userId) {
        return onlineUsers.containsKey(userId);
    }

    /**
     * 获取所有在线用户ID
     * 返回当前所有在线用户的 ID 集合
     *
     * @return 在线用户 ID 集合
     */
    public static Set<Long> getOnlineUserIds() {
        return new HashSet<>(onlineUsers.keySet());
    }

    /**
     * 发送消息给指定用户
     * 向指定在线用户发送消息，如果用户不在线则忽略
     *
     * @param userId 目标用户ID
     * @param message 消息对象
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
