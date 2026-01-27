package com.ruoyi.im.service;

import com.ruoyi.im.vo.ding.DingMessageVO;

import java.util.Set;

public interface ImWebSocketBroadcastService {
    void broadcastMessageToConversation(Long conversationId, Long messageId, Long senderId);

    /**
     * 广播消息给会话成员（优化版，避免重复查询发送者信息）
     *
     * @param conversationId 会话ID
     * @param messageId      消息ID
     * @param sender         发送者信息
     */
    void broadcastMessageToConversation(Long conversationId, Long messageId, com.ruoyi.im.domain.ImUser sender);

    void broadcastReactionUpdate(Long conversationId, Long messageId, Long userId, String emoji, String action);

    void broadcastReadReceipt(Long conversationId, Long lastReadMessageId, Long userId);

    void broadcastTypingStatus(Long conversationId, Long userId, boolean isTyping);

    void broadcastOnlineStatus(Long userId, boolean online);

    /**
     * 广播DING消息给目标用户
     *
     * @param dingVO         DING消息VO
     * @param targetUserIds 目标用户ID集合
     */
    void broadcastDingMessage(DingMessageVO dingVO, Set<Long> targetUserIds);

    /**
     * 广播消息撤回通知
     *
     * @param conversationId 会话ID
     * @param messageId      消息ID
     * @param userId         操作用户ID
     */
    void broadcastRecallNotification(Long conversationId, Long messageId, Long userId);
}