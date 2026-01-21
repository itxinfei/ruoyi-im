package com.ruoyi.im.websocket;

import com.ruoyi.im.util.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketHandshakeInterceptor.class);

    @Autowired
    private JwtUtils jwtUtils;

    private List<String> ipWhitelist;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                  WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        URI uri = request.getURI();
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
            String clientIp = extractClientIp(request);
            if (!isIpAllowed(clientIp)) {
                log.warn("WebSocket connection failed: IP not in whitelist");
                return false;
            }
        }

        attributes.put("userId", userId);
        attributes.put("token", token);

        log.info("WebSocket connection authenticated: userId={}", userId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        log.info("WebSocket handshake completed");
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

    private String extractClientIp(ServerHttpRequest request) {
        if (request.getRemoteAddress() == null) {
            return null;
        }
        String ip = request.getRemoteAddress().toString();
        if (ip != null && ip.contains("/")) {
            int lastSlash = ip.lastIndexOf("/");
            if (lastSlash >= 0 && lastSlash < ip.length() - 1) {
                ip = ip.substring(lastSlash + 1);
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
        log.info("IP whitelist updated");
    }

    public List<String> getIpWhitelist() {
        return ipWhitelist;
    }
}
