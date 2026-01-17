package com.ruoyi.web.service;

import java.util.List;
import java.util.Map;

/**
 * IM系统仪表板服务接口
 * 提供Dashboard首页数据可视化相关业务逻辑
 *
 * @author ruoyi
 */
public interface ImDashboardService {

    // ==================== 基础统计 ====================

    /**
     * 获取系统基础统计数据
     * 包含：用户总数、在线用户、消息总数、群组总数、会话总数等
     *
     * @return 基础统计数据
     */
    Map<String, Object> getBasicStatistics();

    // ==================== 趋势分析 ====================

    /**
     * 获取用户注册趋势
     *
     * @param days 查询天数（7/30/90）
     * @return 用户注册趋势数据
     */
    List<Map<String, Object>> getUserRegistrationTrend(int days);

    /**
     * 获取消息发送趋势
     *
     * @param days 查询天数（7/30/90）
     * @return 消息发送趋势数据
     */
    List<Map<String, Object>> getMessageSendingTrend(int days);

    /**
     * 获取群组创建趋势
     *
     * @param days 查询天数（7/30/90）
     * @return 群组创建趋势数据
     */
    List<Map<String, Object>> getGroupCreationTrend(int days);

    // ==================== 分布分析 ====================

    /**
     * 获取消息类型分布
     *
     * @param days 查询天数（可选，null表示全部）
     * @return 消息类型分布数据
     */
    List<Map<String, Object>> getMessageTypeDistribution(Integer days);

    /**
     * 获取文件类型分布
     *
     * @param days 查询天数（可选，null表示全部）
     * @return 文件类型分布数据
     */
    List<Map<String, Object>> getFileTypeDistribution(Integer days);

    /**
     * 获取用户活跃度分布
     *
     * @param days 查询天数
     * @return 用户活跃度分布数据
     */
    List<Map<String, Object>> getUserActivityDistribution(int days);

    // ==================== 对比分析 ====================

    /**
     * 获取周对比数据（本周vs上周）
     *
     * @return 周对比数据
     */
    Map<String, Object> getWeeklyComparison();

    /**
     * 获取月对比数据（本月vs上月）
     *
     * @return 月对比数据
     */
    Map<String, Object> getMonthlyComparison();

    /**
     * 获取每小时对比数据（今天vs昨天）
     *
     * @return 每小时对比数据
     */
    Map<String, Object> getHourlyComparison();

    // ==================== 排行榜 ====================

    /**
     * 获取最活跃用户排行
     *
     * @param days 查询天数
     * @param limit 返回条数（默认10）
     * @return 最活跃用户列表
     */
    List<Map<String, Object>> getTopActiveUsers(int days, int limit);

    /**
     * 获取最活跃群组排行
     *
     * @param days 查询天数
     * @param limit 返回条数（默认10）
     * @return 最活跃群组列表
     */
    List<Map<String, Object>> getTopActiveGroups(int days, int limit);

    /**
     * 获取消息发送排行
     *
     * @param days 查询天数
     * @param limit 返回条数（默认10）
     * @return 消息发送排行数据
     */
    List<Map<String, Object>> getTopMessageSenders(int days, int limit);

    // ==================== 实时监控 ====================

    /**
     * 获取实时在线用户数
     *
     * @return 在线用户数
     */
    Long getRealtimeOnlineCount();

    /**
     * 获取实时消息流量
     *
     * @param minutes 最近多少分钟（默认30分钟）
     * @return 实时消息流量数据
     */
    List<Map<String, Object>> getRealtimeMessageFlow(int minutes);

    /**
     * 获取每小时活跃度趋势
     *
     * @param days 查询天数（默认7天）
     * @return 每小时活跃度数据
     */
    List<Map<String, Object>> getHourlyActivityTrend(int days);

    /**
     * 获取每日活跃用户数
     *
     * @param days 查询天数（默认30天）
     * @return 每日活跃用户数据
     */
    List<Map<String, Object>> getDailyActiveUsers(int days);

    // ==================== 辅助方法 ====================

    /**
     * 获取会话类型分布
     *
     * @return 会话类型分布数据
     */
    List<Map<String, Object>> getConversationTypeDistribution();
}
