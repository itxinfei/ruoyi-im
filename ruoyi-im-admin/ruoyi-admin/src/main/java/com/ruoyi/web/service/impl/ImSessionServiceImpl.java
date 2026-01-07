package com.ruoyi.web.service.impl;

import com.ruoyi.im.domain.ImSession;
import com.ruoyi.web.mapper.ImSessionMapper;
import com.ruoyi.web.service.ImSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * IM会话Service实现（Admin模块专用）
 */
@Service
public class ImSessionServiceImpl implements ImSessionService {

    @Autowired
    private ImSessionMapper sessionMapper;

    @Override
    public List<ImSession> selectImSessionList(ImSession imSession) {
        return sessionMapper.selectImSessionList(imSession);
    }

    @Override
    public ImSession selectImSessionById(Long id) {
        return sessionMapper.selectImSessionById(id);
    }

    @Override
    public int insertImSession(ImSession imSession) {
        return sessionMapper.insertImSession(imSession);
    }

    @Override
    public int updateImSession(ImSession imSession) {
        return sessionMapper.updateImSession(imSession);
    }

    @Override
    public int deleteImSessionById(Long id) {
        return sessionMapper.deleteImSessionById(id);
    }

    @Override
    public int deleteImSessionByIds(Long[] ids) {
        return sessionMapper.deleteImSessionByIds(ids);
    }

    @Override
    public int countOnlineSessions() {
        return sessionMapper.countOnlineSessions();
    }

    @Override
    public List<ImSession> selectUserActiveSessions(Long userId) {
        return sessionMapper.selectUserActiveSessions(userId);
    }

    @Override
    public int kickOutSession(Long sessionId) {
        return sessionMapper.kickOutSession(sessionId);
    }
}
