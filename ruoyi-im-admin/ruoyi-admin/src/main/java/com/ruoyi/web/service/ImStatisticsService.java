package com.ruoyi.web.service;

import java.util.Map;

/**
 * IM统计Service接口（Admin模块专用）
 */
public interface ImStatisticsService {
    
    Map<String, Object> getSystemStatistics();
    
    Map<String, Object> getUserStatistics(Long userId, String startTime, String endTime);
    
    Map<String, Object> getGroupStatistics(Long groupId, String startTime, String endTime);
    
    Map<String, Object> getMessageStatistics(Long conversationId, String startTime, String endTime);
    
    Map<String, Object> getFileStatistics();
    
    Map<String, Object> getActiveRanking(Integer days);
    
    Map<String, Object> getOnlineStatistics();
}
