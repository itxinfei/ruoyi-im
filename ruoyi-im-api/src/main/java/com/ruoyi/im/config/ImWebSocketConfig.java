package com.ruoyi.im.config;

import com.ruoyi.im.websocket.ImWebSocketHandler;
import com.ruoyi.im.websocket.WebSocketHandshakeInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket 核心配置类 (统一重构版)
 * 1. 支持标准 JSR-356 (@ServerEndpoint)
 * 2. 支持 Spring WebSocketConfigurer 方案 (Doc-34 协议端点)
 */
@Configuration
@EnableWebSocket
@ConditionalOnWebApplication
public class ImWebSocketConfig implements WebSocketConfigurer {

    private final ImWebSocketHandler webSocketHandler;
    private final WebSocketHandshakeInterceptor webSocketHandshakeInterceptor;

    public ImWebSocketConfig(ImWebSocketHandler webSocketHandler, 
                              WebSocketHandshakeInterceptor webSocketHandshakeInterceptor) {
        this.webSocketHandler = webSocketHandler;
        this.webSocketHandshakeInterceptor = webSocketHandshakeInterceptor;
    }

    /**
     * 注册 Spring WebSocket 处理器 (对标 Doc-34 协议)
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws/im")
                .addInterceptors(webSocketHandshakeInterceptor)
                .setAllowedOrigins("*");
    }

    /**
     * 注册 ServerEndpointExporter (兼容原有 @ServerEndpoint 注解)
     */
    @Bean
    @ConditionalOnWebApplication
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
