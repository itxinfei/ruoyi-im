package com.ruoyi.im.listener;

import com.ruoyi.im.vo.message.ImMessageVO;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 消息发送完成事件
 * 用于解耦“消息落库”与“WebSocket 下发”
 */
public class MessageSendEvent extends ApplicationEvent {

    private final ImMessageVO messageVO;
    private final List<Long> targetUserIds; // 接收方 ID 列表（单聊为1个，群聊为多个）
    private final String clientRequestId;   // 用于返回 ACK 的原始请求 ID

    public MessageSendEvent(Object source, ImMessageVO messageVO, List<Long> targetUserIds, String clientRequestId) {
        super(source);
        this.messageVO = messageVO;
        this.targetUserIds = targetUserIds;
        this.clientRequestId = clientRequestId;
    }

    public ImMessageVO getMessageVO() {
        return messageVO;
    }

    public List<Long> getTargetUserIds() {
        return targetUserIds;
    }

    public String getClientRequestId() {
        return clientRequestId;
    }
}
