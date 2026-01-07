package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImSystemNotification;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImSystemNotificationMapper;
import com.ruoyi.im.service.ImSystemNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统通知服务实现
 *
 * @author ruoyi
 */
@Service
public class ImSystemNotificationServiceImpl implements ImSystemNotificationService {

    @Autowired
    private ImSystemNotificationMapper notificationMapper;

    @Override
    public Long sendNotification(Long receiverId, String type, String title, String content, Long relatedId, String relatedType) {
        ImSystemNotification notification = new ImSystemNotification();
        notification.setReceiverId(receiverId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setRelatedType(relatedType);
        notification.setIsRead(false);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insertImSystemNotification(notification);
        return notification.getId();
    }

    @Override
    public void batchSendNotification(List<Long> receiverIds, String type, String title, String content) {
        if (receiverIds == null || receiverIds.isEmpty()) {
            return;
        }
        List<ImSystemNotification> notifications = new ArrayList<>();
        for (Long receiverId : receiverIds) {
            ImSystemNotification notification = new ImSystemNotification();
            notification.setReceiverId(receiverId);
            notification.setType(type);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setIsRead(false);
            notification.setCreateTime(LocalDateTime.now());
            notifications.add(notification);
        }
        notificationMapper.batchInsertNotifications(notifications);
    }

    @Override
    public List<ImSystemNotification> getUserNotifications(Long userId, String type) {
        if (type != null && !type.isEmpty()) {
            return notificationMapper.selectNotificationsByType(userId, type);
        }
        return notificationMapper.selectNotificationsByReceiverId(userId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return notificationMapper.countUnreadByReceiverId(userId);
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        ImSystemNotification notification = notificationMapper.selectImSystemNotificationById(notificationId);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        if (!notification.getReceiverId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }
        notificationMapper.markAsRead(notificationId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
    }

    @Override
    public void deleteNotification(Long notificationId, Long userId) {
        ImSystemNotification notification = notificationMapper.selectImSystemNotificationById(notificationId);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        if (!notification.getReceiverId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }
        notificationMapper.deleteImSystemNotificationById(notificationId);
    }

    @Override
    public ImSystemNotification getNotificationById(Long notificationId) {
        return notificationMapper.selectImSystemNotificationById(notificationId);
    }

    @Override
    public void clearAllNotifications(Long userId) {
        notificationMapper.deleteAllByReceiverId(userId);
    }

    @Override
    public List<ImSystemNotification> selectNotificationList(ImSystemNotification notification) {
        return notificationMapper.selectImSystemNotificationList(notification);
    }

    @Override
    public int insertNotification(ImSystemNotification notification) {
        notification.setCreateTime(LocalDateTime.now());
        return notificationMapper.insertImSystemNotification(notification);
    }

    @Override
    public int updateNotification(ImSystemNotification notification) {
        return notificationMapper.updateImSystemNotification(notification);
    }

    @Override
    public int deleteNotificationByIds(Long[] ids) {
        return notificationMapper.deleteImSystemNotificationByIds(ids);
    }
}