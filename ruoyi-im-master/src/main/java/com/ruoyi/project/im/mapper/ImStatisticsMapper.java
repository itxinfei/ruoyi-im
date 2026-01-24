package com.ruoyi.web.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * IM统计数据访问层接口（Admin模块专用）
 *
 * <p>负责处理IM系统统计相关的数据库操作</p>
 * <p>主要功能包括：系统统计、用户统计、群组统计、消息统计、文件统计、活跃度排行、Dashboard数据可视化等</p>
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Mapper
public interface ImStatisticsMapper {

    /**
     * 获取系统统计数据
     *
     * <p>统计IM系统的整体数据，包括用户数、群组数、消息数、文件数等</p>
     * <p>返回的Map包含：用户总数、在线用户数、群组总数、消息总数、文件总数等</p>
     *
     * @return 系统统计数据Map，key为统计项名称，value为统计值
     */
    Map<String, Object> getSystemStatistics();

    /**
     * 获取用户统计数据
     *
     * <p>统计指定用户在指定时间段内的数据</p>
     * <p>返回的Map包含：发送消息数、接收消息数、加入群组数、上传文件数、在线时长等</p>
     *
     * @param userId 用户ID，不能为空
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @param endTime 结束时间，格式：yyyy-MM-dd HH:mm:ss
     * @return 用户统计数据Map，key为统计项名称，value为统计值
     */
    Map<String, Object> getUserStatistics(@Param("userId") Long userId, @Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 获取群组统计数据
     *
     * <p>统计指定群组在指定时间段内的数据</p>
     * <p>返回的Map包含：群组成员数、消息总数、活跃用户数、文件分享数等</p>
     *
     * @param groupId 群组ID，不能为空
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @param endTime 结束时间，格式：yyyy-MM-dd HH:mm:ss
     * @return 群组统计数据Map，key为统计项名称，value为统计值
     */
    Map<String, Object> getGroupStatistics(@Param("groupId") Long groupId, @Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 获取消息统计数据
     *
     * <p>统计指定会话在指定时间段内的消息数据</p>
     * <p>返回的Map包含：消息总数、各类型消息数量、发送人数、接收人数等</p>
     *
     * @param conversationId 会话ID，不能为空
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @param endTime 结束时间，格式：yyyy-MM-dd HH:mm:ss
     * @return 消息统计数据Map，key为统计项名称，value为统计值
     */
    Map<String, Object> getMessageStatistics(@Param("conversationId") Long conversationId, @Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 获取文件统计数据
     *
     * <p>统计系统中文件的整体数据</p>
     * <p>返回的Map包含：文件总数、总大小、各类型文件数量、上传次数、下载次数等</p>
     *
     * @return 文件统计数据Map，key为统计项名称，value为统计值
     */
    Map<String, Object> getFileStatistics();

    /**
     * 获取活跃度排行
     *
     * <p>统计指定天数内的活跃度排行数据</p>
     * <p>返回的Map包含：最活跃用户列表、最活跃群组列表、最活跃会话列表等</p>
     *
     * @param days 统计天数，为null表示默认天数
     * @return 活跃度排行数据Map，key为排行类型，value为排行列表
     */
    Map<String, Object> getActiveRanking(@Param("days") Integer days);

    /**
     * 获取在线统计数据
     *
     * <p>统计系统当前的在线状态数据</p>
     * <p>返回的Map包含：在线用户数、离线用户数、在线率、今日在线峰值等</p>
     *
     * @return 在线统计数据Map，key为统计项名称，value为统计值
     */
    Map<String, Object> getOnlineStatistics();

    // ==================== Dashboard数据可视化查询方法 ====================

    // 1. 时间序列查询（5个）
    /**
     * 获取用户注册趋势
     *
     * <p>统计从指定时间开始到现在的用户注册趋势</p>
     * <p>用于Dashboard折线图展示</p>
     *
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @param dateFormat 日期格式（如：%Y-%m-%d、%Y-%m），用于GROUP BY
     * @return 用户注册趋势数据列表，每条记录包含日期和注册数
     */
    List<Map<String, Object>> getUserRegistrationTrend(@Param("startTime") String startTime, @Param("dateFormat") String dateFormat);

    /**
     * 获取消息发送趋势
     *
     * <p>统计从指定时间开始到现在的消息发送趋势</p>
     * <p>用于Dashboard折线图展示</p>
     *
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @param dateFormat 日期格式（如：%Y-%m-%d、%Y-%m），用于GROUP BY
     * @return 消息发送趋势数据列表，每条记录包含日期和消息数
     */
    List<Map<String, Object>> getMessageSendingTrend(@Param("startTime") String startTime, @Param("dateFormat") String dateFormat);

    /**
     * 获取群组创建趋势
     *
     * <p>统计从指定时间开始到现在的群组创建趋势</p>
     * <p>用于Dashboard折线图展示</p>
     *
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @param dateFormat 日期格式（如：%Y-%m-%d、%Y-%m），用于GROUP BY
     * @return 群组创建趋势数据列表，每条记录包含日期和创建数
     */
    List<Map<String, Object>> getGroupCreationTrend(@Param("startTime") String startTime, @Param("dateFormat") String dateFormat);

    /**
     * 获取每小时活跃度趋势
     *
     * <p>统计从指定时间开始到现在的每小时活跃度</p>
     * <p>用于Dashboard热力图展示</p>
     *
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @return 每小时活跃度数据列表，每条记录包含小时(0-23)和活跃度
     */
    List<Map<String, Object>> getHourlyActivityTrend(@Param("startTime") String startTime);

    /**
     * 获取每日活跃用户数
     *
     * <p>统计从指定时间开始到现在的每日活跃用户数</p>
     * <p>用于Dashboard折线图展示</p>
     *
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @param dateFormat 日期格式（如：%Y-%m-%d），用于GROUP BY
     * @return 每日活跃用户数据列表，每条记录包含日期和活跃用户数
     */
    List<Map<String, Object>> getDailyActiveUsers(@Param("startTime") String startTime, @Param("dateFormat") String dateFormat);

    // 2. 分类统计查询（3个）
    /**
     * 获取消息类型分布
     *
     * <p>统计各类型消息的数量分布</p>
     * <p>用于Dashboard饼图展示</p>
     *
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss，为null表示统计全部
     * @return 消息类型分布数据列表，每条记录包含消息类型和数量
     */
    List<Map<String, Object>> getMessageTypeDistribution(@Param("startTime") String startTime);

    /**
     * 获取文件类型分布
     *
     * <p>统计各类型文件的数量分布</p>
     * <p>用于Dashboard饼图展示</p>
     *
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss，为null表示统计全部
     * @return 文件类型分布数据列表，每条记录包含文件类型和数量
     */
    List<Map<String, Object>> getFileTypeDistribution(@Param("startTime") String startTime);

    /**
     * 获取用户活跃度分布
     *
     * <p>统计用户活跃度的区间分布（高活跃、中活跃、低活跃等）</p>
     * <p>用于Dashboard柱状图展示</p>
     *
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @return 用户活跃度分布数据列表，每条记录包含活跃度级别和用户数
     */
    List<Map<String, Object>> getUserActivityDistribution(@Param("startTime") String startTime);

    // 3. 对比分析查询（3个）
    /**
     * 获取周对比数据
     *
     * <p>对比本周和上周的各项数据</p>
     * <p>用于Dashboard对比图表展示</p>
     *
     * @return 周对比数据列表，包含本周和上周的消息数、活跃用户数等
     */
    List<Map<String, Object>> getWeeklyComparison();

    /**
     * 获取月对比数据
     *
     * <p>对比本月和上月的各项数据</p>
     * <p>用于Dashboard对比图表展示</p>
     *
     * @return 月对比数据列表，包含本月和上月的数据对比
     */
    List<Map<String, Object>> getMonthlyComparison();

    /**
     * 获取每小时对比数据
     *
     * <p>对比今天和昨天每小时的活跃度数据</p>
     * <p>用于Dashboard对比图表展示</p>
     *
     * @return 每小时对比数据列表，包含今天和昨天每小时的活跃度
     */
    List<Map<String, Object>> getHourlyComparison();

    // 4. 排行榜查询（3个）
    /**
     * 获取最活跃用户排行
     *
     * <p>统计指定时间段内最活跃的用户</p>
     * <p>活跃度按发送消息数计算</p>
     *
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @param limit 返回条数，为null表示默认条数
     * @return 最活跃用户列表，每条记录包含用户ID、用户名、活跃度
     */
    List<Map<String, Object>> getTopActiveUsers(@Param("startTime") String startTime, @Param("limit") Integer limit);

    /**
     * 获取最活跃群组排行
     *
     * <p>统计指定时间段内最活跃的群组</p>
     * <p>活跃度按群组消息数计算</p>
     *
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @param limit 返回条数，为null表示默认条数
     * @return 最活跃群组列表，每条记录包含群组ID、群组名、活跃度
     */
    List<Map<String, Object>> getTopActiveGroups(@Param("startTime") String startTime, @Param("limit") Integer limit);

    /**
     * 获取消息发送排行
     *
     * <p>统计指定时间段内消息发送量排行</p>
     * <p>用于识别活跃用户和活跃群组</p>
     *
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @param limit 返回条数，为null表示默认条数
     * @return 消息发送排行数据列表，每条记录包含发送者和消息数
     */
    List<Map<String, Object>> getTopMessageSenders(@Param("startTime") String startTime, @Param("limit") Integer limit);

    // 5. 实时监控查询（2个）
    /**
     * 获取实时在线用户数
     *
     * <p>统计当前时刻的在线用户总数</p>
     * <p>用于Dashboard实时监控面板</p>
     *
     * @return 在线用户数
     */
    Long getRealtimeOnlineCount();

    /**
     * 获取实时消息流量
     *
     * <p>统计最近N分钟内的消息流量</p>
     * <p>用于Dashboard实时监控面板</p>
     *
     * @param minutes 最近多少分钟，为null表示默认值
     * @return 实时消息流量数据列表，每条记录包含时间点和消息数
     */
    List<Map<String, Object>> getRealtimeMessageFlow(@Param("minutes") Integer minutes);

    // 辅助查询方法
    /**
     * 获取指定时间段的消息总数
     *
     * <p>统计指定时间段内的所有消息数量</p>
     * <p>用于Dashboard辅助计算</p>
     *
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @param endTime 结束时间，格式：yyyy-MM-dd HH:mm:ss
     * @return 消息总数
     */
    Long getMessageCountByPeriod(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 获取指定时间段的用户注册数
     *
     * <p>统计指定时间段内的用户注册数量</p>
     * <p>用于Dashboard辅助计算</p>
     *
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @param endTime 结束时间，格式：yyyy-MM-dd HH:mm:ss
     * @return 用户注册数
     */
    Long getUserCountByPeriod(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 获取指定时间段的群组创建数
     *
     * <p>统计指定时间段内的群组创建数量</p>
     * <p>用于Dashboard辅助计算</p>
     *
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @param endTime 结束时间，格式：yyyy-MM-dd HH:mm:ss
     * @return 群组创建数
     */
    Long getGroupCountByPeriod(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 获取会话类型分布
     *
     * <p>统计各类型会话的数量分布</p>
     * <p>会话类型包括：PRIVATE（私聊）、GROUP（群聊）</p>
     * <p>用于Dashboard饼图展示</p>
     *
     * @return 会话类型分布数据列表，每条记录包含会话类型和数量
     */
    List<Map<String, Object>> getConversationTypeDistribution();
}
