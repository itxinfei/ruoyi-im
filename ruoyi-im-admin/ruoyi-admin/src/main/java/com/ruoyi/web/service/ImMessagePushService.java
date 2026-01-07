package com.ruoyi.web.service;

import java.util.Set;

/**
 * IM消息推送Service接口（Admin模块专用）
 */
public interface ImMessagePushService {
    
    int getOnlineUserCount();
    
    Set<Long> getOnlineUserIds();
    
    boolean isUserOnline(Long userId);
    
    void disconnectUser(Long userId);
    
    void sendMessageToUser(Long userId, String message);
    
    void broadcastMessage(String message);
}
