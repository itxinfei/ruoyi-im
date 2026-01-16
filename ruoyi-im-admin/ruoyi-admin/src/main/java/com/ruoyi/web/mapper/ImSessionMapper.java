package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImSession;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * IM会话Mapper接口（Admin模块专用）
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Mapper
public interface ImSessionMapper {

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
     * 查询总会话数
     */
    int countTotalSessions();

    /**
     * 查询私聊会话数
     */
    int countPrivateSessions();

    /**
     * 查询群聊会话数
     */
    int countGroupSessions();

    /**
     * 查询今日活跃会话数
     */
    int countTodayActiveSessions();
}
