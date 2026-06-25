package com.ruoyi.im.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.server.ServerEndpointConfig;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * WebSocket Origin 白名单校验器
 * 在握手阶段校验 Origin header，拒绝非白名单来源的连接
 */
public class ImWebSocketConfigurator extends ServerEndpointConfig.Configurator {

    private static final Logger log = LoggerFactory.getLogger(ImWebSocketConfigurator.class);

    /**
     * 允许的 Origin 白名单
     * 生产环境应配置为具体域名
     */
    private static final Set<String> ALLOWED_ORIGINS = new HashSet<>(Arrays.asList(
            "http://localhost",
            "http://localhost:3000",
            "http://localhost:80",
            "http://127.0.0.1",
            "http://127.0.0.1:3000"
    ));

    @Override
    public boolean checkOrigin(String originHeaderValue) {
        if (originHeaderValue == null || originHeaderValue.isEmpty()) {
            log.warn("WebSocket 连接被拒绝: Origin 为空");
            return false;
        }

        // 提取 Origin 的 host 部分进行匹配
        try {
            java.net.URI uri = java.net.URI.create(originHeaderValue);
            String originHost = uri.getScheme() + "://" + uri.getHost()
                    + (uri.getPort() > 0 ? ":" + uri.getPort() : "");

            if (ALLOWED_ORIGINS.contains(originHost)) {
                return true;
            }

            // 开发环境允许所有 localhost 来源
            if ("localhost".equals(uri.getHost()) || "127.0.0.1".equals(uri.getHost())) {
                return true;
            }

            log.warn("WebSocket 连接被拒绝: Origin 不在白名单中, origin={}", originHeaderValue);
            return false;
        } catch (Exception e) {
            log.warn("WebSocket Origin 解析失败: origin={}", originHeaderValue, e);
            return false;
        }
    }
}
