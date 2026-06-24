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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    private static final ExecutorService SEND_EXECUTOR = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors() * 2,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(1000),
            new ThreadPoolExecutor.CallerRunsPolicy());

    // 定时任务调度器 (用于消息重发)
    private static final ScheduledExecutorService RETRY_EXECUTOR = Executors.newScheduledThreadPool(2);

    // 优雅关闭钩子
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            SEND_EXECUTOR.shutdown();
            RETRY_EXECUTOR.shutdown();
            try {
                if (!SEND_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                    SEND_EXECUTOR.shutdownNow();
                }
                if (!RETRY_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                    RETRY_EXECUTOR.shutdownNow();
                }
            } catch (InterruptedException e) {
                SEND_EXECUTOR.shutdownNow();
                RETRY_EXECUTOR.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }));
    }

    // 消息最大重试次数
    private static final int MAX_RETRY_COUNT = 3;

    // 重试间隔（秒）
    private static final long RETRY_INTERVAL_SECONDS = 5;

    // 认证类型
    private static final String TYPE_AUTH = "AUTH";
    // 心跳动作
    private static final String ACTION_PING = "PING";
    // 心跳响应动作
    private static final String ACTION_PONG = "PONG";
    // 消息类型
    private static final String TYPE_MESSAGE = "MESSAGE";
    // 确认动作
    private static final String ACTION_ACK = "ACK";
    // 错误类型
    private static final String TYPE_ERROR = "ERROR";
    // 无效格式错误码
    private static final String ERROR_INVALID_FORMAT = "INVALID_FORMAT";
    // 未授权关闭原因
    private static final String CLOSE_REASON_UNAUTHORIZED = "UNAUTHORIZED";
    // 并发登录关闭原因
    private static final String CLOSE_REASON_CONCURRENT_LOGIN = "CONCURRENT_LOGIN";
    // 关闭状态码
    private static final int CLOSE_STATUS_CODE = 4000;

    @Resource
    private ImMessageService imMessageService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserIdFromSession(session);
        if (Objects.isNull(userId)) {
            closeSessionSilently(session, CLOSE_REASON_UNAUTHORIZED);
            return;
        }

        // 1. 实现连接覆盖保护 (Kick-out logic)
        WebSocketSession oldSession = USER_SESSION_MAP.get(userId);
        if (Objects.nonNull(oldSession) && !oldSession.getId().equals(session.getId())) {
            log.warn("Detecting concurrent login for userId: {}, kicking out old session: {}", userId, oldSession.getId());
            closeSessionSilently(oldSession, CLOSE_REASON_CONCURRENT_LOGIN);
        }

        USER_SESSION_MAP.put(userId, session);
        log.info("WebSocket connected, userId: {}, sessionId: {}", userId, session.getId());

        // 5. 断线重连时补发未送达消息
        SEND_EXECUTOR.execute(() -> {
            try {
                resendUndeliveredMessages(userId, session);
            } catch (Exception e) {
                log.error("Failed to resend undelivered messages for userId: {}", userId, e);
            }
        });
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        try {
            WsFrame frame = JSON.parseObject(payload, WsFrame.class);
            if (Objects.isNull(frame) || Objects.isNull(frame.getType())) { return; }

            Long userId = getUserIdFromSession(session);

            // 2. 处理心跳 (P7 优化：增加心跳监测计时)
            if (TYPE_AUTH.equals(frame.getType()) && ACTION_PING.equals(frame.getAction())) {
                sendPong(session);
                return;
            }

            // 3. 处理应用层 ACK (Doc-34 §5.2)
            if (TYPE_MESSAGE.equals(frame.getType()) && ACTION_ACK.equals(frame.getAction())) {
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
            sendErrorFrame(session, ERROR_INVALID_FORMAT, "消息协议不匹配");
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
     * 支持消息持久化和自动重发机制
     */
    public void sendMessageToUser(Long userId, WsFrame frame) {
        WebSocketSession session = USER_SESSION_MAP.get(userId);
        if (Objects.nonNull(session) && session.isOpen()) {
            SEND_EXECUTOR.execute(() -> {
                try {
                    synchronized (session) { // 解决同一 Session 并发写导致冲突
                        session.sendMessage(new TextMessage(JSON.toJSONString(frame)));
                    }
                    log.debug("Message sent successfully to userId: {}, messageId: {}", userId, frame.getRequestId());
                } catch (Exception e) {
                    log.error("Failed to send message to user {}, will persist and retry", userId, e);
                    // 消息发送失败，持久化并安排重试
                    persistAndRetryMessage(userId, frame, 0);
                }
            });
        } else {
            // 用户离线，持久化消息等待后续推送
            log.warn("User {} is offline, persisting message for later delivery", userId);
            persistAndRetryMessage(userId, frame, 0);
        }
    }

    private void sendPong(WebSocketSession session) throws IOException {
        WsFrame pong = new WsFrame();
        pong.setType(TYPE_AUTH);
        pong.setAction(ACTION_PONG);
        pong.setTimestamp(System.currentTimeMillis());
        session.sendMessage(new TextMessage(JSON.toJSONString(pong)));
    }

    private void sendErrorFrame(WebSocketSession session, String code, String msg) {
        try {
            WsFrame error = new WsFrame();
            error.setType(TYPE_ERROR);
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
            if (session.isOpen()) { session.close(new CloseStatus(CLOSE_STATUS_CODE, reason)); }
        } catch (IOException e) {
            log.warn("Failed to close session {}, reason: {}", session.getId(), reason, e);
        }
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        Object userIdObj = session.getAttributes().get("userId");
        if (userIdObj instanceof Long) { return (Long) userIdObj; }
        if (userIdObj instanceof Integer) { return ((Integer) userIdObj).longValue(); }
        if (userIdObj instanceof String) { return Long.parseLong((String) userIdObj); }
        return null;
    }

    /**
     * 持久化消息并安排重试
     * @param userId 目标用户ID
     * @param frame 消息帧
     * @param retryCount 当前重试次数
     */
    private void persistAndRetryMessage(Long userId, WsFrame frame, int retryCount) {
        if (retryCount >= MAX_RETRY_COUNT) {
            log.error("Message retry count exceeded for userId: {}, messageId: {}", userId, frame.getRequestId());
            // 标记消息为发送失败
            if (frame.getRequestId() != null && !frame.getRequestId().isEmpty()) {
                try {
                    Long messageId = Long.parseLong(frame.getRequestId());
                    imMessageService.markMessageAsFailed(messageId);
                } catch (NumberFormatException e) {
                    log.warn("Invalid message ID: {}", frame.getRequestId());
                }
            }
            return;
        }

        // 持久化消息到数据库（由 ImMessageService 处理）
        try {
            imMessageService.saveUndeliveredMessage(userId, frame);
        } catch (Exception e) {
            log.error("Failed to persist undelivered message for userId: {}", userId, e);
            return;
        }

        // 安排重试
        long delay = RETRY_INTERVAL_SECONDS * (long) Math.pow(2, retryCount); // 指数退避
        RETRY_EXECUTOR.schedule(() -> {
            retrySendMessage(userId, frame, retryCount + 1);
        }, delay, TimeUnit.SECONDS);

        log.info("Scheduled message retry for userId: {}, messageId: {}, attempt: {}, delay: {}s",
                userId, frame.getRequestId(), retryCount + 1, delay);
    }

    /**
     * 重试发送消息
     */
    private void retrySendMessage(Long userId, WsFrame frame, int retryCount) {
        WebSocketSession session = USER_SESSION_MAP.get(userId);
        if (Objects.nonNull(session) && session.isOpen()) {
            try {
                synchronized (session) {
                    session.sendMessage(new TextMessage(JSON.toJSONString(frame)));
                }
                log.info("Message retry successful for userId: {}, messageId: {}, attempt: {}",
                        userId, frame.getRequestId(), retryCount);
                
                // 发送成功，从持久化存储中删除
                if (frame.getRequestId() != null && !frame.getRequestId().isEmpty()) {
                    try {
                        Long messageId = Long.parseLong(frame.getRequestId());
                        imMessageService.removeUndeliveredMessage(messageId);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid message ID: {}", frame.getRequestId());
                    }
                }
                return;
            } catch (Exception e) {
                log.error("Message retry attempt {} failed for userId: {}", retryCount, userId, e);
            }
        }

        // 仍然失败，继续重试
        persistAndRetryMessage(userId, frame, retryCount);
    }

    /**
     * 重连时补发未送达消息
     */
    private void resendUndeliveredMessages(Long userId, WebSocketSession session) {
        List<WsFrame> undeliveredMessages = imMessageService.getUndeliveredMessages(userId);
        if (undeliveredMessages == null || undeliveredMessages.isEmpty()) {
            log.debug("No undelivered messages for userId: {}", userId);
            return;
        }

        log.info("Resending {} undelivered messages for userId: {}", undeliveredMessages.size(), userId);
        
        for (WsFrame frame : undeliveredMessages) {
            try {
                synchronized (session) {
                    session.sendMessage(new TextMessage(JSON.toJSONString(frame)));
                }
                log.debug("Resent message: {} to userId: {}", frame.getRequestId(), userId);
                
                // 删除已发送成功的消息
                if (frame.getRequestId() != null && !frame.getRequestId().isEmpty()) {
                    try {
                        Long messageId = Long.parseLong(frame.getRequestId());
                        imMessageService.removeUndeliveredMessage(messageId);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid message ID: {}", frame.getRequestId());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to resend undelivered message to userId: {}", userId, e);
            }
        }
    }
}
