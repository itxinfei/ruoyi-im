package com.ruoyi.web.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Map;

/**
 * IM统计Mapper接口（Admin模块专用）
 */
@Mapper
public interface ImStatisticsMapper {
    
    Map<String, Object> getSystemStatistics();
    
    Map<String, Object> getUserStatistics(@Param("userId") Long userId, @Param("startTime") String startTime, @Param("endTime") String endTime);
    
    Map<String, Object> getGroupStatistics(@Param("groupId") Long groupId, @Param("startTime") String startTime, @Param("endTime") String endTime);
    
    Map<String, Object> getMessageStatistics(@Param("conversationId") Long conversationId, @Param("startTime") String startTime, @Param("endTime") String endTime);
    
    Map<String, Object> getFileStatistics();
    
    Map<String, Object> getActiveRanking(@Param("days") Integer days);
    
    Map<String, Object> getOnlineStatistics();
}
