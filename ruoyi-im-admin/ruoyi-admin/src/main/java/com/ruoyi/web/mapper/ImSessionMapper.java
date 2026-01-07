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
}
