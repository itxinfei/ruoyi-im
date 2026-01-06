package com.ruoyi.im.service.impl;

import com.ruoyi.im.dto.message.ImMessageSendRequest;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.vo.message.ImMessageVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息服务实现
 *
 * @author ruoyi
 */
@Service
public class ImMessageServiceImpl implements ImMessageService {

    @Override
    public Long sendMessage(ImMessageSendRequest request, Long userId) {
        // TODO: 实现发送消息逻辑
        // 1. 保存消息到数据库
        // 2. 通过 WebSocket 推送消息
        // 3. 更新会话最后消息信息
        return System.currentTimeMillis();
    }

    @Override
    public List<ImMessageVO> getMessages(Long sessionId, Long userId, Long lastId, Integer limit) {
        // TODO: 从数据库查询消息列表
        List<ImMessageVO> list = new ArrayList<>();

        // 模拟数据
        ImMessageVO vo1 = new ImMessageVO();
        vo1.setId(1L);
        vo1.setSessionId(sessionId);
        vo1.setSenderId(2L);
        vo1.setSenderName("张三");
        vo1.setSenderAvatar("/avatar/user1.png");
        vo1.setType("text");
        vo1.setContent("你好，在吗？");
        vo1.setStatus(3);
        vo1.setIsRevoked(0);
        vo1.setSendTime(LocalDateTime.now());
        vo1.setIsSelf(false);
        list.add(vo1);

        ImMessageVO vo2 = new ImMessageVO();
        vo2.setId(2L);
        vo2.setSessionId(sessionId);
        vo2.setSenderId(userId);
        vo2.setSenderName("我");
        vo2.setSenderAvatar("/avatar/me.png");
        vo2.setType("text");
        vo2.setContent("在的，有什么事吗？");
        vo2.setStatus(3);
        vo2.setIsRevoked(0);
        vo2.setSendTime(LocalDateTime.now());
        vo2.setIsSelf(true);
        list.add(vo2);

        return list;
    }

    @Override
    public void recallMessage(Long messageId, Long userId) {
        // TODO: 实现撤回消息逻辑
        // 1. 检查消息是否属于当前用户
        // 2. 检查消息是否超过撤回时间限制（如2分钟）
        // 3. 更新消息状态为已撤回
        // 4. 通过 WebSocket 推送撤回通知
    }

    @Override
    public void markAsRead(Long sessionId, Long userId, List<Long> messageIds) {
        // TODO: 实现标记已读逻辑
        // 1. 批量更新消息状态
        // 2. 更新会话未读数
        // 3. 通过 WebSocket 推送已读回执
    }
}
