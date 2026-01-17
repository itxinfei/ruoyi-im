package com.ruoyi.web.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
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

    // ==================== Dashboard数据可视化查询方法 ====================

    // 1. 时间序列查询（5个）
    /**
     * 获取用户注册趋势
     * @param startTime 开始时间
     * @param dateFormat 日期格式（如：%Y-%m-%d、%Y-%m）
     * @return 用户注册趋势数据
     */
    List<Map<String, Object>> getUserRegistrationTrend(@Param("startTime") String startTime, @Param("dateFormat") String dateFormat);

    /**
     * 获取消息发送趋势
     * @param startTime 开始时间
     * @param dateFormat 日期格式
     * @return 消息发送趋势数据
     */
    List<Map<String, Object>> getMessageSendingTrend(@Param("startTime") String startTime, @Param("dateFormat") String dateFormat);

    /**
     * 获取群组创建趋势
     * @param startTime 开始时间
     * @param dateFormat 日期格式
     * @return 群组创建趋势数据
     */
    List<Map<String, Object>> getGroupCreationTrend(@Param("startTime") String startTime, @Param("dateFormat") String dateFormat);

    /**
     * 获取每小时活跃度趋势
     * @param startTime 开始时间
     * @return 每小时活跃度数据
     */
    List<Map<String, Object>> getHourlyActivityTrend(@Param("startTime") String startTime);

    /**
     * 获取每日活跃用户数
     * @param startTime 开始时间
     * @param dateFormat 日期格式
     * @return 每日活跃用户数据
     */
    List<Map<String, Object>> getDailyActiveUsers(@Param("startTime") String startTime, @Param("dateFormat") String dateFormat);

    // 2. 分类统计查询（3个）
    /**
     * 获取消息类型分布
     * @param startTime 开始时间（可选）
     * @return 消息类型分布数据
     */
    List<Map<String, Object>> getMessageTypeDistribution(@Param("startTime") String startTime);

    /**
     * 获取文件类型分布
     * @param startTime 开始时间（可选）
     * @return 文件类型分布数据
     */
    List<Map<String, Object>> getFileTypeDistribution(@Param("startTime") String startTime);

    /**
     * 获取用户活跃度分布
     * @param startTime 开始时间
     * @return 用户活跃度分布数据
     */
    List<Map<String, Object>> getUserActivityDistribution(@Param("startTime") String startTime);

    // 3. 对比分析查询（3个）
    /**
     * 获取周对比数据（本周vs上周）
     * @return 周对比数据
     */
    List<Map<String, Object>> getWeeklyComparison();

    /**
     * 获取月对比数据（本月vs上月）
     * @return 月对比数据
     */
    List<Map<String, Object>> getMonthlyComparison();

    /**
     * 获取每小时对比数据（今天vs昨天）
     * @return 每小时对比数据
     */
    List<Map<String, Object>> getHourlyComparison();

    // 4. 排行榜查询（3个）
    /**
     * 获取最活跃用户排行
     * @param startTime 开始时间
     * @param limit 返回条数
     * @return 最活跃用户列表
     */
    List<Map<String, Object>> getTopActiveUsers(@Param("startTime") String startTime, @Param("limit") Integer limit);

    /**
     * 获取最活跃群组排行
     * @param startTime 开始时间
     * @param limit 返回条数
     * @return 最活跃群组列表
     */
    List<Map<String, Object>> getTopActiveGroups(@Param("startTime") String startTime, @Param("limit") Integer limit);

    /**
     * 获取消息发送排行
     * @param startTime 开始时间
     * @param limit 返回条数
     * @return 消息发送排行数据
     */
    List<Map<String, Object>> getTopMessageSenders(@Param("startTime") String startTime, @Param("limit") Integer limit);

    // 5. 实时监控查询（2个）
    /**
     * 获取实时在线用户数
     * @return 在线用户数
     */
    Long getRealtimeOnlineCount();

    /**
     * 获取实时消息流量
     * @param minutes 最近多少分钟
     * @return 实时消息流量数据
     */
    List<Map<String, Object>> getRealtimeMessageFlow(@Param("minutes") Integer minutes);

    // 辅助查询方法
    /**
     * 获取指定时间段的消息总数
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消息总数
     */
    Long getMessageCountByPeriod(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 获取指定时间段的用户注册数
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户注册数
     */
    Long getUserCountByPeriod(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 获取指定时间段的群组创建数
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 群组创建数
     */
    Long getGroupCountByPeriod(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 获取会话类型分布
     * @return 会话类型分布数据
     */
    List<Map<String, Object>> getConversationTypeDistribution();
}
