package com.ruoyi.web.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Set;

/**
 * IM消息推送Mapper接口（Admin模块专用）
 */
@Mapper
public interface ImMessagePushMapper {
    
    int getOnlineUserCount();
    
    Set<Long> getOnlineUserIds();
    
    boolean isUserOnline(@Param("userId") Long userId);
    
    void disconnectUser(@Param("userId") Long userId);
    
    void sendMessageToUser(@Param("userId") Long userId, @Param("message") String message);
    
    void broadcastMessage(@Param("message") String message);
}
