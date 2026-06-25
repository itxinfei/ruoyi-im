package com.ruoyi.im.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket 核心配置类 (统一重构版)
 * 仅支持标准 JSR-356 (@ServerEndpoint) 实现
 * 已移除 Spring WebSocketConfigurer 方案以消除双处理器冲突
 */
@Configuration
@ConditionalOnWebApplication
public class ImWebSocketConfig {

    /**
     * 注册 ServerEndpointExporter (支持 @ServerEndpoint 注解)
     */
    @Bean
    @ConditionalOnWebApplication
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
