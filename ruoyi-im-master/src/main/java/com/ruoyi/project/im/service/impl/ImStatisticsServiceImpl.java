package com.ruoyi.web.service.impl;

import com.ruoyi.web.mapper.ImStatisticsMapper;
import com.ruoyi.web.service.ImStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * IM统计Service实现（Admin模块专用）
 */
@Service
public class ImStatisticsServiceImpl implements ImStatisticsService {

    @Autowired
    private ImStatisticsMapper statisticsMapper;

    @Override
    public Map<String, Object> getSystemStatistics() {
        return statisticsMapper.getSystemStatistics();
    }

    @Override
    public Map<String, Object> getUserStatistics(Long userId, String startTime, String endTime) {
        return statisticsMapper.getUserStatistics(userId, startTime, endTime);
    }

    @Override
    public Map<String, Object> getGroupStatistics(Long groupId, String startTime, String endTime) {
        return statisticsMapper.getGroupStatistics(groupId, startTime, endTime);
    }

    @Override
    public Map<String, Object> getMessageStatistics(Long conversationId, String startTime, String endTime) {
        return statisticsMapper.getMessageStatistics(conversationId, startTime, endTime);
    }

    @Override
    public Map<String, Object> getFileStatistics() {
        return statisticsMapper.getFileStatistics();
    }

    @Override
    public Map<String, Object> getActiveRanking(Integer days) {
        return statisticsMapper.getActiveRanking(days);
    }

    @Override
    public Map<String, Object> getOnlineStatistics() {
        return statisticsMapper.getOnlineStatistics();
    }
}
