package com.ruoyi.im.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置类
 * 
 * 用于启用WebSocket支持，注册@ServerEndpoint注解的WebSocket端点
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Configuration
@EnableWebSocket
public class ImWebSocketConfig {

    /**
     * 注册ServerEndpointExporter bean
     * 这个bean会自动注册使用@ServerEndpoint注解的WebSocket端点
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
