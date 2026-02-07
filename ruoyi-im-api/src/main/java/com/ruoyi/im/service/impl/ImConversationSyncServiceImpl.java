package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.domain.ImConversationSyncPoint;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.mapper.ImConversationSyncPointMapper;
import com.ruoyi.im.service.ImConversationMemberService;
import com.ruoyi.im.service.ImConversationSyncService;
import com.ruoyi.im.service.ImWebSocketBroadcastService;
import com.ruoyi.im.vo.sync.ConversationSyncEvent;
import com.ruoyi.im.vo.sync.ConversationSyncResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 会话同步服务实现类
 *
 * @author ruoyi
 */
@Slf4j
@Service
public class ImConversationSyncServiceImpl implements ImConversationSyncService {

    private final ImConversationSyncPointMapper syncPointMapper;
    private final ImConversationMemberService conversationMemberService;
    private final ImConversationMemberMapper conversationMemberMapper;
    private final ImWebSocketBroadcastService broadcastService;

    /**
     * 会话事件存储（临时缓存，用于同步）
     * 格式：userId -> List<ConversationSyncEvent>
     */
    private static final Map<Long, List<ConversationSyncEvent>> eventCache = new ConcurrentHashMap<>();

    /**
     * 事件保留时间（7天）
     */
    private static final long EVENT_RETENTION_DAYS = 7;

    public ImConversationSyncServiceImpl(ImConversationSyncPointMapper syncPointMapper,
                                         ImConversationMemberService conversationMemberService,
                                         ImConversationMemberMapper conversationMemberMapper,
                                         ImWebSocketBroadcastService broadcastService) {
        this.syncPointMapper = syncPointMapper;
        this.conversationMemberService = conversationMemberService;
        this.conversationMemberMapper = conversationMemberMapper;
        this.broadcastService = broadcastService;
    }

    /**
     * 会话事件存储（临时缓存，用于同步）
     * 格式：userId -> List<ConversationSyncEvent>
     */
    private static final Map<Long, List<ConversationSyncEvent>> eventCache = new ConcurrentHashMap<>();

    /**
     * 事件保留时间（7天）
     */
    private static final long EVENT_RETENTION_DAYS = 7;

    @Override
    public ConversationSyncResponse syncConversations(Long userId, String deviceId, Long lastSyncTime) {
        if (userId == null || deviceId == null) {
            return ConversationSyncResponse.empty();
        }

        // 如果没有提供同步时间，获取设备的最后同步时间
        if (lastSyncTime == null || lastSyncTime <= 0) {
            ImConversationSyncPoint syncPoint = syncPointMapper.selectByUserAndDevice(userId, deviceId);
            if (syncPoint != null) {
                lastSyncTime = syncPoint.getLastSyncTime();
            } else {
                lastSyncTime = 0L;
            }
        }

        // 查询该时间之后的事件
        List<ConversationSyncEvent> events = getEventsSince(userId, lastSyncTime);

        // 计算新的同步时间
        Long newSyncTime = lastSyncTime;
        if (!events.isEmpty()) {
            ConversationSyncEvent lastEvent = events.get(events.size() - 1);
            newSyncTime = lastEvent.getTimestamp();
        } else {
            newSyncTime = System.currentTimeMillis();
        }

        // 更新同步点
        updateSyncPoint(userId, deviceId, newSyncTime);

        return ConversationSyncResponse.create(events, newSyncTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSyncPoint(Long userId, String deviceId, Long lastSyncTime) {
        if (userId == null || deviceId == null) {
            return;
        }

        ImConversationSyncPoint syncPoint = syncPointMapper.selectByUserAndDevice(userId, deviceId);

        if (syncPoint == null) {
            // 创建新同步点
            syncPoint = new ImConversationSyncPoint();
            syncPoint.setUserId(userId);
            syncPoint.setDeviceId(deviceId);
            syncPoint.setLastSyncTime(lastSyncTime);
            syncPoint.setCreateTime(LocalDateTime.now());
            syncPoint.setUpdateTime(LocalDateTime.now());
            syncPointMapper.insert(syncPoint);
        } else {
            // 更新同步点
            syncPoint.setLastSyncTime(lastSyncTime);
            syncPoint.setUpdateTime(LocalDateTime.now());
            syncPointMapper.updateById(syncPoint);
        }
    }

    @Override
    public void broadcastConversationEvent(Long userId, Long conversationId, String eventType,
            Object eventData, String excludeDeviceId) {
        if (userId == null || conversationId == null || eventType == null) {
            return;
        }

        // 创建同步事件
        ConversationSyncEvent event = new ConversationSyncEvent();
        event.setEventType(eventType);
        event.setConversationId(conversationId);
        event.setEventData(eventData);
        event.setTimestamp(System.currentTimeMillis());
        event.setUserId(userId);
        event.setSourceDeviceId(excludeDeviceId);

        // 存储到缓存
        eventCache.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(event);

        // 广播到其他设备
        Map<String, Object> message = new HashMap<>();
        message.put("type", "conversation_sync");
        message.put("event", event);
        message.put("timestamp", event.getTimestamp());

        broadcastService.broadcastToUserExcept(userId, message);
        log.info("广播会话事件: userId={}, conversationId={}, eventType={}, excludeDeviceId={}",
                userId, conversationId, eventType, excludeDeviceId);
    }

    @Override
    public void handleConversationEvent(Long userId, ConversationSyncEvent event) {
        if (userId == null || event == null) {
            return;
        }

        // 验证事件是否属于当前用户
        if (!event.getUserId().equals(userId)) {
            log.warn("会话事件用户不匹配，忽略: eventUserId={}, currentUserId={}",
                    event.getUserId(), userId);
            return;
        }

        // 根据事件类型处理
        switch (event.getEventType()) {
            case "pin":
                onPinEvent(event);
                break;
            case "mute":
                onMuteEvent(event);
                break;
            case "delete":
                onDeleteEvent(event);
                break;
            case "archive":
                onArchiveEvent(event);
                break;
            case "read":
                onReadEvent(event);
                break;
            default:
                log.warn("未知的会话事件类型: {}", event.getEventType());
        }
    }

    @Override
    public List<ImConversationSyncPoint> getUserSyncPoints(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return syncPointMapper.selectByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetSyncPoint(Long userId, String deviceId) {
        if (userId == null || deviceId == null) {
            return;
        }

        syncPointMapper.deleteByUserAndDevice(userId, deviceId);
        log.info("重置会话同步点: userId={}, deviceId={}", userId, deviceId);
    }

    /**
     * 获取指定时间之后的会话事件
     */
    private List<ConversationSyncEvent> getEventsSince(Long userId, Long sinceTime) {
        List<ConversationSyncEvent> userEvents = eventCache.get(userId);
        if (userEvents == null || userEvents.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDateTime since = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(sinceTime),
                ZoneId.systemDefault());

        return userEvents.stream()
                .filter(event -> event.getTimestamp() != null &&
                        LocalDateTime.ofInstant(
                                java.time.Instant.ofEpochMilli(event.getTimestamp()),
                                ZoneId.systemDefault()).isAfter(since))
                .sorted((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()))
                .collect(Collectors.toList());
    }

    /**
     * 处理置顶事件
     */
    private void onPinEvent(ConversationSyncEvent event) {
        Map<?, ?> eventData = (Map<?, ?>) event.getEventData();
        if (eventData == null) {
            return;
        }

        Object pinnedValue = eventData.get("pinned");
        int pinned = (pinnedValue instanceof Boolean) ? ((Boolean) pinnedValue) ? 1 : 0
                : (pinnedValue instanceof Number) ? ((Number) pinnedValue).intValue() : 0;

        conversationMemberService.togglePin(event.getConversationId(), event.getUserId(), pinned);
        log.info("同步置顶状态: conversationId={}, pinned={}", event.getConversationId(), pinned);
    }

    /**
     * 处理免打扰事件
     */
    private void onMuteEvent(ConversationSyncEvent event) {
        Map<?, ?> eventData = (Map<?, ?>) event.getEventData();
        if (eventData == null) {
            return;
        }

        Object mutedValue = eventData.get("muted");
        int muted = (mutedValue instanceof Boolean) ? ((Boolean) mutedValue) ? 1 : 0
                : (mutedValue instanceof Number) ? ((Number) mutedValue).intValue() : 0;

        conversationMemberService.toggleMute(event.getConversationId(), event.getUserId(), muted);
        log.info("同步免打扰状态: conversationId={}, muted={}", event.getConversationId(), muted);
    }

    /**
     * 处理删除事件
     */
    private void onDeleteEvent(ConversationSyncEvent event) {
        try {
            // 软删除（逻辑删除）
            ImConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(
                    event.getConversationId(), event.getUserId());

            if (member != null) {
                member.setIsDeleted(1);
                member.setDeletedTime(LocalDateTime.now());
                member.setUpdateTime(LocalDateTime.now());
                conversationMemberMapper.updateById(member);

                log.info("同步会话删除: conversationId={}", event.getConversationId());
            }
        } catch (Exception e) {
            log.error("处理会话删除事件失败: conversationId={}", event.getConversationId(), e);
        }
    }

    /**
     * 处理归档事件
     */
    private void onArchiveEvent(ConversationSyncEvent event) {
        Map<?, ?> eventData = (Map<?, ?>) event.getEventData();
        if (eventData == null) {
            return;
        }

        Object archivedValue = eventData.get("archived");
        int archived = (archivedValue instanceof Boolean) ? ((Boolean) archivedValue) ? 1 : 0
                : (archivedValue instanceof Number) ? ((Number) archivedValue).intValue() : 0;

        try {
            ImConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(
                    event.getConversationId(), event.getUserId());

            if (member != null) {
                member.setIsArchived(archived);
                member.setUpdateTime(LocalDateTime.now());
                conversationMemberMapper.updateById(member);

                log.info("同步归档状态: conversationId={}, archived={}", event.getConversationId(), archived);
            }
        } catch (Exception e) {
            log.error("处理归档事件失败: conversationId={}", event.getConversationId(), e);
        }
    }

    /**
     * 处理已读事件
     */
    private void onReadEvent(ConversationSyncEvent event) {
        Map<?, ?> eventData = (Map<?, ?>) event.getEventData();
        if (eventData == null) {
            return;
        }

        Object lastReadIdValue = eventData.get("lastReadMessageId");
        Long lastReadMessageId = null;

        if (lastReadIdValue instanceof Number) {
            lastReadMessageId = ((Number) lastReadIdValue).longValue();
        }

        if (lastReadMessageId != null) {
            try {
                ImConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(
                        event.getConversationId(), event.getUserId());

                if (member != null) {
                    member.setLastReadMessageId(lastReadMessageId);
                    member.setLastReadTime(LocalDateTime.now());
                    member.setUpdateTime(LocalDateTime.now());
                    conversationMemberMapper.updateById(member);

                    log.debug("同步已读状态: conversationId={}, lastReadMessageId={}",
                            event.getConversationId(), lastReadMessageId);
                }
            } catch (Exception e) {
                log.error("处理已读事件失败: conversationId={}", event.getConversationId(), e);
            }
        }
    }

    /**
     * 定时任务：清理过期事件缓存
     * 每小时清理一次
     */
    @Scheduled(fixedRate = 3600000)
    public void cleanExpiredEvents() {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(EVENT_RETENTION_DAYS);

        int totalCleaned = 0;
        for (Map.Entry<Long, List<ConversationSyncEvent>> entry : eventCache.entrySet()) {
            Long userId = entry.getKey();
            List<ConversationSyncEvent> events = entry.getValue();

            events.removeIf(event -> {
                LocalDateTime eventTime = LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(event.getTimestamp()),
                        ZoneId.systemDefault());
                return eventTime.isBefore(expireTime);
            });

            if (events.isEmpty()) {
                eventCache.remove(userId);
            }

            totalCleaned += events.size();
        }

        if (totalCleaned > 0) {
            log.info("清理过期会话事件: count={}", totalCleaned);
        }
    }
}
