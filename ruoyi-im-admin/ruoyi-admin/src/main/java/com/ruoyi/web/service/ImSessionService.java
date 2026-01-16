package com.ruoyi.web.service;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.domain.ImSession;
import java.util.List;

/**
 * IM会话Service接口（Admin模块专用）
 */
public interface ImSessionService {

    List<ImSession> selectImSessionList(ImSession imSession);

    ImSession selectImSessionById(Long id);

    int insertImSession(ImSession imSession);

    int updateImSession(ImSession imSession);

    int deleteImSessionById(Long id);

    int deleteImSessionByIds(Long[] ids);

    int countOnlineSessions();

    List<ImSession> selectUserActiveSessions(Long userId);

    int kickOutSession(Long sessionId);

    /**
     * 获取会话统计信息
     */
    AjaxResult getStatistics();
}
