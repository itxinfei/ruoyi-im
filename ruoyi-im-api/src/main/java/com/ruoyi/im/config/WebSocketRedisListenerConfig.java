package com.ruoyi.im.config;

import com.ruoyi.im.websocket.ImWebSocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * WebSocket分布式消息路由配置
 * 通过Redis Pub/Sub实现跨节点WebSocket消息推送
 */
@Configuration
public class WebSocketRedisListenerConfig {

    private static final Logger log = LoggerFactory.getLogger(WebSocketRedisListenerConfig.class);
    private static final String IM_WS_CHANNEL = "im:ws:broadcast";

    @Autowired
    private RedisMessageListenerContainer redisContainer;

    @PostConstruct
    public void init() {
        // 添加消息监听器，监听WebSocket广播频道
        redisContainer.addMessageListener(new WebSocketMessageListener(), new ChannelTopic(IM_WS_CHANNEL));
        log.info("WebSocket Redis Pub/Sub 监听器已启动，监听频道: {}", IM_WS_CHANNEL);
    }

    /**
     * Redis消息监听器
     * 接收跨节点消息并路由到本地WebSocket会话
     */
    private static class WebSocketMessageListener implements MessageListener {

        private static final Logger listenerLog = LoggerFactory.getLogger(WebSocketMessageListener.class);

        @Override
        @SuppressWarnings("unchecked")
        public void onMessage(Message message, byte[] pattern) {
            try {
                String body = new String(message.getBody());
                listenerLog.debug("收到Redis广播消息: {}", body);

                // 消息格式：
                // 1. 全量广播: {"broadcastAll": true, "messageJson": "..."}
                // 2. 单用户发送: {"targetUserId": Long, "messageJson": "..."}

                if (body.contains("broadcastAll")) {
                    // 全量广播消息
                    com.alibaba.fastjson.JSONObject obj = com.alibaba.fastjson.JSON.parseObject(body);
                    String messageJson = obj.getString("messageJson");
                    if (obj.getBoolean("broadcastAll")) {
                        // 广播给所有在线用户
                        ImWebSocketEndpoint.broadcastToAllOnline(messageJson);
                        listenerLog.debug("全量广播消息已分发到本地用户");
                    }
                } else if (body.contains("targetUserId")) {
                    // 单用户消息
                    com.alibaba.fastjson.JSONObject obj = com.alibaba.fastjson.JSON.parseObject(body);
                    Long targetUserId = obj.getLong("targetUserId");
                    String messageJson = obj.getString("messageJson");

                    if (targetUserId != null) {
                        ImWebSocketEndpoint.sendToUser(targetUserId, messageJson);
                        listenerLog.debug("单用户消息已路由: userId={}", targetUserId);
                    }
                }
            } catch (Exception e) {
                listenerLog.error("处理Redis广播消息异常", e);
            }
        }
    }
}
