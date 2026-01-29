package com.ruoyi.im.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.service.ImWebSocketBroadcastService;
import com.ruoyi.im.util.MessageEncryptionUtil;
import com.ruoyi.im.vo.ding.DingMessageVO;
import com.ruoyi.im.websocket.ImWebSocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ImWebSocketBroadcastServiceImpl implements ImWebSocketBroadcastService {

    private static final Logger log = LoggerFactory.getLogger(ImWebSocketBroadcastServiceImpl.class);

    private final ImConversationMemberMapper conversationMemberMapper;
    private final ImMessageMapper messageMapper;
    private final com.ruoyi.im.mapper.ImUserMapper imUserMapper;
    private final MessageEncryptionUtil encryptionUtil;
    private final ObjectMapper objectMapper;

    public ImWebSocketBroadcastServiceImpl(
            ImConversationMemberMapper conversationMemberMapper,
            ImMessageMapper messageMapper,
            com.ruoyi.im.mapper.ImUserMapper imUserMapper,
            MessageEncryptionUtil encryptionUtil,
            ObjectMapper objectMapper) {
        this.conversationMemberMapper = conversationMemberMapper;
        this.messageMapper = messageMapper;
        this.imUserMapper = imUserMapper;
        this.encryptionUtil = encryptionUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public void broadcastMessageToConversation(Long conversationId, Long messageId, Long senderId) {
        broadcastMessageToConversation(conversationId, messageId, (com.ruoyi.im.domain.ImUser) null);
    }

    @Override
    public void broadcastMessageToConversation(Long conversationId, Long messageId, com.ruoyi.im.domain.ImUser sender) {
        try {
            List<ImConversationMember> members = conversationMemberMapper.selectByConversationId(conversationId);
            if (members == null || members.isEmpty()) {
                return;
            }

            ImMessage message = messageMapper.selectImMessageById(messageId);
            if (message == null) {
                return;
            }

            // 构建标准推送格式: {type: 'message', data: {...}}
            Map<String, Object> wsMessage = new HashMap<>();
            wsMessage.put("type", "message");
            wsMessage.put("data", createMessageData(message, sender));

            String messageJson = objectMapper.writeValueAsString(wsMessage);

            broadcastToMembers(members, messageJson, sender != null ? sender.getId() : message.getSenderId());
        } catch (Exception e) {
            log.error("广播消息异常: conversationId={}, messageId={}", conversationId, messageId, e);
        }
    }

    @Override
    public void broadcastReactionUpdate(Long conversationId, Long messageId, Long userId, String emoji, String action) {
        try {
            List<ImConversationMember> members = conversationMemberMapper.selectByConversationId(conversationId);
            if (members == null || members.isEmpty()) {
                return;
            }

            Map<String, Object> reactionMessage = createReactionMessage(conversationId, messageId, userId, emoji,
                    action);
            String messageJson = objectMapper.writeValueAsString(reactionMessage);

            broadcastToMembers(members, messageJson, userId);
        } catch (Exception e) {
            log.error("广播反应更新异常: conversationId={}, messageId={}", conversationId, messageId, e);
        }
    }

    @Override
    public void broadcastReadReceipt(Long conversationId, Long lastReadMessageId, Long userId) {
        try {
            List<ImConversationMember> members = conversationMemberMapper.selectByConversationId(conversationId);
            if (members == null || members.isEmpty()) {
                return;
            }

            Map<String, Object> readReceipt = createReadReceiptMessage(conversationId, lastReadMessageId, userId);
            String messageJson = objectMapper.writeValueAsString(readReceipt);

            broadcastToMembers(members, messageJson, userId);
        } catch (Exception e) {
            log.error("广播已读回执异常: conversationId={}", conversationId, e);
        }
    }

    @Override
    public void broadcastTypingStatus(Long conversationId, Long userId, boolean isTyping) {
        try {
            List<ImConversationMember> members = conversationMemberMapper.selectByConversationId(conversationId);
            if (members == null || members.isEmpty()) {
                return;
            }

            Map<String, Object> statusMap = new HashMap<>();
            statusMap.put("type", "typing");
            Map<String, Object> data = new HashMap<>();
            data.put("conversationId", conversationId);
            data.put("userId", userId);
            data.put("isTyping", isTyping);
            data.put("timestamp", System.currentTimeMillis());
            statusMap.put("data", data);

            String messageJson = objectMapper.writeValueAsString(statusMap);
            broadcastToMembers(members, messageJson, userId);
        } catch (Exception e) {
            log.error("广播输入状态异常: conversationId={}", conversationId, e);
        }
    }

    @Override
    public void broadcastOnlineStatus(Long userId, boolean online) {
        try {
            Map<String, Object> statusMap = new HashMap<>();
            statusMap.put("type", online ? "online" : "offline");

            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("online", online);
            data.put("timestamp", System.currentTimeMillis());
            statusMap.put("data", data);

            String messageJson = objectMapper.writeValueAsString(statusMap);

            // 广播给所有在线用户
            Map<Long, javax.websocket.Session> onlineUsers = ImWebSocketEndpoint.getOnlineUsers();
            for (javax.websocket.Session session : onlineUsers.values()) {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(messageJson);
                    } catch (Exception e) {
                        // 忽略单个发送失败
                    }
                }
            }
        } catch (Exception e) {
            log.error("广播在线状态异常: userId={}", userId, e);
        }
    }

    @Override
    public void broadcastDingMessage(DingMessageVO dingVO, Set<Long> targetUserIds) {
        try {
            // 构建DING消息格式
            Map<String, Object> wsMessage = new HashMap<>();
            wsMessage.put("type", "ding");
            wsMessage.put("data", dingVO);

            String messageJson = objectMapper.writeValueAsString(wsMessage);

            // 发送给目标用户
            Map<Long, javax.websocket.Session> onlineUsers = ImWebSocketEndpoint.getOnlineUsers();
            for (Long targetUserId : targetUserIds) {
                javax.websocket.Session targetSession = onlineUsers.get(targetUserId);
                if (targetSession != null && targetSession.isOpen()) {
                    try {
                        targetSession.getBasicRemote().sendText(messageJson);
                        log.debug("DING消息已推送给用户: userId={}, dingId={}", targetUserId, dingVO.getId());
                    } catch (Exception e) {
                        log.error("发送DING消息给用户失败: userId={}", targetUserId, e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("广播DING消息异常: dingId={}", dingVO.getId(), e);
        }
    }

    @Override
    public void broadcastRecallNotification(Long conversationId, Long messageId, Long userId) {
        try {
            Map<String, Object> recallNotification = new HashMap<>();
            recallNotification.put("type", "recall");
            recallNotification.put("conversationId", conversationId);
            recallNotification.put("messageId", messageId);
            recallNotification.put("userId", userId);
            recallNotification.put("timestamp", java.time.LocalDateTime.now().toString());

            // 获取用户信息
            com.ruoyi.im.domain.ImUser user = imUserMapper.selectImUserById(userId);
            if (user != null) {
                recallNotification.put("userName",
                        user.getNickname() != null ? user.getNickname() : user.getUsername());
            }

            // 获取会话成员
            List<ImConversationMember> members = conversationMemberMapper.selectByConversationId(conversationId);
            if (members == null || members.isEmpty()) {
                return;
            }

            String messageJson = objectMapper.writeValueAsString(recallNotification);
            broadcastToMembers(members, messageJson, userId); // Exclude the sender/revoker themselves? Original logic excluded userId.
        } catch (Exception e) {
            log.error("广播撤回通知失败: messageId={}", messageId, e);
        }
    }

    @Override
    public void broadcastAnnouncement(Long announcementId, Set<Long> targetUserIds) {
        try {
            // 构建公告通知消息
            Map<String, Object> announcementNotification = new HashMap<>();
            announcementNotification.put("type", "announcement");
            Map<String, Object> data = new HashMap<>();
            data.put("announcementId", announcementId);
            data.put("action", "PUBLISHED");
            data.put("timestamp", System.currentTimeMillis());
            announcementNotification.put("data", data);

            String messageJson = objectMapper.writeValueAsString(announcementNotification);

            // 发送给目标用户
            Map<Long, javax.websocket.Session> onlineUsers = ImWebSocketEndpoint.getOnlineUsers();
            int sentCount = 0;
            for (Long targetUserId : targetUserIds) {
                javax.websocket.Session targetSession = onlineUsers.get(targetUserId);
                if (targetSession != null && targetSession.isOpen()) {
                    try {
                        targetSession.getBasicRemote().sendText(messageJson);
                        sentCount++;
                    } catch (Exception e) {
                        log.error("发送公告通知给用户失败: userId={}, announcementId={}", targetUserId, announcementId, e);
                    }
                }
            }
            log.info("公告通知已推送: announcementId={}, targetCount={}, onlineSent={}",
                    announcementId, targetUserIds.size(), sentCount);
        } catch (Exception e) {
            log.error("广播公告通知失败: announcementId={}", announcementId, e);
        }
    }

    private Map<String, Object> createMessageData(ImMessage message, com.ruoyi.im.domain.ImUser sender) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", message.getId());
        data.put("conversationId", message.getConversationId());
        data.put("sessionId", message.getConversationId()); // 兼容旧版前端
        data.put("senderId", message.getSenderId());
        data.put("type", message.getMessageType() != null ? message.getMessageType().toUpperCase() : "TEXT");
        data.put("content", encryptionUtil.decryptMessage(message.getContent()));

        // 尝试获取发送者信息
        try {
            if (sender == null) {
                sender = imUserMapper.selectImUserById(message.getSenderId());
            }
            
            if (sender != null) {
                data.put("senderName", sender.getNickname());
                data.put("senderAvatar", sender.getAvatar());
            }
        } catch (Exception e) {
            log.warn("构建WS消息时获取发送者信息失败: userId={}", message.getSenderId());
        }

        // 统一时间戳为毫秒数
        long timestamp = System.currentTimeMillis();
        if (message.getCreateTime() != null) {
            timestamp = message.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        data.put("timestamp", timestamp);

        if (message.getFileUrl() != null) {
            data.put("fileUrl", message.getFileUrl());
            data.put("fileName", message.getFileName());
            data.put("fileSize", message.getFileSize());
        }

        if (message.getReplyToMessageId() != null) {
            data.put("replyToMessageId", message.getReplyToMessageId());
        }

        if (message.getIsRevoked() != null && message.getIsRevoked() == 1) {
            data.put("isRevoked", 1);
            data.put("content", "[消息已撤回]");
        }

        return data;
    }

    private Map<String, Object> createReactionMessage(Long conversationId, Long messageId, Long userId, String emoji,
            String action) {
        Map<String, Object> reactionMessage = new HashMap<>();
        reactionMessage.put("type", "reaction");
        reactionMessage.put("action", action);
        reactionMessage.put("conversationId", conversationId);
        reactionMessage.put("messageId", messageId);
        reactionMessage.put("userId", userId);
        if (emoji != null) {
            reactionMessage.put("emoji", emoji);
        }
        reactionMessage.put("timestamp", System.currentTimeMillis());
        return reactionMessage;
    }

    private Map<String, Object> createReadReceiptMessage(Long conversationId, Long lastReadMessageId, Long userId) {
        Map<String, Object> readReceipt = new HashMap<>();
        readReceipt.put("type", "read");
        readReceipt.put("conversationId", conversationId);
        readReceipt.put("lastReadMessageId", lastReadMessageId);
        readReceipt.put("userId", userId);
        readReceipt.put("timestamp", System.currentTimeMillis());
        return readReceipt;
    }

    private void broadcastToMembers(List<ImConversationMember> members, String messageJson, Long excludeUserId) {
        Map<Long, javax.websocket.Session> onlineUsers = ImWebSocketEndpoint.getOnlineUsers();

        for (ImConversationMember member : members) {
            Long targetUserId = member.getUserId();
            if (targetUserId.equals(excludeUserId)) {
                continue;
            }

            javax.websocket.Session targetSession = onlineUsers.get(targetUserId);
            if (targetSession != null && targetSession.isOpen()) {
                try {
                    targetSession.getBasicRemote().sendText(messageJson);
                } catch (Exception e) {
                    log.error("发送消息给用户失败: userId={}", targetUserId, e);
                }
            }
        }
    }

    @Override
    public void broadcastMeetingInvitation(Long meetingId, Set<Long> targetUserIds, Long inviterId) {
        try {
            // 获取邀请人信息
            com.ruoyi.im.domain.ImUser inviter = imUserMapper.selectImUserById(inviterId);

            // 构建会议邀请消息
            Map<String, Object> invitationNotification = new HashMap<>();
            invitationNotification.put("type", "meeting_invitation");

            Map<String, Object> data = new HashMap<>();
            data.put("meetingId", meetingId);
            data.put("action", "INVITED");
            data.put("inviterId", inviterId);
            if (inviter != null) {
                data.put("inviterName", inviter.getNickname() != null ? inviter.getNickname() : inviter.getUsername());
            }
            data.put("timestamp", System.currentTimeMillis());
            invitationNotification.put("data", data);

            String messageJson = objectMapper.writeValueAsString(invitationNotification);

            // 发送给目标用户
            Map<Long, javax.websocket.Session> onlineUsers = ImWebSocketEndpoint.getOnlineUsers();
            int sentCount = 0;
            for (Long targetUserId : targetUserIds) {
                javax.websocket.Session targetSession = onlineUsers.get(targetUserId);
                if (targetSession != null && targetSession.isOpen()) {
                    try {
                        targetSession.getBasicRemote().sendText(messageJson);
                        sentCount++;
                    } catch (Exception e) {
                        log.error("发送会议邀请给用户失败: userId={}, meetingId={}", targetUserId, meetingId, e);
                    }
                }
            }
            log.info("会议邀请已推送: meetingId={}, targetCount={}, onlineSent={}",
                    meetingId, targetUserIds.size(), sentCount);
        } catch (Exception e) {
            log.error("广播会议邀请失败: meetingId={}", meetingId, e);
        }
    }

    @Override
    public void broadcastMeetingRoomBooking(Long bookingId, String roomName, String startTime, String endTime,
                                             Set<Long> targetUserIds, Long bookerId) {
        try {
            // 获取预订人信息
            com.ruoyi.im.domain.ImUser booker = imUserMapper.selectImUserById(bookerId);

            // 构建会议室预订通知消息
            Map<String, Object> bookingNotification = new HashMap<>();
            bookingNotification.put("type", "meeting_room_booking");

            Map<String, Object> data = new HashMap<>();
            data.put("bookingId", bookingId);
            data.put("roomName", roomName);
            data.put("startTime", startTime);
            data.put("endTime", endTime);
            data.put("bookerId", bookerId);
            if (booker != null) {
                data.put("bookerName", booker.getNickname() != null ? booker.getNickname() : booker.getUsername());
            }
            data.put("timestamp", System.currentTimeMillis());
            bookingNotification.put("data", data);

            String messageJson = objectMapper.writeValueAsString(bookingNotification);

            // 发送给目标用户
            Map<Long, javax.websocket.Session> onlineUsers = ImWebSocketEndpoint.getOnlineUsers();
            int sentCount = 0;
            for (Long targetUserId : targetUserIds) {
                javax.websocket.Session targetSession = onlineUsers.get(targetUserId);
                if (targetSession != null && targetSession.isOpen()) {
                    try {
                        targetSession.getBasicRemote().sendText(messageJson);
                        sentCount++;
                    } catch (Exception e) {
                        log.error("发送会议室预订通知给用户失败: userId={}, bookingId={}", targetUserId, bookingId, e);
                    }
                }
            }
            log.info("会议室预订通知已推送: bookingId={}, targetCount={}, onlineSent={}",
                    bookingId, targetUserIds.size(), sentCount);
        } catch (Exception e) {
            log.error("广播会议室预订通知失败: bookingId={}", bookingId, e);
        }
    }
}