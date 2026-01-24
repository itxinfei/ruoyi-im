package com.ruoyi.web.service.impl;

import com.ruoyi.web.mapper.ImMessagePushMapper;
import com.ruoyi.web.service.ImMessagePushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Set;

/**
 * IM消息推送Service实现（Admin模块专用）
 */
@Service
public class ImMessagePushServiceImpl implements ImMessagePushService {

    @Autowired
    private ImMessagePushMapper messagePushMapper;

    @Override
    public int getOnlineUserCount() {
        return messagePushMapper.getOnlineUserCount();
    }

    @Override
    public Set<Long> getOnlineUserIds() {
        return messagePushMapper.getOnlineUserIds();
    }

    @Override
    public boolean isUserOnline(Long userId) {
        return messagePushMapper.isUserOnline(userId);
    }

    @Override
    public void disconnectUser(Long userId) {
        messagePushMapper.disconnectUser(userId);
    }

    @Override
    public void sendMessageToUser(Long userId, String message) {
        messagePushMapper.sendMessageToUser(userId, message);
    }

    @Override
    public void broadcastMessage(String message) {
        messagePushMapper.broadcastMessage(message);
    }
}
