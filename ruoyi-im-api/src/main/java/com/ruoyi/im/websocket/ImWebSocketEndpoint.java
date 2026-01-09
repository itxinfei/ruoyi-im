package com.ruoyi.im.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImConversationMemberService;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.service.ImUserService;

import com.ruoyi.im.utils.ImRedisUtil;
import com.ruoyi.im.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private static ImUserMapper staticImUserMapper;
    private static ImRedisUtil staticImRedisUtil;
    private static ImConversationMemberService staticConversationMemberService;
    private static ImConversationService staticConversationService;
    private static ImConversationMemberMapper staticConversationMemberMapper;
    private static boolean staticSecurityEnabled;
    private static Long staticDevUserId;

    @Autowired
    private ImMessageService imMessageService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ImUserService imUserService;

    @Autowired
    private ImUserMapper imUserMapper;

    @Autowired
    private ImRedisUtil imRedisUtil;

    @Autowired
    private ImConversationMemberService conversationMemberService;

    @Autowired
    private ImConversationService conversationService;

    @Autowired
    private ImConversationMemberMapper conversationMemberMapper;

    @Value("${app.security.enabled:true}")
    private boolean securityEnabled;

    @Value("${app.dev.user-id:1}")
    private Long devUserId;

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
    public void setImUserMapper(ImUserMapper imUserMapper) {
        staticImUserMapper = imUserMapper;
    }

    @Autowired
    public void setImRedisUtil(ImRedisUtil imRedisUtil) {
        staticImRedisUtil = imRedisUtil;
    }

    @Autowired
    public void setConversationMemberService(ImConversationMemberService conversationMemberService) {
        staticConversationMemberService = conversationMemberService;
    }

    @Autowired
    public void setConversationService(ImConversationService conversationService) {
        staticConversationService = conversationService;
    }

    @Autowired
    public void setConversationMemberMapper(ImConversationMemberMapper conversationMemberMapper) {
        staticConversationMemberMapper = conversationMemberMapper;
    }

    /**
     * 获取在线用户集合（供 ImMessageController 使用）
     */
    public static Map<Long, Session> getOnlineUsers() {
        return onlineUsers;
    }

    @Value("${app.security.enabled:true}")
    public void setSecurityEnabled(boolean securityEnabled) {
        staticSecurityEnabled = securityEnabled;
    }

    public void setDevUserId(Long devUserId) {
        staticDevUserId = devUserId;
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

            Long userId = null;
            String queryString = session.getQueryString();
            String tokenValue = extractTokenFromQuery(queryString);

            // 尝试从查询参数中直接获取userId（开发环境支持）
            if (queryString != null && queryString.contains("userId=")) {
                try {
                    String[] params = queryString.split("&");
                    for (String param : params) {
                        if (param.startsWith("userId=")) {
                            String userIdStr = param.substring(7);
                            userId = Long.parseLong(userIdStr);
                            log.info("从查询参数中获取到用户ID: userId={}", userId);
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.warn("从查询参数解析userId失败: {}", e.getMessage());
                }
            }

            // 如果没有从查询参数获取到userId，尝试从token中解析
            if (userId == null && tokenValue != null && !tokenValue.isEmpty() && staticJwtUtils != null) {
                try {
                    // 生产环境需要验证token，开发环境跳过验证但仍然解析用户ID
                    if (staticSecurityEnabled) {
                        log.debug("生产环境模式：进行token验证");
                        if (!staticJwtUtils.validateToken(tokenValue)) {
                            log.warn("WebSocket 连接失败: token 无效");
                            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "token 无效或已过期"));
                            return;
                        }
                    } else {
                        log.debug("开发环境模式：跳过token验证，但仍然解析token中的用户ID");
                    }

                    userId = staticJwtUtils.getUserIdFromToken(tokenValue);
                    if (userId != null) {
                        log.info("从token解析到用户ID: userId={}", userId);
                    }
                } catch (Exception e) {
                    log.warn("从token解析用户ID失败: {}", e.getMessage());
                }
            }

            // 如果仍然无法获取userId，使用默认用户ID（仅开发环境）
            if (userId == null) {
                if (!staticSecurityEnabled) {
                    userId = staticDevUserId;
                    if (userId == null) {
                        userId = 1L;
                    }
                    log.warn("无法获取用户ID，使用默认用户ID: userId={}", userId);
                } else {
                    log.warn("WebSocket 连接失败: 无法获取用户ID且安全验证已启用");
                    session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "缺少有效的认证信息"));
                    return;
                }
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

            // 同步更新Redis中的在线状态
            if (staticImRedisUtil != null) {
                staticImRedisUtil.addOnlineUser(userId);
            }

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

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> messageMap = mapper.readValue(message, new TypeReference<Map<String, Object>>() {});

            String type = (String) messageMap.get("type");
            Object payload = messageMap.get("payload");

            switch (type) {
                case "auth":
                    // 处理认证消息
                    handleAuthMessage(session, messageMap);
                    break;
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
                
                // 同步更新Redis中的在线状态
                if (staticImRedisUtil != null) {
                    staticImRedisUtil.removeOnlineUser(userId);
                }

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
            Map<String, Object> messageData = mapper.convertValue(payload, new TypeReference<Map<String, Object>>() {});

            // 兼容 conversationId 和 sessionId 两种字段名（优先使用 conversationId）
            Object idValue = messageData.get("conversationId");
            if (idValue == null) {
                idValue = messageData.get("sessionId");
            }
            if (idValue == null) {
                log.warn("WebSocket消息缺少conversationId或sessionId字段");
                return;
            }
            Long conversationId = Long.valueOf(idValue.toString());

            // 兼容 messageType 和 type 两种字段名
            String messageType = (String) messageData.get("messageType");
            if (messageType == null) {
                messageType = (String) messageData.get("type");
            }
            if (messageType == null) {
                messageType = "TEXT";
            }

            String content = (String) messageData.get("content");

            // 获取可选参数
            String replyToMessageIdStr = messageData.get("replyToMessageId") != null
                ? messageData.get("replyToMessageId").toString() : null;
            String clientMsgId = (String) messageData.get("clientMsgId");

            // 构建消息发送请求
            com.ruoyi.im.dto.message.ImMessageSendRequest request = new com.ruoyi.im.dto.message.ImMessageSendRequest();
            request.setConversationId(conversationId);
            request.setType(messageType);
            request.setContent(content);
            if (replyToMessageIdStr != null) {
                request.setReplyToMessageId(Long.valueOf(replyToMessageIdStr));
            }
            request.setClientMsgId(clientMsgId);

            // 保存消息到数据库
            Long messageId = staticImMessageService.sendMessage(request, userId);

            if (messageId != null) {
                // 获取完整消息对象
                com.ruoyi.im.domain.ImMessage message = new com.ruoyi.im.domain.ImMessage();
                message.setId(messageId);
                message.setConversationId(conversationId);
                message.setSenderId(userId);
                message.setType(messageType);
                message.setContent(content);

                // 广播消息给会话中的所有用户
                broadcastMessageToSession(conversationId, message);

                log.info("消息已发送: messageId={}, conversationId={}, senderId={}", messageId, conversationId, userId);
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
            Map<String, Object> typingData = mapper.convertValue(payload, new TypeReference<Map<String, Object>>() {});

            // 兼容 conversationId 和 sessionId 两种字段名
            Object idValue = typingData.get("conversationId");
            if (idValue == null) {
                idValue = typingData.get("sessionId");
            }
            if (idValue == null) {
                log.warn("正在输入状态消息缺少conversationId或sessionId字段");
                return;
            }
            Long conversationId = Long.valueOf(idValue.toString());

            Object isTypingValue = typingData.get("isTyping");
            boolean isTyping = isTypingValue != null && Boolean.parseBoolean(isTypingValue.toString());

            // 广播正在输入状态
            broadcastTypingStatus(conversationId, userId, isTyping);

        } catch (Exception e) {
            log.error("处理正在输入状态异常", e);
        }
    }

    /**
     * 处理认证消息
     * 验证用户身份并返回认证结果
     *
     * @param session WebSocket会话
     * @param messageData 认证消息数据
     */
    private void handleAuthMessage(Session session, Map<String, Object> messageData) {
        try {
            String token = (String) messageData.get("token");
            Long userId = null;

            // 优先从消息payload中直接获取userId（前端发送的userId）
            Object userIdObj = messageData.get("userId");
            if (userIdObj != null) {
                try {
                    if (userIdObj instanceof Number) {
                        userId = ((Number) userIdObj).longValue();
                        log.info("从认证消息中获取到用户ID: userId={}", userId);
                    } else if (userIdObj instanceof String) {
                        userId = Long.parseLong((String) userIdObj);
                        log.info("从认证消息中获取到用户ID: userId={}", userId);
                    }
                } catch (Exception e) {
                    log.warn("解析消息中的userId失败: {}", e.getMessage());
                }
            }

            // 如果消息中没有userId，尝试从token中解析用户ID
            if (userId == null && token != null && !token.isEmpty() && staticJwtUtils != null) {
                try {
                    // 生产环境需要验证token，开发环境跳过验证但仍然解析用户ID
                    if (staticSecurityEnabled) {
                        if (staticJwtUtils.validateToken(token)) {
                            userId = staticJwtUtils.getUserIdFromToken(token);
                        }
                    } else {
                        // 开发环境：跳过验证但仍然解析token中的用户ID
                        userId = staticJwtUtils.getUserIdFromToken(token);
                        if (userId != null) {
                            log.info("开发环境：从token解析到用户ID: userId={}", userId);
                        }
                    }
                } catch (Exception e) {
                    log.warn("从token解析用户ID失败: {}", e.getMessage());
                }
            }

            // 如果仍然无法获取userId，使用默认用户ID（仅开发环境）
            if (userId == null && !staticSecurityEnabled) {
                userId = staticDevUserId;
                if (userId == null) {
                    userId = 1L;
                }
                log.warn("无法获取用户ID，使用默认用户ID: userId={}", userId);
            }

            if (userId != null) {
                // 更新会话中的用户映射
                Long oldUserId = sessionUserMap.get(session);
                if (oldUserId != null && !oldUserId.equals(userId)) {
                    // 如果当前会话绑定了其他用户ID，移除旧映射
                    onlineUsers.remove(oldUserId);
                }

                sessionUserMap.put(session, userId);
                onlineUsers.put(userId, session);

                // 同步更新Redis中的在线状态
                if (staticImRedisUtil != null) {
                    staticImRedisUtil.addOnlineUser(userId);
                }

                // 发送认证成功消息
                Map<String, Object> response = new HashMap<>();
                response.put("type", "auth_response");
                response.put("success", true);
                response.put("userId", userId);
                response.put("message", "认证成功");

                sendMessage(session, response);
                
                log.info("用户认证成功: userId={}, sessionId={}", userId, session.getId());
            } else {
                // 发送认证失败消息
                Map<String, Object> response = new HashMap<>();
                response.put("type", "auth_response");
                response.put("success", false);
                response.put("message", "认证失败");

                sendMessage(session, response);
                
                log.warn("用户认证失败: sessionId={}", session.getId());
            }
        } catch (Exception e) {
            log.error("处理认证消息异常", e);
            
            // 发送认证失败消息
            try {
                Map<String, Object> response = new HashMap<>();
                response.put("type", "auth_response");
                response.put("success", false);
                response.put("message", "认证异常: " + e.getMessage());

                session.getBasicRemote().sendText(new ObjectMapper().writeValueAsString(response));
            } catch (Exception ex) {
                log.error("发送认证失败消息异常", ex);
            }
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
            Map<String, Object> readData = mapper.convertValue(payload, new TypeReference<Map<String, Object>>() {});

            // 兼容 conversationId 和 sessionId 两种字段名
            Object idValue = readData.get("conversationId");
            if (idValue == null) {
                idValue = readData.get("sessionId");
            }
            if (idValue == null) {
                log.warn("消息已读数据缺少conversationId或sessionId字段");
                return;
            }
            Long conversationId = Long.valueOf(idValue.toString());

            @SuppressWarnings("unchecked")
            List<Long> messageIds = (List<Long>) readData.get("messageIds");

            // 更新消息状态为已读
            if (messageIds != null && !messageIds.isEmpty()) {
                staticImMessageService.markAsRead(conversationId, userId, messageIds);
            }

            // 广播已读回执
            broadcastReadReceipt(conversationId, userId, messageIds);

            log.info("消息已读: conversationId={}, userId={}", conversationId, userId);

        } catch (Exception e) {
            log.error("处理消息已读异常", e);
        }
    }

    /**
     * 广播消息给会话中的所有用户
     * 根据会话ID获取会话成员并推送消息
     *
     * @param conversationId 会话ID
     * @param message 消息对象
     */
    private void broadcastMessageToSession(Long conversationId, com.ruoyi.im.domain.ImMessage message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("type", "message");
            messageMap.put("payload", message);

            String messageJson = mapper.writeValueAsString(messageMap);

            // 直接通过mapper获取会话中的所有成员
            List<com.ruoyi.im.domain.ImConversationMember> members =
                staticConversationMemberMapper.selectByConversationId(conversationId);

            if (members == null || members.isEmpty()) {
                log.warn("会话无成员，无法广播消息: conversationId={}", conversationId);
                return;
            }

            // 向会话中的所有在线用户发送消息
            for (com.ruoyi.im.domain.ImConversationMember member : members) {
                Long targetUserId = member.getUserId();
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

            log.info("消息已广播到会话: conversationId={}, memberCount={}, messageId={}",
                conversationId, members.size(), message.getId());

        } catch (Exception e) {
            log.error("广播消息异常", e);
        }
    }

    /**
     * 广播正在输入状态
     * 向会话中的其他用户发送输入状态通知
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param isTyping 是否正在输入
     */
    private void broadcastTypingStatus(Long conversationId, Long userId, boolean isTyping) {
        try {
            Map<String, Object> statusMap = new HashMap<>();
            statusMap.put("type", "typing");
            statusMap.put("conversationId", conversationId);
            statusMap.put("userId", userId);
            statusMap.put("isTyping", isTyping);
            statusMap.put("timestamp", System.currentTimeMillis());

            // 获取用户信息
            if (staticImUserMapper != null) {
                try {
                    com.ruoyi.im.domain.ImUser user = staticImUserMapper.selectImUserById(userId);
                    if (user != null) {
                        statusMap.put("userName", user.getNickname() != null ? user.getNickname() : user.getUsername());
                    }
                } catch (Exception e) {
                    log.warn("获取用户信息失败: userId={}", userId, e);
                }
            }

            // 只向该会话的其他成员广播，不是所有人
            broadcastToConversation(conversationId, userId, statusMap);

        } catch (Exception e) {
            log.error("广播正在输入状态异常", e);
        }
    }

    /**
     * 广播已读回执
     * 向会话中的其他用户发送消息已读通知
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param messageIds 消息ID列表
     */
    private void broadcastReadReceipt(Long conversationId, Long userId, List<Long> messageIds) {
        try {
            Map<String, Object> receiptMap = new HashMap<>();
            receiptMap.put("type", "read_receipt");
            receiptMap.put("conversationId", conversationId);
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
            // 获取用户详细信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", userId);
            userInfo.put("online", online);
            userInfo.put("timestamp", System.currentTimeMillis());

            // 尝试获取用户详细信息
            if (staticImUserMapper != null) {
                try {
                    com.ruoyi.im.domain.ImUser user = staticImUserMapper.selectImUserById(userId);
                    if (user != null) {
                        userInfo.put("userName", user.getNickname() != null ? user.getNickname() : user.getUsername());
                        userInfo.put("avatar", user.getAvatar());
                        userInfo.put("status", user.getStatus());
                    }
                } catch (Exception e) {
                    log.warn("获取用户信息失败: userId={}", userId, e);
                }
            }

            Map<String, Object> statusMap = new HashMap<>();
            statusMap.put("type", online ? "online" : "offline");
            statusMap.put("userId", userId);
            statusMap.put("userInfo", userInfo);
            statusMap.put("timestamp", System.currentTimeMillis());

            ObjectMapper mapper = new ObjectMapper();
            String messageJson = mapper.writeValueAsString(statusMap);

            broadcastToAllOnline(messageJson);

            log.info("广播在线状态: userId={}, online={}", userId, online);

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
    public static void broadcastToAllOnline(String messageJson) {
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
     * 广播消息给会话中的其他成员（排除发送者）
     *
     * @param conversationId 会话ID
     * @param excludeUserId  排除的用户ID（通常是发送者）
     * @param message        消息对象
     */
    private void broadcastToConversation(Long conversationId, Long excludeUserId, Object message) {
        try {
            // 获取会话成员
            if (staticConversationMemberMapper == null) {
                log.warn("conversationMemberMapper 未初始化，无法广播消息");
                return;
            }

            List<com.ruoyi.im.domain.ImConversationMember> members =
                staticConversationMemberMapper.selectByConversationId(conversationId);

            if (members == null || members.isEmpty()) {
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            String messageJson = mapper.writeValueAsString(message);

            // 向会话中的每个成员发送消息
            for (com.ruoyi.im.domain.ImConversationMember member : members) {
                Long targetUserId = member.getUserId();

                // 不发送给排除的用户（通常是发送者自己）
                if (targetUserId.equals(excludeUserId)) {
                    continue;
                }

                Session targetSession = onlineUsers.get(targetUserId);
                if (targetSession != null && targetSession.isOpen()) {
                    try {
                        targetSession.getBasicRemote().sendText(messageJson);
                    } catch (IOException e) {
                        log.error("发送消息给会话成员失败: userId={}", targetUserId, e);
                    }
                }
            }

        } catch (Exception e) {
            log.error("广播消息到会话异常: conversationId={}", conversationId, e);
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