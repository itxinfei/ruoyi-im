package com.ruoyi.im.service;

import javax.websocket.Session;

/**
 * WebSocket 业务处理服务接口
 */
public interface ImWebSocketService {

    /**
     * 处理连接开启
     */
    void onOpen(Session session, Long userId, String deviceId);

    /**
     * 处理连接关闭
     */
    void onClose(Session session);

    /**
     * 处理错误
     */
    void onError(Session session, Throwable error);

    /**
     * 处理消息
     */
    void onMessage(Session session, String message);

    /**
     * 发送消息
     */
    void sendMessage(Session session, String message);
}
