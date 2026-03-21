package com.ruoyi.im.websocket;

import lombok.Data;

/**
 * WebSocket 标准数据帧
 * 严格对齐 Doc-34 协议 §1.1
 */
@Data
public class WsFrame {
    /**
     * 消息大类: AUTH, MESSAGE, NOTIFY, EVENT, CALL
     */
    private String type;

    /**
     * 具体动作: SEND, RECEIVE, PING, PONG 等
     */
    private String action;

    /**
     * 请求 ID (用于防重放与 ACK 确认)
     */
    private String requestId;

    /**
     * 业务数据载荷
     */
    private Object data;

    /**
     * 时间戳 (Long)
     */
    private Long timestamp;
}
