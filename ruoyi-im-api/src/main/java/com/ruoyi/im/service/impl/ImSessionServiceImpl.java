package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImSession;
import com.ruoyi.im.dto.session.ImSessionUpdateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImSessionMapper;
import com.ruoyi.im.service.ImSessionService;
import com.ruoyi.im.vo.session.ImSessionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 会话服务实现（已废弃）
 *
 * 该实现已被 ImConversationMemberServiceImpl 替代
 * 请使用 ImConversationMemberServiceImpl 进行会话成员管理
 *
 * @deprecated 使用 {@link ImConversationMemberServiceImpl} 替代
 * @author ruoyi
 */
@Deprecated
@Service
public class ImSessionServiceImpl implements ImSessionService {

    @Autowired
    private ImSessionMapper imSessionMapper;

    @Override
    public List<ImSessionVO> getSessionList(Long userId) {
        List<ImSessionVO> voList = new ArrayList<>();
        List<ImSession> sessionList = imSessionMapper.selectSessionsByUserId(userId);
        for (ImSession session : sessionList) {
            ImSessionVO vo = new ImSessionVO();
            BeanUtils.copyProperties(session, vo);
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public ImSessionVO getSessionById(Long sessionId) {
        ImSession session = imSessionMapper.selectImSessionById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        ImSessionVO vo = new ImSessionVO();
        BeanUtils.copyProperties(session, vo);
        return vo;
    }

    @Override
    public void updateSession(Long sessionId, ImSessionUpdateRequest request) {
        ImSession session = imSessionMapper.selectImSessionById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        if (request.getName() != null) {
            session.setName(request.getName());
        }
        if (request.getIsPinned() != null) {
            session.setIsPinned(request.getIsPinned());
        }
        if (request.getIsMuted() != null) {
            session.setIsMuted(request.getIsMuted());
        }
        imSessionMapper.updateImSession(session);
    }

    @Override
    public void deleteSession(Long sessionId, Long userId) {
        ImSession session = imSessionMapper.selectImSessionById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该会话");
        }
        imSessionMapper.deleteImSessionById(sessionId);
    }

    @Override
    public void clearUnread(Long sessionId, Long userId) {
        ImSession session = imSessionMapper.selectImSessionById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该会话");
        }
        session.setUnreadCount(0);
        imSessionMapper.updateImSession(session);
    }

    @Override
    public void togglePin(Long sessionId, Integer pinned) {
        ImSession session = imSessionMapper.selectImSessionById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        session.setIsPinned(pinned);
        imSessionMapper.updateImSession(session);
    }

    @Override
    public void toggleMute(Long sessionId, Integer muted) {
        ImSession session = imSessionMapper.selectImSessionById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        session.setIsMuted(muted);
        imSessionMapper.updateImSession(session);
    }
}
