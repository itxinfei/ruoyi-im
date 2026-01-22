package com.ruoyi.im.service;

public interface ImWebSocketBroadcastService {
    void broadcastMessageToConversation(Long conversationId, Long messageId, Long senderId);

    void broadcastReactionUpdate(Long conversationId, Long messageId, Long userId, String emoji, String action);

    void broadcastReadReceipt(Long conversationId, Long lastReadMessageId, Long userId);

    void broadcastTypingStatus(Long conversationId, Long userId, boolean isTyping);

    void broadcastOnlineStatus(Long userId, boolean online);
}