package com.ruoyi.im.vo.message;

import lombok.Data;

/**
 * 消息ACK确认响应
 *
 * @author ruoyi
 */
@Data
public class MessageAckVO {

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 客户端消息ID
     */
    private String clientMsgId;

    /**
     * ACK类型：deliver/receive/read
     */
    private String ackType;

    /**
     * 确认时间戳
     */
    private Long timestamp;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建发送确认ACK
     */
    public static MessageAckVO createDeliverAck(String clientMsgId, Long messageId) {
        MessageAckVO ack = new MessageAckVO();
        ack.setClientMsgId(clientMsgId);
        ack.setMessageId(messageId);
        ack.setAckType("deliver");
        ack.setTimestamp(System.currentTimeMillis());
        ack.setSuccess(true);
        return ack;
    }

    /**
     * 创建接收确认ACK
     */
    public static MessageAckVO createReceiveAck(Long messageId) {
        MessageAckVO ack = new MessageAckVO();
        ack.setMessageId(messageId);
        ack.setAckType("receive");
        ack.setTimestamp(System.currentTimeMillis());
        ack.setSuccess(true);
        return ack;
    }

    /**
     * 创建已读确认ACK
     */
    public static MessageAckVO createReadAck(Long messageId) {
        MessageAckVO ack = new MessageAckVO();
        ack.setMessageId(messageId);
        ack.setAckType("read");
        ack.setTimestamp(System.currentTimeMillis());
        ack.setSuccess(true);
        return ack;
    }

    /**
     * 创建错误ACK
     */
    public static MessageAckVO createError(String clientMsgId, String errorMessage) {
        MessageAckVO ack = new MessageAckVO();
        ack.setClientMsgId(clientMsgId);
        ack.setAckType("error");
        ack.setTimestamp(System.currentTimeMillis());
        ack.setSuccess(false);
        ack.setErrorMessage(errorMessage);
        return ack;
    }
}
