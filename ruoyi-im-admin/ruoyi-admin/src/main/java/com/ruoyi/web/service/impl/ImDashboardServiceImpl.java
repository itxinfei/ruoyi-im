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
 * 提供Dashboard首页数据可视化相关业务逻辑（带缓存）
 *
 * @author ruoyi
 */
@Service
public class ImDashboardServiceImpl implements ImDashboardService {

    @Autowired
    private ImStatisticsMapper statisticsMapper;

    @Autowired
    private ImDashboardCacheService cacheService;

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
        // 先从缓存获取
        Map<String, Object> cached = cacheService.getBasicStatistics();
        if (cached != null) {
            return cached;
        }
        // 缓存未命中，查询数据库
        Map<String, Object> data = statisticsMapper.getSystemStatistics();
        // 写入缓存
        cacheService.setBasicStatistics(data);
        return data;
    }

    @Override
    public List<Map<String, Object>> getUserRegistrationTrend(int days) {
        // 先从缓存获取
        List<Map<String, Object>> cached = cacheService.getUserRegistrationTrend(days);
        if (cached != null) {
            return cached;
        }
        // 缓存未命中，查询数据库
        String startTime = calculateStartTime(days);
        String dateFormat = getDateFormat(days);
        List<Map<String, Object>> data = statisticsMapper.getUserRegistrationTrend(startTime, dateFormat);
        // 写入缓存
        cacheService.setUserRegistrationTrend(days, data);
        return data;
    }

    @Override
    public List<Map<String, Object>> getMessageSendingTrend(int days) {
        // 先从缓存获取
        List<Map<String, Object>> cached = cacheService.getMessageSendingTrend(days);
        if (cached != null) {
            return cached;
        }
        // 缓存未命中，查询数据库
        String startTime = calculateStartTime(days);
        String dateFormat = getDateFormat(days);
        List<Map<String, Object>> data = statisticsMapper.getMessageSendingTrend(startTime, dateFormat);
        // 写入缓存
        cacheService.setMessageSendingTrend(days, data);
        return data;
    }

    @Override
    public List<Map<String, Object>> getGroupCreationTrend(int days) {
        // 先从缓存获取
        List<Map<String, Object>> cached = cacheService.getGroupCreationTrend(days);
        if (cached != null) {
            return cached;
        }
        // 缓存未命中，查询数据库
        String startTime = calculateStartTime(days);
        String dateFormat = getDateFormat(days);
        List<Map<String, Object>> data = statisticsMapper.getGroupCreationTrend(startTime, dateFormat);
        // 写入缓存
        cacheService.setGroupCreationTrend(days, data);
        return data;
    }

    @Override
    public List<Map<String, Object>> getMessageTypeDistribution(Integer days) {
        // 如果指定了天数，不使用缓存
        if (days != null && days > 0) {
            String startTime = calculateStartTime(days);
            return statisticsMapper.getMessageTypeDistribution(startTime);
        }
        // 未指定天数，使用缓存
        List<Map<String, Object>> cached = cacheService.getMessageTypeDistribution();
        if (cached != null) {
            return cached;
        }
        // 缓存未命中，查询数据库
        List<Map<String, Object>> data = statisticsMapper.getMessageTypeDistribution(null);
        // 写入缓存
        cacheService.setMessageTypeDistribution(data);
        return data;
    }

    @Override
    public List<Map<String, Object>> getFileTypeDistribution(Integer days) {
        // 如果指定了天数，不使用缓存
        if (days != null && days > 0) {
            String startTime = calculateStartTime(days);
            return statisticsMapper.getFileTypeDistribution(startTime);
        }
        // 未指定天数，使用缓存
        List<Map<String, Object>> cached = cacheService.getFileTypeDistribution();
        if (cached != null) {
            return cached;
        }
        // 缓存未命中，查询数据库
        List<Map<String, Object>> data = statisticsMapper.getFileTypeDistribution(null);
        // 写入缓存
        cacheService.setFileTypeDistribution(data);
        return data;
    }

    @Override
    public List<Map<String, Object>> getUserActivityDistribution(int days) {
        // 先从缓存获取
        List<Map<String, Object>> cached = cacheService.getUserActivityDistribution(days);
        if (cached != null) {
            return cached;
        }
        // 缓存未命中，查询数据库
        String startTime = calculateStartTime(days);
        List<Map<String, Object>> data = statisticsMapper.getUserActivityDistribution(startTime);
        // 写入缓存
        cacheService.setUserActivityDistribution(days, data);
        return data;
    }

    @Override
    public Map<String, Object> getWeeklyComparison() {
        // 先从缓存获取
        Map<String, Object> cached = cacheService.getWeeklyComparison();
        if (cached != null) {
            return cached;
        }
        // 缓存未命中，查询数据库
        List<Map<String, Object>> comparisonList = statisticsMapper.getWeeklyComparison();
        Map<String, Object> result = new HashMap<>();
        for (Map<String, Object> item : comparisonList) {
            String period = (String) item.get("period_label");
            Long count = (Long) item.get("count");
            result.put(period, count);
        }
        // 写入缓存
        cacheService.setWeeklyComparison(result);
        return result;
    }

    @Override
    public Map<String, Object> getMonthlyComparison() {
        // 先从缓存获取
        Map<String, Object> cached = cacheService.getMonthlyComparison();
        if (cached != null) {
            return cached;
        }
        // 缓存未命中，查询数据库
        List<Map<String, Object>> comparisonList = statisticsMapper.getMonthlyComparison();
        Map<String, Object> result = new HashMap<>();
        for (Map<String, Object> item : comparisonList) {
            String period = (String) item.get("period_label");
            Long count = (Long) item.get("count");
            result.put(period, count);
        }
        // 写入缓存
        cacheService.setMonthlyComparison(result);
        return result;
    }

    @Override
    public Map<String, Object> getHourlyComparison() {
        // 先从缓存获取
        Map<String, Object> cached = cacheService.getHourlyComparison();
        if (cached != null) {
            return cached;
        }
        // 缓存未命中，查询数据库
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
        // 写入缓存
        cacheService.setHourlyComparison(result);
        return result;
    }

    @Override
    public List<Map<String, Object>> getTopActiveUsers(int days, int limit) {
        // 先从缓存获取
        List<Map<String, Object>> cached = cacheService.getTopActiveUsers(days, limit);
        if (cached != null) {
            return cached;
        }
        // 缓存未命中，查询数据库
        String startTime = calculateStartTime(days);
        List<Map<String, Object>> data = statisticsMapper.getTopActiveUsers(startTime, limit);
        // 写入缓存
        cacheService.setTopActiveUsers(days, limit, data);
        return data;
    }

    @Override
    public List<Map<String, Object>> getTopActiveGroups(int days, int limit) {
        // 先从缓存获取
        List<Map<String, Object>> cached = cacheService.getTopActiveGroups(days, limit);
        if (cached != null) {
            return cached;
        }
        // 缓存未命中，查询数据库
        String startTime = calculateStartTime(days);
        List<Map<String, Object>> data = statisticsMapper.getTopActiveGroups(startTime, limit);
        // 写入缓存
        cacheService.setTopActiveGroups(days, limit, data);
        return data;
    }

    @Override
    public List<Map<String, Object>> getTopMessageSenders(int days, int limit) {
        // 先从缓存获取
        List<Map<String, Object>> cached = cacheService.getTopMessageSenders(days, limit);
        if (cached != null) {
            return cached;
        }
        // 缓存未命中，查询数据库
        String startTime = calculateStartTime(days);
        List<Map<String, Object>> data = statisticsMapper.getTopMessageSenders(startTime, limit);
        // 写入缓存
        cacheService.setTopMessageSenders(days, limit, data);
        return data;
    }

    @Override
    public Long getRealtimeOnlineCount() {
        // 先从缓存获取
        Long cached = cacheService.getRealtimeOnlineCount();
        if (cached != null) {
            return cached;
        }
        // 缓存未命中，查询数据库
        Long count = statisticsMapper.getRealtimeOnlineCount();
        // 写入缓存
        cacheService.setRealtimeOnlineCount(count);
        return count;
    }

    @Override
    public List<Map<String, Object>> getRealtimeMessageFlow(int minutes) {
        // 实时数据不使用缓存，直接查询数据库
        return statisticsMapper.getRealtimeMessageFlow(minutes);
    }

    @Override
    public List<Map<String, Object>> getHourlyActivityTrend(int days) {
        // 先从缓存获取
        List<Map<String, Object>> cached = cacheService.getHourlyActivityTrend(days);
        if (cached != null) {
            return cached;
        }
        // 缓存未命中，查询数据库
        String startTime = calculateStartTime(days);
        List<Map<String, Object>> data = statisticsMapper.getHourlyActivityTrend(startTime);
        // 写入缓存
        cacheService.setHourlyActivityTrend(days, data);
        return data;
    }

    @Override
    public List<Map<String, Object>> getDailyActiveUsers(int days) {
        // 先从缓存获取
        List<Map<String, Object>> cached = cacheService.getDailyActiveUsers(days);
        if (cached != null) {
            return cached;
        }
        // 缓存未命中，查询数据库
        String startTime = calculateStartTime(days);
        String dateFormat = "%Y-%m-%d";
        List<Map<String, Object>> data = statisticsMapper.getDailyActiveUsers(startTime, dateFormat);
        // 写入缓存
        cacheService.setDailyActiveUsers(days, data);
        return data;
    }

    @Override
    public List<Map<String, Object>> getConversationTypeDistribution() {
        // 先从缓存获取
        List<Map<String, Object>> cached = cacheService.getConversationTypeDistribution();
        if (cached != null) {
            return cached;
        }
        // 缓存未命中，查询数据库
        List<Map<String, Object>> data = statisticsMapper.getConversationTypeDistribution();
        // 写入缓存
        cacheService.setConversationTypeDistribution(data);
        return data;
    }
}
