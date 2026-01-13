package com.ruoyi.im.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.service.IOfflineMessageService;
import com.ruoyi.im.service.ImMessagePushService;
import com.ruoyi.im.websocket.ImWebSocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 离线消息服务实现
 * <p>
 * 使用Redis存储离线消息，确保消息不丢失
 * 消息保留7天，过期自动清理
 * </p>
 *
 * @author ruoyi
 */
@Service
public class OfflineMessageServiceImpl implements IOfflineMessageService {

    private static final Logger log = LoggerFactory.getLogger(OfflineMessageServiceImpl.class);

    private static final String OFFLINE_KEY_PREFIX = "im:offline:";
    private static final long OFFLINE_MESSAGE_EXPIRE_DAYS = 7;
    private static final int MAX_OFFLINE_MESSAGES = 1000;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean storeOfflineMessage(Long userId, ImMessage message) {
        if (userId == null || message == null) {
            return false;
        }

        try {
            String key = OFFLINE_KEY_PREFIX + userId;

            // 检查离线消息数量，避免无限增长
            Long count = redisTemplate.opsForList().size(key);
            if (count != null && count >= MAX_OFFLINE_MESSAGES) {
                log.warn("离线消息数量已达上限，删除最旧的消息: userId={}, count={}", userId, count);
                redisTemplate.opsForList().trim(key, 1, -1);
            }

            // 序列化消息
            String messageJson = objectMapper.writeValueAsString(message);

            // 存储到Redis列表头部
            redisTemplate.opsForList().leftPush(key, messageJson);

            // 设置过期时间
            redisTemplate.expire(key, OFFLINE_MESSAGE_EXPIRE_DAYS, TimeUnit.DAYS);

            log.debug("存储离线消息: userId={}, messageId={}", userId, message.getId());
            return true;
        } catch (Exception e) {
            log.error("存储离线消息失败: userId={}", userId, e);
            return false;
        }
    }

    @Override
    public int batchStoreOfflineMessage(Collection<Long> userIds, ImMessage message) {
        if (userIds == null || userIds.isEmpty() || message == null) {
            return 0;
        }

        int successCount = 0;
        for (Long userId : userIds) {
            if (storeOfflineMessage(userId, message)) {
                successCount++;
            }
        }

        log.info("批量存储离线消息: 总数={}, 成功={}", userIds.size(), successCount);
        return successCount;
    }

    @Override
    public long getOfflineMessageCount(Long userId) {
        if (userId == null) {
            return 0;
        }

        String key = OFFLINE_KEY_PREFIX + userId;
        Long size = redisTemplate.opsForList().size(key);
        return size != null ? size : 0;
    }

    @Override
    public List<ImMessage> getOfflineMessages(Long userId, int limit) {
        if (userId == null) {
            return new ArrayList<>();
        }

        String key = OFFLINE_KEY_PREFIX + userId;
        List<Object> messageObjects = redisTemplate.opsForList().range(key, 0, limit - 1);

        List<ImMessage> messages = new ArrayList<>();
        if (messageObjects != null && !messageObjects.isEmpty()) {
            for (Object obj : messageObjects) {
                try {
                    String messageJson = (String) obj;
                    ImMessage message = objectMapper.readValue(messageJson, ImMessage.class);
                    messages.add(message);
                } catch (Exception e) {
                    log.error("解析离线消息失败: userId={}, message={}", userId, obj, e);
                }
            }
        }

        return messages;
    }

    @Override
    public long clearOfflineMessages(Long userId) {
        if (userId == null) {
            return 0;
        }

        String key = OFFLINE_KEY_PREFIX + userId;
        Long count = redisTemplate.opsForList().size(key);
        redisTemplate.delete(key);

        log.info("清空离线消息: userId={}, count={}", userId, count);
        return count != null ? count : 0;
    }

    @Override
    public int pushAndClearOfflineMessages(Long userId) {
        if (userId == null) {
            return 0;
        }

        // 检查用户是否在线
        if (!ImWebSocketEndpoint.isUserOnline(userId)) {
            log.warn("用户不在线，无法推送离线消息: userId={}", userId);
            return 0;
        }

        // 获取所有离线消息
        List<ImMessage> offlineMessages = getOfflineMessages(userId, MAX_OFFLINE_MESSAGES);

        if (offlineMessages.isEmpty()) {
            return 0;
        }

        int pushedCount = 0;
        for (ImMessage message : offlineMessages) {
            try {
                // 构造推送消息格式
                OfflineMessagePush pushMsg = new OfflineMessagePush();
                pushMsg.setType("offline_message");
                pushMsg.setMessageId(message.getId());
                pushMsg.setConversationId(message.getConversationId());
                pushMsg.setSenderId(message.getSenderId());
                pushMsg.setMessageType(message.getMessageType());
                pushMsg.setContent(message.getContent());
                pushMsg.setCreateTime(message.getCreateTime());
                pushMsg.setIsOffline(true);

                // 推送给用户
                ImWebSocketEndpoint.sendToUser(userId, pushMsg);
                pushedCount++;
            } catch (Exception e) {
                log.error("推送离线消息失败: userId={}, messageId={}", userId, message.getId(), e);
            }
        }

        // 推送完成后清空离线消息
        clearOfflineMessages(userId);

        log.info("推送离线消息完成: userId={}, count={}", userId, pushedCount);
        return pushedCount;
    }

    @Override
    public void checkAndPushOfflineMessages(Long userId) {
        if (userId == null) {
            return;
        }

        // 异步推送离线消息，避免阻塞WebSocket连接建立
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(500); // 延迟500ms，确保连接完全建立
                pushAndClearOfflineMessages(userId);
            } catch (Exception e) {
                log.error("检查和推送离线消息失败: userId={}", userId, e);
            }
        });
    }

    /**
     * 离线消息推送DTO
     */
    public static class OfflineMessagePush {
        private String type;
        private Long messageId;
        private Long conversationId;
        private Long senderId;
        private String messageType;
        private String content;
        private java.time.LocalDateTime createTime;
        private Boolean isOffline;

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public Long getMessageId() { return messageId; }
        public void setMessageId(Long messageId) { this.messageId = messageId; }

        public Long getConversationId() { return conversationId; }
        public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

        public Long getSenderId() { return senderId; }
        public void setSenderId(Long senderId) { this.senderId = senderId; }

        public String getMessageType() { return messageType; }
        public void setMessageType(String messageType) { this.messageType = messageType; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public java.time.LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(java.time.LocalDateTime createTime) { this.createTime = createTime; }

        public Boolean getIsOffline() { return isOffline; }
        public void setIsOffline(Boolean isOffline) { this.isOffline = isOffline; }
    }
}
