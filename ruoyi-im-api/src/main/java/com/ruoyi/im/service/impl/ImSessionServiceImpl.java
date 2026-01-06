package com.ruoyi.im.service.impl;

import com.ruoyi.im.dto.session.ImSessionUpdateRequest;
import com.ruoyi.im.service.ImSessionService;
import com.ruoyi.im.vo.session.ImSessionVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 会话服务实现
 *
 * @author ruoyi
 */
@Service
public class ImSessionServiceImpl implements ImSessionService {

    @Override
    public List<ImSessionVO> getSessionList(Long userId) {
        // TODO: 从数据库查询会话列表
        List<ImSessionVO> list = new ArrayList<>();

        // 模拟数据
        ImSessionVO vo1 = new ImSessionVO();
        vo1.setId(1L);
        vo1.setType("private");
        vo1.setName("张三");
        vo1.setAvatar("/avatar/user1.png");
        vo1.setPeerId(2L);
        vo1.setUnreadCount(3);
        vo1.setIsPinned(1);
        vo1.setIsMuted(0);
        vo1.setLastMessage("你好，在吗？");
        vo1.setLastMessageTime(LocalDateTime.now());
        vo1.setLastMessageType("text");
        vo1.setOnline(true);
        list.add(vo1);

        return list;
    }

    @Override
    public ImSessionVO getSessionById(Long sessionId) {
        // TODO: 从数据库查询会话
        ImSessionVO vo = new ImSessionVO();
        vo.setId(sessionId);
        vo.setType("private");
        vo.setName("张三");
        vo.setAvatar("/avatar/user1.png");
        vo.setPeerId(2L);
        vo.setUnreadCount(0);
        vo.setIsPinned(0);
        vo.setIsMuted(0);
        return vo;
    }

    @Override
    public void updateSession(Long sessionId, ImSessionUpdateRequest request) {
        // TODO: 更新会话信息
    }

    @Override
    public void deleteSession(Long sessionId, Long userId) {
        // TODO: 删除会话（逻辑删除）
    }

    @Override
    public void clearUnread(Long sessionId, Long userId) {
        // TODO: 清空未读消息数
    }

    @Override
    public void togglePin(Long sessionId, Integer pinned) {
        // TODO: 更新置顶状态
    }

    @Override
    public void toggleMute(Long sessionId, Integer muted) {
        // TODO: 更新免打扰状态
    }
}
