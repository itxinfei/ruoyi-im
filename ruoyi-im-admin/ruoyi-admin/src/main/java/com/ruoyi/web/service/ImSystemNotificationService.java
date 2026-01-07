package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImSystemNotification;
import java.util.List;

/**
 * IM系统通知Service接口（Admin模块专用）
 */
public interface ImSystemNotificationService {
    
    List<ImSystemNotification> selectNotificationList(ImSystemNotification notification);
    
    List<ImSystemNotification> getUserNotifications(Long userId, String type);
    
    ImSystemNotification getNotificationById(Long id);
    
    int getUnreadCount(Long userId);
    
    Long sendNotification(Long receiverId, String type, String title, String content, Long relatedId, String relatedType);
    
    void batchSendNotification(List<Long> receiverIds, String type, String title, String content);
    
    void markAsRead(Long id, Long userId);
    
    void markAllAsRead(Long userId);
    
    void deleteNotification(Long id, Long userId);
    
    void clearAllNotifications(Long userId);
}
