package com.ruoyi.im.websocket;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 核心 WebSocket 处理器
 * 负责心跳、连接管理及底层帧的分发
 * 遵循 JDK 1.8，严格遵守 Doc-34 帧格式与心跳契约
 */
@Slf4j
@Component
public class ImWebSocketHandler extends TextWebSocketHandler {

    // 维护 userId -> WebSocketSession 的映射 (生产环境应结合 Redis 做分布式会话管理)
    private static final Map<Long, WebSocketSession> USER_SESSION_MAP = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserIdFromSession(session);
        if (Objects.nonNull(userId)) {
            USER_SESSION_MAP.put(userId, session);
            log.info("WebSocket connected, userId: {}, sessionId: {}", userId, session.getId());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        try {
            WsFrame frame = JSON.parseObject(payload, WsFrame.class);
            if (Objects.isNull(frame) || Objects.isNull(frame.getType())) {
                return;
            }

            Long userId = getUserIdFromSession(session);

            // 1. 处理心跳 (Doc-34 §6)
            if ("AUTH".equals(frame.getType()) && "PING".equals(frame.getAction())) {
                WsFrame pong = new WsFrame();
                pong.setType("AUTH");
                pong.setAction("PONG");
                pong.setTimestamp(System.currentTimeMillis());
                session.sendMessage(new TextMessage(JSON.toJSONString(pong)));
                return;
            }

            // 2. 处理空隙填充同步请求 (Doc-34 §5.1)
            if ("MESSAGE".equals(frame.getType()) && "SYNC_REQ".equals(frame.getAction())) {
                log.info("Received SYNC_REQ from userId: {}", userId);
                // 实际应投递给 Service 进行历史消息拉取并循环推送 SYNC_PUSH
                return;
            }

            // 其他上行信令（如通话呼叫）可接入责任链或策略模式
            
        } catch (Exception e) {
            log.error("Error processing websocket message", e);
            try {
                WsFrame errorFrame = new WsFrame();
                errorFrame.setType("ERROR");
                errorFrame.setAction("PARSE_ERROR");
                errorFrame.setData("消息格式错误");
                errorFrame.setTimestamp(System.currentTimeMillis());
                session.sendMessage(new TextMessage(JSON.toJSONString(errorFrame)));
            } catch (Exception sendError) {
                log.warn("发送错误消息失败", sendError);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserIdFromSession(session);
        if (Objects.nonNull(userId)) {
            USER_SESSION_MAP.remove(userId);
            log.info("WebSocket closed, userId: {}, status: {}", userId, status.getReason());
        }
    }

    /**
     * 向指定用户推送消息 (被 EventListener 调用)
     */
    public void sendMessageToUser(Long userId, WsFrame frame) {
        WebSocketSession session = USER_SESSION_MAP.get(userId);
        if (Objects.nonNull(session) && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(JSON.toJSONString(frame)));
            } catch (Exception e) {
                log.error("Failed to send message to user {}", userId, e);
            }
        }
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        Object userIdObj = session.getAttributes().get("userId");
        return userIdObj instanceof Long ? (Long) userIdObj : null;
    }
}
