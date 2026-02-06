package com.ruoyi.im.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 测试环境配置类
 * <p>
 * 排除在MockMvc测试环境中无法正常工作的WebSocket相关配置
 *
 * @author ruoyi
 */
@TestConfiguration
@Configuration
@Import(WebSocketServletAutoConfiguration.class)
public class TestConfig {
    // 这个配置类用于测试环境
    // ServerEndpointExporter 在 MockMvc 测试环境中无法正常工作
    // 因为它需要真实的 Servlet 容器
}
