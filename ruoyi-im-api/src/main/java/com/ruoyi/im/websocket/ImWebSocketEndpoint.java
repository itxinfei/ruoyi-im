package com.ruoyi.im.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImConversationSyncService;
import com.ruoyi.im.service.ImDeviceService;
import com.ruoyi.im.service.ImMessageAckService;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.service.ImWebSocketBroadcastService;
import com.ruoyi.im.util.ImRedisUtil;
import com.ruoyi.im.util.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

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

    /**
     * Spring ApplicationContext（用于获取Bean）
     */
    private static ApplicationContext applicationContext;

    private static ImMessageService staticImMessageService;
    private static JwtUtils staticJwtUtils;
    private static ImUserMapper staticImUserMapper;
    private static ImRedisUtil staticImRedisUtil;
    private static ImWebSocketBroadcastService staticBroadcastService;
    private static ImDeviceService staticDeviceService;
    private static ImMessageAckService staticMessageAckService;
    private static ImConversationSyncService staticConversationSyncService;
    private static boolean staticSecurityEnabled;
    // staticDevUserId 已删除,未使用

    @Autowired
    public void setImMessageService(ImMessageService imMessageService) {
        staticImMessageService = imMessageService;
    }

    @Autowired
    public void setJwtUtils(JwtUtils jwtUtils) {
        staticJwtUtils = jwtUtils;
    }

    @Autowired
    public void setBroadcastService(ImWebSocketBroadcastService broadcastService) {
        staticBroadcastService = broadcastService;
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
    public void setDeviceService(ImDeviceService deviceService) {
        staticDeviceService = deviceService;
    }

    @Autowired
    public void setMessageAckService(ImMessageAckService messageAckService) {
        staticMessageAckService = messageAckService;
    }

    @Autowired
    public void setConversationSyncService(ImConversationSyncService conversationSyncService) {
        staticConversationSyncService = conversationSyncService;
    }

    /**
     * 注入ApplicationContext（用于获取其他Bean）
     */
    @Autowired
    public void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
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

            // 获取服务器ID（支持集群部署，可从配置文件或环境变量获取）
            String serverId = System.getProperty("server.id", "default-server");

            // 获取客户端信息
            String clientInfo = buildClientInfo(session);

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

            // 如果仍然无法获取userId，连接暂时挂起，等待客户端发送认证消息
            // 这种情况下，不立即关闭连接，允许客户端通过auth消息进行认证
            if (userId == null) {
                log.warn("WebSocket连接时无法获取用户ID，等待客户端发送认证消息: sessionId={}", session.getId());
                // 将session标记为未认证状态，使用特殊标记
                sessionUserMap.put(session, -1L); // -1 表示未认证
                // 发送需要认证的消息
                sendMessage(session, buildStatusMessage("auth_required", null, "请发送认证消息"));
                return;
            }

            // 使用同步块确保连接更新的原子性，防止竞态条件
            synchronized (onlineUsers) {
                // 检查用户是否已存在在线连接，如果存在则关闭旧连接
                Session oldSession = onlineUsers.get(userId);
                if (oldSession != null && oldSession.isOpen()) {
                    log.info("用户已存在连接，关闭旧连接: userId={}, oldSessionId={}, newSessionId={}",
                            userId, oldSession.getId(), session.getId());
                    try {
                        // 先从映射中移除，防止并发问题
                        sessionUserMap.remove(oldSession);
                        oldSession.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "新连接建立"));
                    } catch (IOException e) {
                        log.error("关闭旧连接异常: userId={}", userId, e);
                    }
                }

                // 保存用户会话
                onlineUsers.put(userId, session);
                sessionUserMap.put(session, userId);
            }

            // 同步更新Redis中的在线状态
            if (staticImRedisUtil != null) {
                staticImRedisUtil.addOnlineUser(userId);
                // 记录会话信息
                staticImRedisUtil.recordSessionInfo(userId, serverId, session.getId(), clientInfo);
                // 清除断线信息（如果有的话）
                staticImRedisUtil.clearDisconnectInfo(userId);
            }

            // 注册设备（从查询参数获取设备信息）
            if (staticDeviceService != null) {
                String deviceId = extractParamFromQuery(queryString, "deviceId");
                String deviceType = extractParamFromQuery(queryString, "deviceType");
                String deviceName = extractParamFromQuery(queryString, "deviceName");
                String clientVersion = extractParamFromQuery(queryString, "clientVersion");
                String osVersion = extractParamFromQuery(queryString, "osVersion");

                // 如果没有提供deviceId，生成一个默认的
                if (deviceId == null || deviceId.isEmpty()) {
                    deviceId = "web_" + session.getId();
                }

                // 如果没有提供deviceType，默认为web
                if (deviceType == null || deviceType.isEmpty()) {
                    deviceType = "web";
                }

                // 获取客户端IP
                String ipAddress = extractClientIpFromSession(session);

                staticDeviceService.registerDevice(userId, deviceId, deviceType, deviceName,
                        clientVersion, osVersion, ipAddress);
            }

            log.info("用户上线: userId={}, sessionId={}, serverId={}", userId, session.getId(), serverId);

            // 广播用户上线消息
            if (staticBroadcastService != null) {
                staticBroadcastService.broadcastOnlineStatus(userId, true);
            }

            // 发送连接成功消息给客户端
            sendMessage(session, buildStatusMessage("connected", userId, true));

            // 推送离线消息（异步执行，避免阻塞连接建立）
            pushOfflineMessages(userId);

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
    @SuppressWarnings("unchecked")
    public void onMessage(String message, Session session) {
        try {
            Long userId = sessionUserMap.get(session);
            if (userId == null) {
                log.warn("收到消息但用户未认证: sessionId={}", session.getId());
                sendMessage(session, buildStatusMessage("error", null, "请先认证"));
                return;
            }

            // 检查是否为未认证状态（-1表示未认证）
            if (userId == -1L) {
                // 只允许认证消息通过
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> messageMap = mapper.readValue(message, new TypeReference<Map<String, Object>>() {
                });
                String type = (String) messageMap.get("type");
                if ("auth".equals(type)) {
                    // 传入 data 字段的内容，而不是整个 messageMap
                    Object payload = messageMap.get("data");
                    if (payload instanceof Map) {
                        // noinspection unchecked
                        Map<String, Object> authData = (Map<String, Object>) payload;
                        processAuthMessage(session, authData);
                    } else {
                        sendMessage(session, buildStatusMessage("error", null, "认证消息格式错误"));
                    }
                } else {
                    sendMessage(session, buildStatusMessage("error", null, "请先认证"));
                }
                return;
            }

            log.debug("收到消息: userId={}, message={}", userId, message);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> messageMap = mapper.readValue(message, new TypeReference<Map<String, Object>>() {
            });

            String type = (String) messageMap.get("type");
            Object payload = messageMap.get("data");

            switch (type) {
                case "auth":
                    // 处理认证消息（允许重新认证）
                    // 注意：payload 是 data 字段的内容，包含 token、userId 等认证信息
                    if (payload instanceof Map) {
                        // noinspection unchecked
                        Map<String, Object> authData = (Map<String, Object>) payload;
                        processAuthMessage(session, authData);
                    } else {
                        sendMessage(session, buildStatusMessage("error", null, "认证消息格式错误"));
                    }
                    break;
                case "message":
                    // 处理聊天消息
                    processChatMessage(userId, payload);
                    break;
                case "typing":
                case "stop-typing": {
                    // 处理正在输入状态（typing 和 stop-typing）
                    // 对于 stop-typing，设置 isTyping 为 false
                    Object typingPayload = payload;
                    if ("stop-typing".equals(type) && payload instanceof Map) {
                        // noinspection unchecked
                        Map<String, Object> enhancedPayload = new HashMap<>((Map<String, Object>) payload);
                        enhancedPayload.put("isTyping", false);
                        typingPayload = enhancedPayload;
                    }
                    processTypingStatus(userId, typingPayload);
                    break;
                }
                case "read":
                    // 处理消息已读
                    processReadReceipt(userId, payload);
                    break;
                case "ping":
                    // 处理心跳并更新Redis中的心跳时间
                    if (staticImRedisUtil != null) {
                        staticImRedisUtil.updateHeartbeat(userId);
                    }
                    sendMessage(session, buildStatusMessage("pong", userId, true));
                    break;
                case "ack":
                    // 处理客户端ACK确认（送达、接收、已读）
                    processAckMessage(userId, payload, session);
                    break;
                case "conversation_event":
                    // 处理会话同步事件（置顶、免打扰、归档、删除）
                    processConversationEvent(userId, payload, session);
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
            Long userId = null;
            // 使用同步块确保原子性操作
            synchronized (onlineUsers) {
                userId = sessionUserMap.remove(session);
                if (userId != null && userId != -1L) { // -1 表示未认证，不需要处理在线状态
                    // 检查当前session是否是该用户的活跃连接
                    Session currentSession = onlineUsers.get(userId);
                    // 只有当关闭的session是当前活跃连接时才清理
                    if (currentSession != null && currentSession.getId().equals(session.getId())) {
                        onlineUsers.remove(userId);
                    } else if (currentSession == null) {
                        // 如果没有找到连接，也清理
                        onlineUsers.remove(userId);
                    }
                    // 如果是其他session，说明用户已经在新连接上，不需要清理onlineUsers
                }
            }

            if (userId != null && userId != -1L) { // -1 表示未认证，不需要广播离线消息
                // 设置设备离线
                if (staticDeviceService != null) {
                    String deviceId = extractDeviceIdFromSession(session);
                    if (deviceId != null) {
                        staticDeviceService.setDeviceOffline(userId, deviceId);
                    }
                }

                // 同步更新Redis中的在线状态
                if (staticImRedisUtil != null) {
                    staticImRedisUtil.removeOnlineUser(userId);
                    staticImRedisUtil.removeSessionInfo(userId);
                    // 记录断线信息
                    staticImRedisUtil.recordDisconnectInfo(userId, "client_close", System.currentTimeMillis());
                }

                log.info("用户离线: userId={}, sessionId={}", userId, session.getId());
                // 广播用户离线消息
                if (staticBroadcastService != null) {
                    staticBroadcastService.broadcastOnlineStatus(userId, false);
                }
            } else if (userId != null && userId == -1L) {
                log.info("未认证会话关闭: sessionId={}", session.getId());
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
     * @param error   异常信息
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 异常: sessionId={}", session.getId(), error);
        try {
            Long userId = sessionUserMap.remove(session);
            // 只有当userId不是-1（未认证标记）时，才从onlineUsers中移除
            if (userId != null && userId != -1L) {
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
     * 支持ACK响应和错误处理
     *
     * @param userId  发送者用户ID
     * @param payload 消息数据
     */
    private void processChatMessage(Long userId, Object payload) {
        Session senderSession = onlineUsers.get(userId);
        String clientMsgId = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> messageData = mapper.convertValue(payload, new TypeReference<Map<String, Object>>() {
            });

            // 兼容 conversationId 和 sessionId 两种字段名（优先使用 conversationId）
            Object idValue = messageData.get("conversationId");
            if (idValue == null) {
                idValue = messageData.get("sessionId");
            }
            if (idValue == null) {
                log.warn("WebSocket消息缺少conversationId或sessionId字段");
                sendErrorMessage(senderSession, null, "MISSING_CONVERSATION_ID", "缺少会话ID");
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
                    ? messageData.get("replyToMessageId").toString()
                    : null;
            clientMsgId = (String) messageData.get("clientMsgId");

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
            com.ruoyi.im.vo.message.ImMessageVO vo = staticImMessageService.sendMessage(request, userId);
            Long messageId = vo != null ? vo.getId() : null;

            if (messageId != null) {
                // 立即返回ACK确认
                sendAckMessage(senderSession, clientMsgId, messageId);

                log.info("消息已发送: messageId={}, conversationId={}, senderId={}", messageId, conversationId, userId);
            } else {
                // 消息保存失败
                sendErrorMessage(senderSession, clientMsgId, "SAVE_FAILED", "消息保存失败");
            }

        } catch (Exception e) {
            log.error("处理聊天消息异常", e);
            // 发送错误响应给客户端
            sendErrorMessage(senderSession, clientMsgId, "PROCESS_ERROR", e.getMessage());
        }
    }

    /**
     * 发送ACK确认消息
     * 确认已收到消息并返回消息ID
     *
     * @param session     WebSocket会话
     * @param clientMsgId 客户端消息ID
     * @param messageId   服务器消息ID
     */
    private void sendAckMessage(Session session, String clientMsgId, Long messageId) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            Map<String, Object> ackMessage = new HashMap<>();
            ackMessage.put("type", "message_ack");
            ackMessage.put("clientMsgId", clientMsgId);
            ackMessage.put("messageId", messageId);
            ackMessage.put("timestamp", System.currentTimeMillis());

            sendMessage(session, ackMessage);
            log.debug("发送ACK确认: clientMsgId={}, messageId={}", clientMsgId, messageId);
        } catch (Exception e) {
            log.error("发送ACK消息失败", e);
        }
    }

    /**
     * 发送错误消息
     *
     * @param session      WebSocket会话
     * @param clientMsgId  客户端消息ID
     * @param errorCode    错误码
     * @param errorMessage 错误信息
     */
    private void sendErrorMessage(Session session, String clientMsgId, String errorCode, String errorMessage) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("type", "message_error");
            errorMsg.put("clientMsgId", clientMsgId);
            errorMsg.put("errorCode", errorCode);
            errorMsg.put("errorMessage", errorMessage);
            errorMsg.put("timestamp", System.currentTimeMillis());

            sendMessage(session, errorMsg);
            log.warn("发送错误消息: clientMsgId={}, errorCode={}, errorMessage={}",
                    clientMsgId, errorCode, errorMessage);
        } catch (Exception e) {
            log.error("发送错误消息失败", e);
        }
    }

    /**
     * 处理正在输入状态
     * 广播用户的输入状态给会话中的其他用户
     *
     * @param userId  用户ID
     * @param payload 输入状态数据
     */
    private void processTypingStatus(Long userId, Object payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> typingData = mapper.convertValue(payload, new TypeReference<Map<String, Object>>() {
            });

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
            if (staticBroadcastService != null) {
                staticBroadcastService.broadcastTypingStatus(conversationId, userId, isTyping);
            }

        } catch (Exception e) {
            log.error("处理正在输入状态异常", e);
        }
    }

    /**
     * 处理认证消息
     * 验证用户身份并返回认证结果
     *
     * @param session     WebSocket会话
     * @param messageData 认证消息数据
     */
    private void processAuthMessage(Session session, Map<String, Object> messageData) {
        try {
            String token = (String) messageData.get("token");
            Long userId = null;
            boolean isNewLogin = false; // 标记是否是新登录（从未认证到认证）

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

            // 验证userId对应的用户是否存在
            if (userId != null && staticImUserMapper != null) {
                try {
                    com.ruoyi.im.domain.ImUser user = staticImUserMapper.selectImUserById(userId);
                    if (user == null) {
                        log.error("认证失败：用户不存在，userId={}", userId);
                        userId = null;
                    } else {
                        log.info("用户验证通过: userId={}, username={}", userId, user.getUsername());
                    }
                } catch (Exception e) {
                    log.error("验证用户存在性失败: userId={}", userId, e);
                }
            }

            if (userId != null) {
                // 更新会话中的用户映射
                Long oldUserId = sessionUserMap.get(session);

                // 检查是否是新登录（从-1未认证状态到认证状态）
                if (oldUserId != null && oldUserId == -1L) {
                    isNewLogin = true;
                    // 移除未认证标记
                    sessionUserMap.remove(session);
                } else if (oldUserId != null && !oldUserId.equals(userId)) {
                    // 如果当前会话绑定了其他用户ID，移除旧映射
                    onlineUsers.remove(oldUserId);
                }

                // 使用同步块确保连接更新的原子性，防止竞态条件
                synchronized (onlineUsers) {
                    // 检查用户是否已存在在线连接，如果存在则关闭旧连接
                    Session oldSession = onlineUsers.get(userId);
                    if (oldSession != null && oldSession.isOpen() && !oldSession.getId().equals(session.getId())) {
                        log.info("用户已存在连接，关闭旧连接: userId={}, oldSessionId={}, newSessionId={}",
                                userId, oldSession.getId(), session.getId());
                        try {
                            // 从sessionUserMap中删除旧会话
                            sessionUserMap.remove(oldSession);
                            onlineUsers.remove(userId);
                            oldSession.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "新连接建立"));
                        } catch (IOException e) {
                            log.error("关闭旧连接异常: userId={}", userId, e);
                        }
                    }

                    // 保存用户会话
                    sessionUserMap.put(session, userId);
                    onlineUsers.put(userId, session);
                }

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

                // 如果是新登录（从此前状态变更为已认证），广播上线消息并推送离线消息
                if (isNewLogin && staticBroadcastService != null) {
                    staticBroadcastService.broadcastOnlineStatus(userId, true);
                    pushOfflineMessages(userId);
                }
            } else {
                // 发送认证失败消息
                Map<String, Object> response = new HashMap<>();
                response.put("type", "auth_response");
                response.put("success", false);
                response.put("message", "认证失败：无法获取有效的用户ID");

                sendMessage(session, response);

                log.warn("用户认证失败: sessionId={}, 无法获取有效用户ID", session.getId());
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
     * @param userId  用户ID
     * @param payload 已读数据
     */
    private void processReadReceipt(Long userId, Object payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> readData = mapper.convertValue(payload, new TypeReference<Map<String, Object>>() {
            });

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

                // 广播已读回执
                if (staticBroadcastService != null) {
                    // 这里注意：Service目前的方法是 broadcastReadReceipt(Long conversationId, Long
                    // lastReadMessageId, Long userId)
                    // 而这里是 messageIds。为了简化，我们取最后一个ID
                    Long lastId = messageIds.get(messageIds.size() - 1);
                    staticBroadcastService.broadcastReadReceipt(conversationId, lastId, userId);
                }
            }

            log.info("消息已读: conversationId={}, userId={}", conversationId, userId);

        } catch (Exception e) {
            log.error("处理消息已读异常", e);
        }
    }

    /**
     * 构建状态消息
     * 创建标准格式的状态消息对象
     *
     * @param type   消息类型
     * @param userId 用户ID
     * @param data   消息数据
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
     * 发送消息给指定会话
     * 将消息对象序列化为 JSON 并发送给指定的 WebSocket 会话
     *
     * @param session WebSocket 会话
     * @param message 消息对象
     */
    private void sendMessage(Session session, Object message) {
        try {
            if (session != null && session.isOpen()) {
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
     * @param userId  目标用户ID
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

    // ==================== WebRTC 视频通话信令支持 ====================

    /**
     * 发送通话通知
     * 通知被叫方有来电
     *
     * @param calleeId 接收者ID
     * @param callId   通话ID
     * @param callType 通话类型 VIDEO/VOICE
     * @param callerId 发起者ID
     */
    public static void sendCallNotification(Long calleeId, Long callId, String callType, Long callerId) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "incoming_call");
            notification.put("callId", callId);
            notification.put("callType", callType);
            notification.put("callerId", callerId);
            notification.put("timestamp", System.currentTimeMillis());

            // 获取发起者信息
            if (staticImUserMapper != null) {
                try {
                    com.ruoyi.im.domain.ImUser caller = staticImUserMapper.selectImUserById(callerId);
                    if (caller != null) {
                        notification.put("callerName",
                                caller.getNickname() != null ? caller.getNickname() : caller.getUsername());
                        notification.put("callerAvatar", caller.getAvatar());
                    }
                } catch (Exception e) {
                    log.warn("获取发起者信息失败: callerId={}", callerId, e);
                }
            }

            sendToUser(calleeId, notification);
            log.info("发送通话通知: calleeId={}, callId={}, type={}", calleeId, callId, callType);

        } catch (Exception e) {
            log.error("发送通话通知失败", e);
        }
    }

    /**
     * 发送WebRTC信令消息
     * 转发offer/answer/ice-candidate等信令给通话对端
     *
     * @param callId     通话ID
     * @param fromUserId 发送者用户ID
     * @param signalType 信令类型 offer/answer/ice-candidate/hangup
     * @param signalData 信令数据（JSON字符串）
     */
    public static void sendWebRTCSignal(Long callId, Long fromUserId, String signalType, String signalData) {
        try {
            // 获取通话信息，确定接收者
            // 注意：这里需要调用VideoCallService，但由于是静态方法，我们通过其他方式获取
            // 简化处理：通过Redis获取通话信息，或者直接广播给所有用户让客户端过滤

            Map<String, Object> signalMessage = new HashMap<>();
            signalMessage.put("type", "webrtc_signal");
            signalMessage.put("callId", callId);
            signalMessage.put("signalType", signalType);
            signalMessage.put("fromUserId", fromUserId);
            signalMessage.put("signalData", signalData);
            signalMessage.put("timestamp", System.currentTimeMillis());

            // 广播给所有在线用户（实际应该只发给通话参与者）
            broadcastToAllOnline(new ObjectMapper().writeValueAsString(signalMessage));

            log.debug("转发WebRTC信令: callId={}, type={}, from={}", callId, signalType, fromUserId);

        } catch (Exception e) {
            log.error("转发WebRTC信令失败", e);
        }
    }

    /**
     * 发送通话状态变化通知
     * 通知通话参与者通话状态变化
     *
     * @param callId   通话ID
     * @param callerId 发起者ID
     * @param calleeId 接收者ID
     * @param status   新状态
     */
    public static void sendCallStatusUpdate(Long callId, Long callerId, Long calleeId, String status) {
        try {
            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("type", "call_status");
            statusUpdate.put("callId", callId);
            statusUpdate.put("status", status);
            statusUpdate.put("timestamp", System.currentTimeMillis());

            // 发送给双方
            sendToUser(callerId, statusUpdate);
            sendToUser(calleeId, statusUpdate);

            log.info("发送通话状态通知: callId={}, status={}", callId, status);

        } catch (Exception e) {
            log.error("发送通话状态通知失败", e);
        }
    }

    /**
     * 推送离线消息
     * <p>
     * 用户上线时异步推送离线期间收到的消息
     * 确保消息不丢失，提升系统可靠性
     * </p>
     *
     * @param userId 用户ID
     */
    private void pushOfflineMessages(Long userId) {
        // 使用Spring ApplicationContext获取离线消息服务Bean
        if (applicationContext != null) {
            CompletableFuture.runAsync(() -> {
                try {
                    Object offlineService = applicationContext.getBean("offlineMessageServiceImpl");
                    if (offlineService != null) {
                        // 调用离线消息服务推送消息
                        java.lang.reflect.Method method = offlineService.getClass()
                                .getMethod("pushAndClearOfflineMessages", Long.class);
                        int count = (int) method.invoke(offlineService, userId);
                        log.info("推送离线消息完成: userId={}, count={}", userId, count);
                    }
                } catch (Exception e) {
                    log.error("推送离线消息失败: userId={}", userId, e);
                }
            });
        }
    }

    /**
     * 从Session中提取设备ID
     *
     * @param session WebSocket会话
     * @return 设备ID
     */
    private String extractDeviceIdFromSession(Session session) {
        try {
            String queryString = session.getQueryString();
            if (queryString != null) {
                return extractParamFromQuery(queryString, "deviceId");
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 构建客户端信息
     * 从Session中提取客户端IP、User-Agent等信息
     *
     * @param session WebSocket会话
     * @return 客户端信息JSON字符串
     */
    private String buildClientInfo(Session session) {
        try {
            Map<String, Object> clientInfo = new HashMap<>();
            clientInfo.put("sessionId", session.getId());
            clientInfo.put("remoteAddress", session.getUserProperties().get("javax.websocket.endpoint.remoteAddress"));
            clientInfo.put("userAgent", session.getUserProperties().get("http.userAgent"));
            clientInfo.put("connectTime", System.currentTimeMillis());
            return new ObjectMapper().writeValueAsString(clientInfo);
        } catch (Exception e) {
            log.warn("构建客户端信息失败: sessionId={}", session.getId(), e);
            return "{}";
        }
    }

    /**
     * 从查询字符串中提取指定参数
     *
     * @param queryString 查询字符串
     * @param paramName   参数名
     * @return 参数值，不存在返回null
     */
    private String extractParamFromQuery(String queryString, String paramName) {
        if (queryString == null || queryString.isEmpty() || paramName == null) {
            return null;
        }

        String[] params = queryString.split("&");
        for (String param : params) {
            if (param.startsWith(paramName + "=")) {
                try {
                    return java.net.URLDecoder.decode(param.substring(paramName.length() + 1), "UTF-8");
                } catch (Exception e) {
                    log.warn("解码参数失败: {}", param, e);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 从Session中提取客户端IP地址
     *
     * @param session WebSocket会话
     * @return IP地址
     */
    private String extractClientIpFromSession(Session session) {
        try {
            Object remoteAddress = session.getUserProperties().get("javax.websocket.endpoint.remoteAddress");
            if (remoteAddress != null) {
                return remoteAddress.toString();
            }
            return session.getId();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 处理客户端ACK确认消息
     * 处理消息的送达、接收、已读确认
     *
     * @param userId  用户ID
     * @param payload ACK数据
     * @param session WebSocket会话
     */
    private void processAckMessage(Long userId, Object payload, Session session) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> ackData = mapper.convertValue(payload, new TypeReference<Map<String, Object>>() {
            });

            String ackType = (String) ackData.get("ackType");
            Object messageIdObj = ackData.get("messageId");
            Object clientMsgIdObj = ackData.get("clientMsgId");
            String clientMsgId = clientMsgIdObj != null ? clientMsgIdObj.toString() : null;
            Object deviceIdObj = ackData.get("deviceId");
            String deviceId = deviceIdObj != null ? deviceIdObj.toString() : null;

            if (messageIdObj == null) {
                log.warn("ACK消息缺少messageId字段");
                return;
            }

            Long messageId = Long.valueOf(messageIdObj.toString());

            // 调用ACK服务记录确认
            if (staticMessageAckService != null) {
                switch (ackType) {
                    case "deliver":
                        // 送达确认（服务器收到消息）
                        staticMessageAckService.recordDeliverAck(messageId, clientMsgId, userId);
                        log.debug("记录送达ACK: messageId={}, clientMsgId={}", messageId, clientMsgId);
                        break;
                    case "receive":
                        // 接收确认（客户端收到消息）
                        staticMessageAckService.recordReceiveAck(messageId, userId, deviceId);
                        log.debug("记录接收ACK: messageId={}, userId={}", messageId, userId);
                        break;
                    case "read":
                        // 已读确认
                        staticMessageAckService.recordReadAck(messageId, userId);
                        log.debug("记录已读ACK: messageId={}, userId={}", messageId, userId);
                        break;
                    default:
                        log.warn("未知的ACK类型: ackType={}", ackType);
                }
            }

        } catch (Exception e) {
            log.error("处理ACK消息异常", e);
        }
    }

    /**
     * 处理会话同步事件
     * 处理置顶、免打扰、归档、删除等会话操作，并广播到其他设备
     *
     * @param userId  用户ID
     * @param payload 事件数据
     * @param session WebSocket会话
     */
    private void processConversationEvent(Long userId, Object payload, Session session) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> eventData = mapper.convertValue(payload, new TypeReference<Map<String, Object>>() {
            });

            String eventType = (String) eventData.get("eventType");
            Object conversationIdObj = eventData.get("conversationId");
            String deviceId = extractDeviceIdFromSession(session);

            if (conversationIdObj == null) {
                log.warn("会话事件缺少conversationId字段");
                return;
            }

            Long conversationId = Long.valueOf(conversationIdObj.toString());

            // 调用会话同步服务广播事件
            if (staticConversationSyncService != null) {
                staticConversationSyncService.broadcastConversationEvent(
                        userId, conversationId, eventType, eventData, deviceId);
            }

            log.info("处理会话事件: userId={}, conversationId={}, eventType={}",
                    userId, conversationId, eventType);

        } catch (Exception e) {
            log.error("处理会话事件异常", e);
        }
    }
}
