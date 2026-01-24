package com.ruoyi.web.service.impl;

import com.ruoyi.web.mapper.ImStatisticsMapper;
import com.ruoyi.web.service.ImDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * IM系统仪表板服务实现
 * 提供Dashboard首页数据可视化相关业务逻辑（无缓存版本）
 *
 * @author ruoyi
 */
@Service
public class ImDashboardServiceImpl implements ImDashboardService {

    @Autowired
    private ImStatisticsMapper statisticsMapper;

    /**
     * 计算开始时间
     *
     * @param days 天数
     * @return 格式化的开始时间字符串
     */
    private String calculateStartTime(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
    }

    /**
     * 根据天数决定日期格式
     *
     * @param days 天数
     * @return 日期格式字符串
     */
    private String getDateFormat(int days) {
        if (days <= 7) {
            return "%Y-%m-%d %H:%i"; // 按小时
        } else if (days <= 90) {
            return "%Y-%m-%d"; // 按天
        } else {
            return "%Y-%m"; // 按月
        }
    }

    @Override
    public Map<String, Object> getBasicStatistics() {
        // 直接查询数据库，不使用缓存
        return statisticsMapper.getSystemStatistics();
    }

    @Override
    public List<Map<String, Object>> getUserRegistrationTrend(int days) {
        // 直接查询数据库，不使用缓存
        String startTime = calculateStartTime(days);
        String dateFormat = getDateFormat(days);
        return statisticsMapper.getUserRegistrationTrend(startTime, dateFormat);
    }

    @Override
    public List<Map<String, Object>> getMessageSendingTrend(int days) {
        // 直接查询数据库，不使用缓存
        String startTime = calculateStartTime(days);
        String dateFormat = getDateFormat(days);
        return statisticsMapper.getMessageSendingTrend(startTime, dateFormat);
    }

    @Override
    public List<Map<String, Object>> getGroupCreationTrend(int days) {
        // 直接查询数据库，不使用缓存
        String startTime = calculateStartTime(days);
        String dateFormat = getDateFormat(days);
        return statisticsMapper.getGroupCreationTrend(startTime, dateFormat);
    }

    @Override
    public List<Map<String, Object>> getMessageTypeDistribution(Integer days) {
        // 直接查询数据库，不使用缓存
        String startTime = null;
        if (days != null && days > 0) {
            startTime = calculateStartTime(days);
        }
        return statisticsMapper.getMessageTypeDistribution(startTime);
    }

    @Override
    public List<Map<String, Object>> getFileTypeDistribution(Integer days) {
        // 直接查询数据库，不使用缓存
        String startTime = null;
        if (days != null && days > 0) {
            startTime = calculateStartTime(days);
        }
        return statisticsMapper.getFileTypeDistribution(startTime);
    }

    @Override
    public List<Map<String, Object>> getUserActivityDistribution(int days) {
        // 直接查询数据库，不使用缓存
        String startTime = calculateStartTime(days);
        return statisticsMapper.getUserActivityDistribution(startTime);
    }

    @Override
    public Map<String, Object> getWeeklyComparison() {
        // 直接查询数据库，不使用缓存
        List<Map<String, Object>> comparisonList = statisticsMapper.getWeeklyComparison();
        Map<String, Object> result = new HashMap<>();
        for (Map<String, Object> item : comparisonList) {
            String period = (String) item.get("period_label");
            Long count = (Long) item.get("count");
            result.put(period, count);
        }
        return result;
    }

    @Override
    public Map<String, Object> getMonthlyComparison() {
        // 直接查询数据库，不使用缓存
        List<Map<String, Object>> comparisonList = statisticsMapper.getMonthlyComparison();
        Map<String, Object> result = new HashMap<>();
        for (Map<String, Object> item : comparisonList) {
            String period = (String) item.get("period_label");
            Long count = (Long) item.get("count");
            result.put(period, count);
        }
        return result;
    }

    @Override
    public Map<String, Object> getHourlyComparison() {
        // 直接查询数据库，不使用缓存
        List<Map<String, Object>> comparisonList = statisticsMapper.getHourlyComparison();
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Long> todayData = new LinkedHashMap<>();
        Map<String, Long> yesterdayData = new LinkedHashMap<>();

        // 初始化24小时数据
        for (int i = 0; i < 24; i++) {
            String hour = String.format("%02d", i);
            todayData.put(hour, 0L);
            yesterdayData.put(hour, 0L);
        }

        // 填充数据
        for (Map<String, Object> item : comparisonList) {
            String hour = String.format("%02d", item.get("hour_label"));
            String period = (String) item.get("period_label");
            Long count = (Long) item.get("count");

            if ("今天".equals(period)) {
                todayData.put(hour, count);
            } else if ("昨天".equals(period)) {
                yesterdayData.put(hour, count);
            }
        }

        result.put("today", todayData);
        result.put("yesterday", yesterdayData);
        return result;
    }

    @Override
    public List<Map<String, Object>> getTopActiveUsers(int days, int limit) {
        // 直接查询数据库，不使用缓存
        String startTime = calculateStartTime(days);
        return statisticsMapper.getTopActiveUsers(startTime, limit);
    }

    @Override
    public List<Map<String, Object>> getTopActiveGroups(int days, int limit) {
        // 直接查询数据库，不使用缓存
        String startTime = calculateStartTime(days);
        return statisticsMapper.getTopActiveGroups(startTime, limit);
    }

    @Override
    public List<Map<String, Object>> getTopMessageSenders(int days, int limit) {
        // 直接查询数据库，不使用缓存
        String startTime = calculateStartTime(days);
        return statisticsMapper.getTopMessageSenders(startTime, limit);
    }

    @Override
    public Long getRealtimeOnlineCount() {
        // 直接查询数据库，不使用缓存
        return statisticsMapper.getRealtimeOnlineCount();
    }

    @Override
    public List<Map<String, Object>> getRealtimeMessageFlow(int minutes) {
        // 实时数据直接查询数据库
        return statisticsMapper.getRealtimeMessageFlow(minutes);
    }

    @Override
    public List<Map<String, Object>> getHourlyActivityTrend(int days) {
        // 直接查询数据库，不使用缓存
        String startTime = calculateStartTime(days);
        return statisticsMapper.getHourlyActivityTrend(startTime);
    }

    @Override
    public List<Map<String, Object>> getDailyActiveUsers(int days) {
        // 直接查询数据库，不使用缓存
        String startTime = calculateStartTime(days);
        String dateFormat = "%Y-%m-%d";
        return statisticsMapper.getDailyActiveUsers(startTime, dateFormat);
    }

    @Override
    public List<Map<String, Object>> getConversationTypeDistribution() {
        // 直接查询数据库，不使用缓存
        return statisticsMapper.getConversationTypeDistribution();
    }
}
