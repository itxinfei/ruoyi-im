package com.ruoyi.web.service.impl;

import com.ruoyi.im.domain.ImSystemNotification;
import com.ruoyi.web.mapper.ImSystemNotificationMapper;
import com.ruoyi.web.service.ImSystemNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * IM系统通知Service实现（Admin模块专用）
 */
@Service
public class ImSystemNotificationServiceImpl implements ImSystemNotificationService {

    @Autowired
    private ImSystemNotificationMapper notificationMapper;

    @Override
    public List<ImSystemNotification> selectNotificationList(ImSystemNotification notification) {
        return notificationMapper.selectNotificationList(notification);
    }

    @Override
    public List<ImSystemNotification> getUserNotifications(Long userId, String type) {
        return notificationMapper.getUserNotifications(userId, type);
    }

    @Override
    public ImSystemNotification getNotificationById(Long id) {
        return notificationMapper.selectNotificationById(id);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return notificationMapper.getUnreadCount(userId);
    }

    @Override
    public Long sendNotification(Long receiverId, String type, String title, String content, Long relatedId, String relatedType) {
        ImSystemNotification notification = new ImSystemNotification();
        notification.setReceiverId(receiverId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setRelatedType(relatedType);
        notificationMapper.insertNotification(notification);
        return notification.getId();
    }

    @Override
    public void batchSendNotification(List<Long> receiverIds, String type, String title, String content) {
        for (Long receiverId : receiverIds) {
            sendNotification(receiverId, type, title, content, null, null);
        }
    }

    @Override
    public void markAsRead(Long id, Long userId) {
        notificationMapper.markAsRead(id);
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
    }

    @Override
    public void deleteNotification(Long id, Long userId) {
        notificationMapper.deleteNotificationById(id);
    }

    @Override
    public void clearAllNotifications(Long userId) {
        notificationMapper.clearUserNotifications(userId);
    }
}
