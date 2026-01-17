package com.ruoyi.web.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * IM系统仪表板缓存服务
 * 专门管理Dashboard相关的Redis缓存，提供智能缓存策略
 *
 * @author ruoyi
 */
@Component
public class ImDashboardCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ==================== Redis Key常量 ====================

    // 基础统计缓存
    private static final String CACHE_KEY_BASIC_STATS = "im:dashboard:stats:basic";

    // 趋势数据缓存
    private static final String CACHE_KEY_PREFIX_TREND = "im:dashboard:trend:";
    private static final String CACHE_KEY_TREND_USER = CACHE_KEY_PREFIX_TREND + "user:";
    private static final String CACHE_KEY_TREND_MESSAGE = CACHE_KEY_PREFIX_TREND + "message:";
    private static final String CACHE_KEY_TREND_GROUP = CACHE_KEY_PREFIX_TREND + "group:";
    private static final String CACHE_KEY_TREND_HOURLY = CACHE_KEY_PREFIX_TREND + "hourly:";
    private static final String CACHE_KEY_TREND_DAILY_ACTIVE = CACHE_KEY_PREFIX_TREND + "daily_active:";

    // 分布数据缓存
    private static final String CACHE_KEY_DISTRIBUTION_MSG_TYPE = "im:dashboard:distribution:msgType";
    private static final String CACHE_KEY_DISTRIBUTION_FILE_TYPE = "im:dashboard:distribution:fileType";
    private static final String CACHE_KEY_DISTRIBUTION_USER_ACTIVITY = "im:dashboard:distribution:userActivity:";
    private static final String CACHE_KEY_DISTRIBUTION_CONVERSATION_TYPE = "im:dashboard:distribution:conversationType";

    // 对比数据缓存
    private static final String CACHE_KEY_COMPARISON_WEEKLY = "im:dashboard:comparison:weekly";
    private static final String CACHE_KEY_COMPARISON_MONTHLY = "im:dashboard:comparison:monthly";
    private static final String CACHE_KEY_COMPARISON_HOURLY = "im:dashboard:comparison:hourly";

    // 排行榜缓存
    private static final String CACHE_KEY_RANKING_USERS = "im:dashboard:ranking:users:";
    private static final String CACHE_KEY_RANKING_GROUPS = "im:dashboard:ranking:groups:";
    private static final String CACHE_KEY_RANKING_SENDERS = "im:dashboard:ranking:senders:";

    // 实时监控缓存
    private static final String CACHE_KEY_REALTIME_ONLINE = "im:dashboard:realtime:online";
    private static final String CACHE_KEY_REALTIME_MESSAGE_FLOW = "im:dashboard:realtime:messageFlow:";

    // ==================== TTL常量（秒） ====================

    private static final long TTL_BASIC_STATS = 60; // 基础统计：60秒
    private static final long TTL_TREND_SHORT = 300; // 趋势数据（短期）：5分钟
    private static final long TTL_TREND_LONG = 600; // 趋势数据（长期）：10分钟
    private static final long TTL_DISTRIBUTION = 600; // 分布数据：10分钟
    private static final long TTL_COMPARISON = 300; // 对比数据：5分钟
    private static final long TTL_RANKING = 600; // 排行榜：10分钟
    private static final long TTL_REALTIME = 30; // 实时数据：30秒

    // ==================== 基础缓存操作 ====================

    /**
     * 获取缓存数据
     *
     * @param key Redis键
     * @param <T> 返回类型
     * @return 缓存数据，不存在返回null
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    /**
     * 设置缓存数据（永久）
     *
     * @param key   Redis键
     * @param value 缓存值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存数据（带过期时间）
     *
     * @param key        Redis键
     * @param value      缓存值
     * @param timeout    过期时间
     * @param timeUnit   时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 删除缓存
     *
     * @param key Redis键
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 批量删除缓存（匹配前缀）
     *
     * @param pattern 键模式
     */
    public void deletePattern(String pattern) {
        redisTemplate.delete(redisTemplate.keys(pattern));
    }

    /**
     * 清空所有Dashboard缓存
     */
    public void clearAll() {
        deletePattern("im:dashboard:*");
    }

    // ==================== 基础统计缓存 ====================

    /**
     * 获取基础统计数据（缓存）
     */
    public Map<String, Object> getBasicStatistics() {
        return get(CACHE_KEY_BASIC_STATS);
    }

    /**
     * 设置基础统计数据（缓存）
     */
    public void setBasicStatistics(Map<String, Object> data) {
        set(CACHE_KEY_BASIC_STATS, data, TTL_BASIC_STATS, TimeUnit.SECONDS);
    }

    // ==================== 趋势数据缓存 ====================

    /**
     * 获取用户注册趋势（缓存）
     */
    public List<Map<String, Object>> getUserRegistrationTrend(int days) {
        return get(CACHE_KEY_TREND_USER + days);
    }

    /**
     * 设置用户注册趋势（缓存）
     */
    public void setUserRegistrationTrend(int days, List<Map<String, Object>> data) {
        set(CACHE_KEY_TREND_USER + days, data, TTL_TREND_SHORT, TimeUnit.SECONDS);
    }

    /**
     * 获取消息发送趋势（缓存）
     */
    public List<Map<String, Object>> getMessageSendingTrend(int days) {
        return get(CACHE_KEY_TREND_MESSAGE + days);
    }

    /**
     * 设置消息发送趋势（缓存）
     */
    public void setMessageSendingTrend(int days, List<Map<String, Object>> data) {
        set(CACHE_KEY_TREND_MESSAGE + days, data, TTL_TREND_SHORT, TimeUnit.SECONDS);
    }

    /**
     * 获取群组创建趋势（缓存）
     */
    public List<Map<String, Object>> getGroupCreationTrend(int days) {
        return get(CACHE_KEY_TREND_GROUP + days);
    }

    /**
     * 设置群组创建趋势（缓存）
     */
    public void setGroupCreationTrend(int days, List<Map<String, Object>> data) {
        set(CACHE_KEY_TREND_GROUP + days, data, TTL_TREND_SHORT, TimeUnit.SECONDS);
    }

    /**
     * 获取每小时活跃度趋势（缓存）
     */
    public List<Map<String, Object>> getHourlyActivityTrend(int days) {
        return get(CACHE_KEY_TREND_HOURLY + days);
    }

    /**
     * 设置每小时活跃度趋势（缓存）
     */
    public void setHourlyActivityTrend(int days, List<Map<String, Object>> data) {
        set(CACHE_KEY_TREND_HOURLY + days, data, TTL_TREND_SHORT, TimeUnit.SECONDS);
    }

    /**
     * 获取每日活跃用户（缓存）
     */
    public List<Map<String, Object>> getDailyActiveUsers(int days) {
        return get(CACHE_KEY_TREND_DAILY_ACTIVE + days);
    }

    /**
     * 设置每日活跃用户（缓存）
     */
    public void setDailyActiveUsers(int days, List<Map<String, Object>> data) {
        set(CACHE_KEY_TREND_DAILY_ACTIVE + days, data, TTL_TREND_SHORT, TimeUnit.SECONDS);
    }

    // ==================== 分布数据缓存 ====================

    /**
     * 获取消息类型分布（缓存）
     */
    public List<Map<String, Object>> getMessageTypeDistribution() {
        return get(CACHE_KEY_DISTRIBUTION_MSG_TYPE);
    }

    /**
     * 设置消息类型分布（缓存）
     */
    public void setMessageTypeDistribution(List<Map<String, Object>> data) {
        set(CACHE_KEY_DISTRIBUTION_MSG_TYPE, data, TTL_DISTRIBUTION, TimeUnit.SECONDS);
    }

    /**
     * 获取文件类型分布（缓存）
     */
    public List<Map<String, Object>> getFileTypeDistribution() {
        return get(CACHE_KEY_DISTRIBUTION_FILE_TYPE);
    }

    /**
     * 设置文件类型分布（缓存）
     */
    public void setFileTypeDistribution(List<Map<String, Object>> data) {
        set(CACHE_KEY_DISTRIBUTION_FILE_TYPE, data, TTL_DISTRIBUTION, TimeUnit.SECONDS);
    }

    /**
     * 获取用户活跃度分布（缓存）
     */
    public List<Map<String, Object>> getUserActivityDistribution(int days) {
        return get(CACHE_KEY_DISTRIBUTION_USER_ACTIVITY + days);
    }

    /**
     * 设置用户活跃度分布（缓存）
     */
    public void setUserActivityDistribution(int days, List<Map<String, Object>> data) {
        set(CACHE_KEY_DISTRIBUTION_USER_ACTIVITY + days, data, TTL_DISTRIBUTION, TimeUnit.SECONDS);
    }

    /**
     * 获取会话类型分布（缓存）
     */
    public List<Map<String, Object>> getConversationTypeDistribution() {
        return get(CACHE_KEY_DISTRIBUTION_CONVERSATION_TYPE);
    }

    /**
     * 设置会话类型分布（缓存）
     */
    public void setConversationTypeDistribution(List<Map<String, Object>> data) {
        set(CACHE_KEY_DISTRIBUTION_CONVERSATION_TYPE, data, TTL_DISTRIBUTION, TimeUnit.SECONDS);
    }

    // ==================== 对比数据缓存 ====================

    /**
     * 获取周对比数据（缓存）
     */
    public Map<String, Object> getWeeklyComparison() {
        return get(CACHE_KEY_COMPARISON_WEEKLY);
    }

    /**
     * 设置周对比数据（缓存）
     */
    public void setWeeklyComparison(Map<String, Object> data) {
        set(CACHE_KEY_COMPARISON_WEEKLY, data, TTL_COMPARISON, TimeUnit.SECONDS);
    }

    /**
     * 获取月对比数据（缓存）
     */
    public Map<String, Object> getMonthlyComparison() {
        return get(CACHE_KEY_COMPARISON_MONTHLY);
    }

    /**
     * 设置月对比数据（缓存）
     */
    public void setMonthlyComparison(Map<String, Object> data) {
        set(CACHE_KEY_COMPARISON_MONTHLY, data, TTL_COMPARISON, TimeUnit.SECONDS);
    }

    /**
     * 获取每小时对比数据（缓存）
     */
    public Map<String, Object> getHourlyComparison() {
        return get(CACHE_KEY_COMPARISON_HOURLY);
    }

    /**
     * 设置每小时对比数据（缓存）
     */
    public void setHourlyComparison(Map<String, Object> data) {
        set(CACHE_KEY_COMPARISON_HOURLY, data, TTL_COMPARISON, TimeUnit.SECONDS);
    }

    // ==================== 排行榜缓存 ====================

    /**
     * 获取最活跃用户排行（缓存）
     */
    public List<Map<String, Object>> getTopActiveUsers(int days, int limit) {
        return get(CACHE_KEY_RANKING_USERS + days + ":" + limit);
    }

    /**
     * 设置最活跃用户排行（缓存）
     */
    public void setTopActiveUsers(int days, int limit, List<Map<String, Object>> data) {
        set(CACHE_KEY_RANKING_USERS + days + ":" + limit, data, TTL_RANKING, TimeUnit.SECONDS);
    }

    /**
     * 获取最活跃群组排行（缓存）
     */
    public List<Map<String, Object>> getTopActiveGroups(int days, int limit) {
        return get(CACHE_KEY_RANKING_GROUPS + days + ":" + limit);
    }

    /**
     * 设置最活跃群组排行（缓存）
     */
    public void setTopActiveGroups(int days, int limit, List<Map<String, Object>> data) {
        set(CACHE_KEY_RANKING_GROUPS + days + ":" + limit, data, TTL_RANKING, TimeUnit.SECONDS);
    }

    /**
     * 获取消息发送排行（缓存）
     */
    public List<Map<String, Object>> getTopMessageSenders(int days, int limit) {
        return get(CACHE_KEY_RANKING_SENDERS + days + ":" + limit);
    }

    /**
     * 设置消息发送排行（缓存）
     */
    public void setTopMessageSenders(int days, int limit, List<Map<String, Object>> data) {
        set(CACHE_KEY_RANKING_SENDERS + days + ":" + limit, data, TTL_RANKING, TimeUnit.SECONDS);
    }

    // ==================== 实时监控缓存 ====================

    /**
     * 获取实时在线用户数（缓存）
     */
    public Long getRealtimeOnlineCount() {
        return get(CACHE_KEY_REALTIME_ONLINE);
    }

    /**
     * 设置实时在线用户数（缓存）
     */
    public void setRealtimeOnlineCount(Long count) {
        set(CACHE_KEY_REALTIME_ONLINE, count, TTL_REALTIME, TimeUnit.SECONDS);
    }

    /**
     * 获取实时消息流量（缓存）
     */
    public List<Map<String, Object>> getRealtimeMessageFlow(int minutes) {
        return get(CACHE_KEY_REALTIME_MESSAGE_FLOW + minutes);
    }

    /**
     * 设置实时消息流量（缓存）
     */
    public void setRealtimeMessageFlow(int minutes, List<Map<String, Object>> data) {
        set(CACHE_KEY_REALTIME_MESSAGE_FLOW + minutes, data, TTL_REALTIME, TimeUnit.SECONDS);
    }

    // ==================== 缓存清除方法 ====================

    /**
     * 清除基础统计缓存
     */
    public void clearBasicStats() {
        delete(CACHE_KEY_BASIC_STATS);
    }

    /**
     * 清除所有趋势缓存
     */
    public void clearTrendCache() {
        deletePattern(CACHE_KEY_PREFIX_TREND + "*");
    }

    /**
     * 清除所有分布缓存
     */
    public void clearDistributionCache() {
        deletePattern("im:dashboard:distribution:*");
    }

    /**
     * 清除所有对比缓存
     */
    public void clearComparisonCache() {
        deletePattern("im:dashboard:comparison:*");
    }

    /**
     * 清除所有排行榜缓存
     */
    public void clearRankingCache() {
        deletePattern("im:dashboard:ranking:*");
    }

    /**
     * 清除所有实时监控缓存
     */
    public void clearRealtimeCache() {
        deletePattern("im:dashboard:realtime:*");
    }
}
