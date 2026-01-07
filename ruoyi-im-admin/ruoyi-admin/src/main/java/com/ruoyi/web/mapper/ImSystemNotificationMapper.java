package com.ruoyi.web.mapper;

import com.ruoyi.im.domain.ImSystemNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * IM系统通知Mapper接口（Admin模块专用）
 */
@Mapper
public interface ImSystemNotificationMapper {
    
    List<ImSystemNotification> selectNotificationList(ImSystemNotification notification);
    
    List<ImSystemNotification> getUserNotifications(@Param("userId") Long userId, @Param("type") String type);
    
    ImSystemNotification selectNotificationById(Long id);
    
    int getUnreadCount(Long userId);
    
    int insertNotification(ImSystemNotification notification);
    
    int markAsRead(Long id);
    
    int markAllAsRead(Long userId);
    
    int deleteNotificationById(Long id);
    
    int clearUserNotifications(Long userId);
}
