package com.ruoyi.im.websocket;

import com.ruoyi.im.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

import java.net.URI;
import java.util.List;

@Component
public class WebSocketHandshakeInterceptor extends WebSocketHandlerDecorator {

    private static final Logger log = LoggerFactory.getLogger(WebSocketHandshakeInterceptor.class);

    @Autowired
    private JwtUtils jwtUtils;

    private List<String> ipWhitelist;

    public WebSocketHandshakeInterceptor(TextWebSocketHandler delegate) {
        super(delegate);
    }

    @Override
    public boolean beforeHandshake(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            log.warn("WebSocket connection failed: URI is null");
            return false;
        }

        String queryString = uri.getQuery();
        if (queryString == null || queryString.isEmpty()) {
            log.warn("WebSocket connection failed: Missing auth parameters");
            return false;
        }

        String token = extractToken(queryString);
        if (token == null || token.isEmpty()) {
            log.warn("WebSocket connection failed: Missing token");
            return false;
        }

        if (!jwtUtils.validateToken(token)) {
            log.warn("WebSocket connection failed: Invalid or expired token");
            return false;
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        if (userId == null) {
            log.warn("WebSocket connection failed: Cannot parse userId from token");
            return false;
        }

        if (ipWhitelist != null && !ipWhitelist.isEmpty()) {
            String clientIp = extractClientIp(session);
            if (!isIpAllowed(clientIp)) {
                log.warn("WebSocket connection failed: IP not in whitelist, clientIp={}, userId={}", clientIp, userId);
                return false;
            }
        }

        session.getAttributes().put("userId", userId);
        session.getAttributes().put("token", token);

        log.info("WebSocket connection authenticated: userId={}, sessionId={}", userId, session.getId());
        return true;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session, TextMessage message) {
        super.afterConnectionEstablished(session, message);
        Long userId = (Long) session.getAttributes().get("userId");
        log.info("WebSocket connection established: userId={}, sessionId={}", userId, session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus, TextMessage message) {
        super.afterConnectionClosed(session, closeStatus, message);
        Long userId = (Long) session.getAttributes().get("userId");
        log.info("WebSocket connection closed: userId={}, statusCode={}, sessionId={}",
                userId, closeStatus.getCode(), session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        super.handleTransportError(session, exception);
        Long userId = (Long) session.getAttributes().get("userId");
        log.error("WebSocket transport error: userId={}, sessionId={}",
                userId, session != null ? session.getId() : "null", exception);
    }

    private String extractToken(String queryString) {
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

    private String extractClientIp(WebSocketSession session) {
        String ip = null;

        Object ipObj = session.getAttributes().get("javax.websocket.endpoint.remoteAddress");
        if (ipObj != null) {
            ip = ipObj.toString();
            if (ip != null && ip.contains("/")) {
                int lastSlash = ip.lastIndexOf("/");
                if (lastSlash > 0) {
                    ip = ip.substring(0, lastSlash);
                }
            }
        }

        return ip;
    }

    private boolean isIpAllowed(String clientIp) {
        if (clientIp == null || clientIp.isEmpty()) {
            return false;
        }

        if (ipWhitelist == null || ipWhitelist.isEmpty()) {
            return true;
        }

        for (String whitelistEntry : ipWhitelist) {
            if (whitelistEntry.contains("*")) {
                return true;
            }
            if (whitelistEntry.equals(clientIp)) {
                return true;
            }
        }

        return false;
    }

    public void setIpWhitelist(List<String> ipWhitelist) {
        this.ipWhitelist = ipWhitelist;
        log.info("IP whitelist updated: {}", ipWhitelist);
    }

    public List<String> getIpWhitelist() {
        return ipWhitelist;
    }
}
