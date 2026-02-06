package com.ruoyi.im.vo.sync;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 会话同步事件
 *
 * @author ruoyi
 */
@Data
public class ConversationSyncEvent {

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 事件数据
     */
    private Object eventData;

    /**
     * 事件时间戳
     */
    private Long timestamp;

    /**
     * 源设备ID
     */
    private String sourceDeviceId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 创建置顶事件
     */
    public static ConversationSyncEvent createPinEvent(Long conversationId, Boolean pinned, Long userId) {
        ConversationSyncEvent event = new ConversationSyncEvent();
        event.setEventType("pin");
        event.setConversationId(conversationId);
        Map<String, Object> data = new HashMap<>();
        data.put("pinned", pinned);
        event.setEventData(data);
        event.setTimestamp(System.currentTimeMillis());
        event.setUserId(userId);
        return event;
    }

    /**
     * 创建免打扰事件
     */
    public static ConversationSyncEvent createMuteEvent(Long conversationId, Boolean muted, Long userId) {
        ConversationSyncEvent event = new ConversationSyncEvent();
        event.setEventType("mute");
        event.setConversationId(conversationId);
        Map<String, Object> data = new HashMap<>();
        data.put("muted", muted);
        event.setEventData(data);
        event.setTimestamp(System.currentTimeMillis());
        event.setUserId(userId);
        return event;
    }

    /**
     * 创建删除事件
     */
    public static ConversationSyncEvent createDeleteEvent(Long conversationId, Long userId) {
        ConversationSyncEvent event = new ConversationSyncEvent();
        event.setEventType("delete");
        event.setConversationId(conversationId);
        event.setTimestamp(System.currentTimeMillis());
        event.setUserId(userId);
        return event;
    }

    /**
     * 创建归档事件
     */
    public static ConversationSyncEvent createArchiveEvent(Long conversationId, Boolean archived, Long userId) {
        ConversationSyncEvent event = new ConversationSyncEvent();
        event.setEventType("archive");
        event.setConversationId(conversationId);
        Map<String, Object> data = new HashMap<>();
        data.put("archived", archived);
        event.setEventData(data);
        event.setTimestamp(System.currentTimeMillis());
        event.setUserId(userId);
        return event;
    }

    /**
     * 创建已读事件
     */
    public static ConversationSyncEvent createReadEvent(Long conversationId, Long lastReadMessageId, Long userId) {
        ConversationSyncEvent event = new ConversationSyncEvent();
        event.setEventType("read");
        event.setConversationId(conversationId);
        Map<String, Object> data = new HashMap<>();
        data.put("lastReadMessageId", lastReadMessageId);
        data.put("readTime", System.currentTimeMillis());
        event.setEventData(data);
        event.setTimestamp(System.currentTimeMillis());
        event.setUserId(userId);
        return event;
    }
}
