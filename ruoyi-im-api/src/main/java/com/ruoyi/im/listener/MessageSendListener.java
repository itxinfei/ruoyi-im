package com.ruoyi.im.listener;

import com.ruoyi.im.vo.message.ImMessageVO;
import com.ruoyi.im.websocket.ImWebSocketHandler;
import com.ruoyi.im.websocket.WsFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息发送事件监听器
 * 负责将落库成功的消息异步通过 WebSocket 推送给目标用户，并给发送者返回 ACK
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSendListener {

    private final ImWebSocketHandler webSocketHandler;

    @Async("messageTaskExecutor")
    @EventListener
    public void handleMessageSendEvent(MessageSendEvent event) {
        ImMessageVO messageVO = event.getMessageVO();
        Long senderId = messageVO.getSenderId();

        // 1. 给发送者返回 SEND_ACK
        WsFrame ackFrame = new WsFrame();
        ackFrame.setType("MESSAGE");
        ackFrame.setAction("SEND_ACK");
        ackFrame.setRequestId(event.getClientRequestId());
        
        Map<String, Object> ackData = new HashMap<>();
        ackData.put("msgId", messageVO.getId());
        ackData.put("seq", messageVO.getSeq());
        ackData.put("timestamp", messageVO.getTimestamp());
        ackData.put("clientMsgId", messageVO.getClientMsgId());
        ackFrame.setData(ackData);

        webSocketHandler.sendMessageToUser(senderId, ackFrame);

        // 2. 向所有接收者推送 RECEIVE
        WsFrame receiveFrame = new WsFrame();
        receiveFrame.setType("MESSAGE");
        receiveFrame.setAction("RECEIVE");
        receiveFrame.setData(messageVO);
        receiveFrame.setTimestamp(System.currentTimeMillis());

        for (Long targetUserId : event.getTargetUserIds()) {
            if (!targetUserId.equals(senderId)) {
                webSocketHandler.sendMessageToUser(targetUserId, receiveFrame);
            }
        }
    }
}
