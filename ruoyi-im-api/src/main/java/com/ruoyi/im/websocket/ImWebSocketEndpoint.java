package com.ruoyi.im.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.service.ImMessageService;
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
 * WebSocket 瀹炴椂閫氫俊绔偣
 * 
 * 澶勭悊瀹㈡埛绔殑 WebSocket 杩炴帴銆佹秷鎭彂閫併€佸湪绾跨姸鎬佺鐞嗙瓑
 * 鏀寔鍗曡亰銆佺兢鑱娿€佹秷鎭帹閫佺瓑鍔熻兘
 * 
 * @author ruoyi
 */
@Component
@ServerEndpoint("/ws/im")
public class ImWebSocketEndpoint {

    private static final Logger log = LoggerFactory.getLogger(ImWebSocketEndpoint.class);

    // 瀛樺偍鎵€鏈夊湪绾跨敤鎴风殑 WebSocket 浼氳瘽锛堢敤鎴稩D -> 浼氳瘽锛?
    private static final Map<Long, Session> onlineUsers = new ConcurrentHashMap<>();
    
    // 瀛樺偍浼氳瘽鍒扮敤鎴稩D鐨勬槧灏勶紙浼氳瘽 -> 鐢ㄦ埛ID锛?
    private static final Map<Session, Long> sessionUserMap = new ConcurrentHashMap<>();

    @Autowired
    private ImMessageService imMessageService;

    @Autowired
    private JwtUtils jwtUtils;

    private static ImMessageService staticImMessageService;
    private static JwtUtils staticJwtUtils;
    private static com.ruoyi.im.service.ImUserService staticImUserService;

    @Autowired
    public void setImMessageService(ImMessageService imMessageService) {
        staticImMessageService = imMessageService;
    }

    @Autowired
    public void setJwtUtils(JwtUtils jwtUtils) {
        staticJwtUtils = jwtUtils;
    }
    
    @Autowired
    public void setImUserService(com.ruoyi.im.service.ImUserService imUserService) {
        staticImUserService = imUserService;
    }

    /**
     * 瀹㈡埛绔繛鎺ユ椂璋冪敤
     */
    @OnOpen
    public void onOpen(Session session, @javax.websocket.server.PathParam("token") String token) {
        try {
            log.info("WebSocket 杩炴帴璇锋眰: sessionId={}", session.getId());

            // 浠庢煡璇㈠弬鏁颁腑鑾峰彇 token
            String queryString = session.getQueryString();
            String tokenValue = extractTokenFromQuery(queryString);

            if (tokenValue == null || tokenValue.isEmpty()) {
                log.warn("WebSocket 杩炴帴澶辫触: 缂哄皯 token");
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "缂哄皯璁よ瘉 token"));
                return;
            }

            // 楠岃瘉 token 骞惰幏鍙栫敤鎴峰悕
            String username = staticJwtUtils.getUsernameFromToken(tokenValue);
            if (username == null) {
                log.warn("WebSocket 杩炴帴澶辫触: token 鏃犳晥");
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "token 鏃犳晥"));
                return;
            }

            // 鑾峰彇鐪熷疄鐢ㄦ埛ID
            com.ruoyi.im.domain.ImUser user = staticImUserService.selectImUserByUsername(username);
            if (user == null) {
                log.warn("WebSocket 杩炴帴澶辫触: 鐢ㄦ埛涓嶅瓨鍦?username={}", username);
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "鐢ㄦ埛涓嶅瓨鍦?));
                return;
            }
            Long userId = user.getId();

            // 淇濆瓨鐢ㄦ埛浼氳瘽
            onlineUsers.put(userId, session);
            sessionUserMap.put(session, userId);

            log.info("鐢ㄦ埛涓婄嚎: userId={}, sessionId={}", userId, session.getId());

            // 骞挎挱鐢ㄦ埛涓婄嚎娑堟伅
            broadcastOnlineStatus(userId, true);

            // 鍙戦€佽繛鎺ユ垚鍔熸秷鎭粰瀹㈡埛绔?
            sendMessage(session, buildStatusMessage("connected", userId, true));

        } catch (Exception e) {
            log.error("WebSocket 杩炴帴澶勭悊寮傚父", e);
            try {
                session.close();
            } catch (IOException ex) {
                log.error("鍏抽棴 WebSocket 浼氳瘽寮傚父", ex);
            }
        }
    }

    /**
     * 鎺ユ敹瀹㈡埛绔秷鎭?
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            Long userId = sessionUserMap.get(session);
            if (userId == null) {
                log.warn("鏀跺埌娑堟伅浣嗙敤鎴锋湭璁よ瘉: sessionId={}", session.getId());
                return;
            }

            log.debug("鏀跺埌娑堟伅: userId={}, message={}", userId, message);

            // 瑙ｆ瀽娑堟伅
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> messageMap = mapper.readValue(message, Map.class);

            String type = (String) messageMap.get("type");
            Object payload = messageMap.get("payload");

            switch (type) {
                case "message":
                    // 澶勭悊鑱婂ぉ娑堟伅
                    handleChatMessage(userId, payload);
                    break;
                case "typing":
                    // 澶勭悊姝ｅ湪杈撳叆鐘舵€?
                    handleTypingStatus(userId, payload);
                    break;
                case "read":
                    // 澶勭悊娑堟伅宸茶
                    handleReadReceipt(userId, payload);
                    break;
                case "ping":
                    // 澶勭悊蹇冭烦
                    sendMessage(session, buildStatusMessage("pong", userId, true));
                    break;
                default:
                    log.warn("鏈煡娑堟伅绫诲瀷: type={}", type);
            }

        } catch (Exception e) {
            log.error("澶勭悊 WebSocket 娑堟伅寮傚父", e);
        }
    }

    /**
     * 瀹㈡埛绔柇寮€杩炴帴鏃惰皟鐢?
     */
    @OnClose
    public void onClose(Session session) {
        try {
            Long userId = sessionUserMap.remove(session);
            if (userId != null) {
                onlineUsers.remove(userId);
                log.info("鐢ㄦ埛绂荤嚎: userId={}, sessionId={}", userId, session.getId());

                // 骞挎挱鐢ㄦ埛绂荤嚎娑堟伅
                broadcastOnlineStatus(userId, false);
            }
        } catch (Exception e) {
            log.error("澶勭悊 WebSocket 鍏抽棴寮傚父", e);
        }
    }

    /**
     * 杩炴帴寮傚父鏃惰皟鐢?
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 寮傚父: sessionId={}", session.getId(), error);
        try {
            Long userId = sessionUserMap.remove(session);
            if (userId != null) {
                onlineUsers.remove(userId);
            }
            session.close();
        } catch (IOException e) {
            log.error("鍏抽棴寮傚父杩炴帴澶辫触", e);
        }
    }

    /**
     * 澶勭悊鑱婂ぉ娑堟伅
     */
    private void handleChatMessage(Long userId, Object payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> messageData = mapper.convertValue(payload, Map.class);

            Long conversationId = Long.valueOf(messageData.get("conversationId").toString());
            String messageType = (String) messageData.get("type");
            String content = (String) messageData.get("content");

            // 淇濆瓨娑堟伅鍒版暟鎹簱
            Long messageId = staticImMessageService.sendMessage(conversationId, userId, messageType, content);

            if (messageId != null) {
                // 鑾峰彇瀹屾暣娑堟伅瀵硅薄
                ImMessage message = staticImMessageService.selectImMessageById(messageId);

                // 骞挎挱娑堟伅缁欎細璇濅腑鐨勬墍鏈夌敤鎴?
                broadcastMessageToConversation(conversationId, message);

                log.info("娑堟伅宸插彂閫? messageId={}, conversationId={}", messageId, conversationId);
            }

        } catch (Exception e) {
            log.error("澶勭悊鑱婂ぉ娑堟伅寮傚父", e);
        }
    }

    /**
     * 澶勭悊姝ｅ湪杈撳叆鐘舵€?
     */
    private void handleTypingStatus(Long userId, Object payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> typingData = mapper.convertValue(payload, Map.class);

            Long conversationId = Long.valueOf(typingData.get("conversationId").toString());
            boolean isTyping = (boolean) typingData.get("isTyping");

            // 骞挎挱姝ｅ湪杈撳叆鐘舵€?
            broadcastTypingStatus(conversationId, userId, isTyping);

        } catch (Exception e) {
            log.error("澶勭悊姝ｅ湪杈撳叆鐘舵€佸紓甯?, e);
        }
    }

    /**
     * 澶勭悊娑堟伅宸茶
     */
    private void handleReadReceipt(Long userId, Object payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> readData = mapper.convertValue(payload, Map.class);

            Long messageId = Long.valueOf(readData.get("messageId").toString());

            // 鏇存柊娑堟伅鐘舵€佷负宸茶
            staticImMessageService.updateMessageStatus(messageId, "READ");

            // 骞挎挱宸茶鍥炴墽
            broadcastReadReceipt(messageId, userId);

            log.info("娑堟伅宸茶: messageId={}, userId={}", messageId, userId);

        } catch (Exception e) {
            log.error("澶勭悊娑堟伅宸茶寮傚父", e);
        }
    }

    /**
     * 骞挎挱娑堟伅缁欎細璇濅腑鐨勬墍鏈夌敤鎴?
     */
    private void broadcastMessageToConversation(Long conversationId, ImMessage message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("type", "message");
            messageMap.put("payload", message);

            String messageJson = mapper.writeValueAsString(messageMap);

            // 杩欓噷搴旇鏍规嵁 conversationId 鑾峰彇浼氳瘽涓殑鎵€鏈夌敤鎴?
            // 绠€鍖栬捣瑙侊紝骞挎挱缁欐墍鏈夊湪绾跨敤鎴?
            // 浣跨敤骞跺彂瀹夊叏鐨勯亶鍘嗘柟寮?
            List<Session> sessions = new ArrayList<>(onlineUsers.values());
            for (Session session : sessions) {
                if (session.isOpen()) {
                    sendMessage(session, messageJson);
                }
            }

        } catch (Exception e) {
            log.error("骞挎挱娑堟伅寮傚父", e);
        }
    }

    /**
     * 骞挎挱姝ｅ湪杈撳叆鐘舵€?
     */
    private void broadcastTypingStatus(Long conversationId, Long userId, boolean isTyping) {
        try {
            Map<String, Object> statusMap = new HashMap<>();
            statusMap.put("type", "typing");
            statusMap.put("conversationId", conversationId);
            statusMap.put("userId", userId);
            statusMap.put("isTyping", isTyping);

            ObjectMapper mapper = new ObjectMapper();
            String messageJson = mapper.writeValueAsString(statusMap);

            // 骞挎挱缁欐墍鏈夊湪绾跨敤鎴?
            // 浣跨敤骞跺彂瀹夊叏鐨勯亶鍘嗘柟寮?
            List<Session> sessions = new ArrayList<>(onlineUsers.values());
            for (Session session : sessions) {
                if (session.isOpen()) {
                    sendMessage(session, messageJson);
                }
            }

        } catch (Exception e) {
            log.error("骞挎挱姝ｅ湪杈撳叆鐘舵€佸紓甯?, e);
        }
    }

    /**
     * 骞挎挱宸茶鍥炴墽
     */
    private void broadcastReadReceipt(Long messageId, Long userId) {
        try {
            Map<String, Object> receiptMap = new HashMap<>();
            receiptMap.put("type", "read_receipt");
            receiptMap.put("messageId", messageId);
            receiptMap.put("userId", userId);
            receiptMap.put("timestamp", System.currentTimeMillis());

            ObjectMapper mapper = new ObjectMapper();
            String messageJson = mapper.writeValueAsString(receiptMap);

            // 骞挎挱缁欐墍鏈夊湪绾跨敤鎴?
            // 浣跨敤骞跺彂瀹夊叏鐨勯亶鍘嗘柟寮?
            List<Session> sessions = new ArrayList<>(onlineUsers.values());
            for (Session session : sessions) {
                if (session.isOpen()) {
                    sendMessage(session, messageJson);
                }
            }

        } catch (Exception e) {
            log.error("骞挎挱宸茶鍥炴墽寮傚父", e);
        }
    }

    /**
     * 骞挎挱鐢ㄦ埛鍦ㄧ嚎鐘舵€?
     */
    private void broadcastOnlineStatus(Long userId, boolean online) {
        try {
            Map<String, Object> statusMap = buildStatusMessage("online", userId, online);

            ObjectMapper mapper = new ObjectMapper();
            String messageJson = mapper.writeValueAsString(statusMap);

            // 骞挎挱缁欐墍鏈夊湪绾跨敤鎴?
            // 浣跨敤骞跺彂瀹夊叏鐨勯亶鍘嗘柟寮?
            List<Session> sessions = new ArrayList<>(onlineUsers.values());
            for (Session session : sessions) {
                if (session.isOpen()) {
                    sendMessage(session, messageJson);
                }
            }

        } catch (Exception e) {
            log.error("骞挎挱鍦ㄧ嚎鐘舵€佸紓甯?, e);
        }
    }

    /**
     * 鍙戦€佹秷鎭粰鎸囧畾浼氳瘽
     */
    private void sendMessage(Session session, Object message) {
        try {
            if (session.isOpen()) {
                ObjectMapper mapper = new ObjectMapper();
                String messageJson = mapper.writeValueAsString(message);
                session.getBasicRemote().sendText(messageJson);
            }
        } catch (IOException e) {
            log.error("鍙戦€佹秷鎭紓甯?, e);
        }
    }

    /**
     * 鏋勫缓鐘舵€佹秷鎭?
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
     * 浠庢煡璇㈠瓧绗︿覆涓彁鍙?token
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
     * 鑾峰彇鍦ㄧ嚎鐢ㄦ埛鏁?
     */
    public static int getOnlineUserCount() {
        return onlineUsers.size();
    }

    /**
     * 妫€鏌ョ敤鎴锋槸鍚﹀湪绾?
     */
    public static boolean isUserOnline(Long userId) {
        return onlineUsers.containsKey(userId);
    }

    /**
     * 鑾峰彇鎵€鏈夊湪绾跨敤鎴稩D
     */
    public static Set<Long> getOnlineUserIds() {
        return new HashSet<>(onlineUsers.keySet());
    }
}
