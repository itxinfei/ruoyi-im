package com.ruoyi.im.service;

import com.ruoyi.im.dto.message.ImMessageSendRequest;

import java.util.List;
import java.util.Set;

/**
 * IM Message Push Service Interface
 *
 * Provides WebSocket message push, user online management, message broadcast
 *
 * @author ruoyi
 */
public interface ImMessagePushService {

    /**
     * Push message to specified user
     *
     * @param userId User ID
     * @param message Message object
     * @return true if push success
     */
    boolean pushToUser(Long userId, Object message);

    /**
     * Push message to all users in specified session
     *
     * @param conversationId Conversation ID
     * @param message Message object
     * @return Number of users pushed
     */
    int pushToSession(Long conversationId, Object message);

    /**
     * Push message to all members in specified group
     *
     * @param groupId Group ID
     * @param message Message object
     * @return Number of users pushed
     */
    int pushToGroup(Long groupId, Object message);

    /**
     * Broadcast message to all online users
     *
     * @param message Message object
     * @return Number of users pushed
     */
    int broadcastToAll(Object message);

    /**
     * Push user online status
     *
     * @param userId User ID
     * @param online true=online, false=offline
     */
    void pushOnlineStatus(Long userId, boolean online);

    /**
     * Push typing status
     *
     * @param conversationId Conversation ID
     * @param userId User ID
     * @param typing true=typing, false=not typing
     */
    void pushTypingStatus(Long conversationId, Long userId, boolean typing);

    /**
     * Push read receipt
     *
     * @param conversationId Conversation ID
     * @param userId User ID
     * @param messageIds Read message IDs
     */
    void pushReadReceipt(Long conversationId, Long userId, List<Long> messageIds);

    /**
     * Get online user count
     *
     * @return Online user count
     */
    int getOnlineUserCount();

    /**
     * Get all online user IDs
     *
     * @return Online user ID set
     */
    Set<Long> getOnlineUserIds();

    /**
     * Check if user is online
     *
     * @param userId User ID
     * @return true=online, false=offline
     */
    boolean isUserOnline(Long userId);

    /**
     * Disconnect user WebSocket connection
     *
     * @param userId User ID
     */
    void disconnectUser(Long userId);

    /**
     * Send system notification to specified user
     *
     * @param userId User ID
     * @param title Notification title
     * @param content Notification content
     * @param type Notification type
     */
    void sendSystemNotification(Long userId, String title, String content, String type);

    /**
     * Send system notification to all online users
     *
     * @param title Notification title
     * @param content Notification content
     * @param type Notification type
     */
    void broadcastSystemNotification(String title, String content, String type);

    /**
     * Save and send chat message
     *
     * @param request Message send request
     * @param senderId Sender ID
     * @return Message ID
     */
    Long saveAndSendMessage(ImMessageSendRequest request, Long senderId);
}
