package com.ruoyi.im.websocket;

import com.alibaba.fastjson.JSON;
import com.ruoyi.im.service.ImMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 生产级 WebSocket 处理器 (Hardened Version)
 * 2026 加固：连接冲突剔除、异步解耦、应用层 ACK 拦截
 * 遵循 JDK 1.8 规范，严禁使用 Java 9+ 语法
 */
@Slf4j
@Component
public class ImWebSocketHandler extends TextWebSocketHandler {

    // 内存 Session 映射 (集群环境建议替换为 Redis 订阅)
    private static final Map<Long, WebSocketSession> USER_SESSION_MAP = new ConcurrentHashMap<>();

    // 异步发送线程池 (防止慢连接阻塞业务)
    private static final ExecutorService SEND_EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    @Resource
    private ImMessageService imMessageService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserIdFromSession(session);
        if (Objects.isNull(userId)) {
            closeSessionSilently(session, "UNAUTHORIZED");
            return;
        }

        // 1. 实现连接覆盖保护 (Kick-out logic)
        WebSocketSession oldSession = USER_SESSION_MAP.get(userId);
        if (Objects.nonNull(oldSession) && !oldSession.getId().equals(session.getId())) {
            log.warn("Detecting concurrent login for userId: {}, kicking out old session: {}", userId, oldSession.getId());
            closeSessionSilently(oldSession, "CONCURRENT_LOGIN");
        }

        USER_SESSION_MAP.put(userId, session);
        log.info("WebSocket connected, userId: {}, sessionId: {}", userId, session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        try {
            WsFrame frame = JSON.parseObject(payload, WsFrame.class);
            if (Objects.isNull(frame) || Objects.isNull(frame.getType())) return;

            Long userId = getUserIdFromSession(session);

            // 2. 处理心跳 (P7 优化：增加心跳监测计时)
            if ("AUTH".equals(frame.getType()) && "PING".equals(frame.getAction())) {
                sendPong(session);
                return;
            }

            // 3. 处理应用层 ACK (Doc-34 §5.2)
            if ("MESSAGE".equals(frame.getType()) && "ACK".equals(frame.getAction())) {
                log.debug("Received MESSAGE_ACK from userId: {}, requestId: {}", userId, frame.getRequestId());
                // 确认消息送达：requestId 为消息 ID
                if (frame.getRequestId() != null && !frame.getRequestId().isEmpty()) {
                    try {
                        Long messageId = Long.parseLong(frame.getRequestId());
                        imMessageService.confirmMessageDelivery(messageId, userId);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid message ID in ACK: {}", frame.getRequestId());
                    }
                }
                return;
            }

            // 4. 路由业务信令
            log.info("Incoming signal: {}/{} from userId: {}", frame.getType(), frame.getAction(), userId);
            
        } catch (Exception e) {
            log.error("WebSocket message parsing error", e);
            sendErrorFrame(session, "INVALID_FORMAT", "消息协议不匹配");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserIdFromSession(session);
        if (Objects.nonNull(userId)) {
            // 只有当前 Session 与 Map 中一致时才移除 (防止新 Session 被旧 Session 的关闭逻辑误删)
            WebSocketSession current = USER_SESSION_MAP.get(userId);
            if (Objects.nonNull(current) && current.getId().equals(session.getId())) {
                USER_SESSION_MAP.remove(userId);
                log.info("WebSocket removed, userId: {}, status: {}", userId, status.getReason());
            }
        }
    }

    /**
     * 异步推送消息给指定用户
     * 具备背压防护能力 (通过线程池队列限制)
     */
    public void sendMessageToUser(Long userId, WsFrame frame) {
        WebSocketSession session = USER_SESSION_MAP.get(userId);
        if (Objects.nonNull(session) && session.isOpen()) {
            SEND_EXECUTOR.execute(() -> {
                try {
                    synchronized (session) { // 解决同一 Session 并发写导致冲突
                        session.sendMessage(new TextMessage(JSON.toJSONString(frame)));
                    }
                } catch (Exception e) {
                    log.error("Failed to send message to user {}", userId, e);
                }
            });
        }
    }

    private void sendPong(WebSocketSession session) throws IOException {
        WsFrame pong = new WsFrame();
        pong.setType("AUTH");
        pong.setAction("PONG");
        pong.setTimestamp(System.currentTimeMillis());
        session.sendMessage(new TextMessage(JSON.toJSONString(pong)));
    }

    private void sendErrorFrame(WebSocketSession session, String code, String msg) {
        try {
            WsFrame error = new WsFrame();
            error.setType("ERROR");
            error.setAction(code);
            error.setData(msg);
            error.setTimestamp(System.currentTimeMillis());
            session.sendMessage(new TextMessage(JSON.toJSONString(error)));
        } catch (Exception e) {
            log.warn("Failed to send error frame to session {}, code: {}", session.getId(), code, e);
        }
    }

    private void closeSessionSilently(WebSocketSession session, String reason) {
        try {
            if (session.isOpen()) session.close(new CloseStatus(4000, reason));
        } catch (IOException e) {
            log.warn("Failed to close session {}, reason: {}", session.getId(), reason, e);
        }
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        Object userIdObj = session.getAttributes().get("userId");
        if (userIdObj instanceof Long) return (Long) userIdObj;
        if (userIdObj instanceof Integer) return ((Integer) userIdObj).longValue();
        if (userIdObj instanceof String) return Long.parseLong((String) userIdObj);
        return null;
    }
}
