package com.ruoyi.im.service;

import com.ruoyi.im.vo.ding.DingMessageVO;

import java.util.Set;

public interface ImWebSocketBroadcastService {
    void broadcastMessageToConversation(Long conversationId, Long messageId, Long senderId);

    void broadcastReactionUpdate(Long conversationId, Long messageId, Long userId, String emoji, String action);

    void broadcastReadReceipt(Long conversationId, Long lastReadMessageId, Long userId);

    void broadcastRecallNotification(Long conversationId, Long messageId, Long userId);

    void broadcastTypingStatus(Long conversationId, Long userId, boolean isTyping);

    void broadcastOnlineStatus(Long userId, boolean online);

    /**
     * 发送通话信令给目标用户 (支持分布式)
     *
     * @param toUserId 目标用户ID
     * @param signal   信令数据
     */
    void sendCallSignal(Long toUserId, Object signal);

    /**
     * 精准发送WebSocket消息给指定用户 (支持分布式)
     *
     * @param userId 目标用户ID
     * @param message 消息对象
     */
    void sendToUser(Long userId, Object message);

    /**
     * 广播DING消息给目标用户
     *
     * @param dingVO         DING消息VO
     * @param targetUserIds 目标用户ID集合
     */
    void broadcastDingMessage(DingMessageVO dingVO, Set<Long> targetUserIds);
}