package com.ruoyi.web.service.impl;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.domain.ImSession;
import com.ruoyi.web.mapper.ImSessionMapper;
import com.ruoyi.web.service.ImSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public AjaxResult getStatistics() {
        Map<String, Object> data = new HashMap<>();

        // 总会话数
        int totalCount = sessionMapper.countTotalSessions();
        data.put("totalCount", totalCount);

        // 私聊会话数
        int privateCount = sessionMapper.countPrivateSessions();
        data.put("privateCount", privateCount);

        // 群聊会话数
        int groupCount = sessionMapper.countGroupSessions();
        data.put("groupCount", groupCount);

        // 今日活跃会话数
        int activeCount = sessionMapper.countTodayActiveSessions();
        data.put("activeCount", activeCount);

        return AjaxResult.success(data);
    }
}
