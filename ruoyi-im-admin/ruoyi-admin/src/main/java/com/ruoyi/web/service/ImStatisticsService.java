package com.ruoyi.web.service;

import java.util.Map;

/**
 * IM统计Service接口（Admin模块专用）
 *
 * <p>负责处理IM系统统计相关的业务逻辑</p>
 * <p>主要功能包括：系统统计、用户统计、群组统计、消息统计、文件统计、活跃度排行等</p>
 *
 * @author ruoyi
 * @date 2025-01-07
 */
public interface ImStatisticsService {

    /**
     * 获取系统统计数据
     *
     * <p>统计IM系统的整体数据，包括用户数、群组数、消息数、文件数等</p>
     * <p>用于系统概览展示和Dashboard数据面板</p>
     *
     * @return 系统统计数据Map，包含用户总数、在线用户数、群组总数、消息总数、文件总数等
     */
    Map<String, Object> getSystemStatistics();

    /**
     * 获取用户统计数据
     *
     * <p>统计指定用户在指定时间段内的数据</p>
     * <p>用于用户个人中心、用户画像分析</p>
     *
     * @param userId 用户ID，不能为空
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @param endTime 结束时间，格式：yyyy-MM-dd HH:mm:ss
     * @return 用户统计数据Map，包含发送消息数、接收消息数、加入群组数、上传文件数、在线时长等
     */
    Map<String, Object> getUserStatistics(Long userId, String startTime, String endTime);

    /**
     * 获取群组统计数据
     *
     * <p>统计指定群组在指定时间段内的数据</p>
     * <p>用于群组详情页、群组活跃度分析</p>
     *
     * @param groupId 群组ID，不能为空
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @param endTime 结束时间，格式：yyyy-MM-dd HH:mm:ss
     * @return 群组统计数据Map，包含群组成员数、消息总数、活跃用户数、文件分享数等
     */
    Map<String, Object> getGroupStatistics(Long groupId, String startTime, String endTime);

    /**
     * 获取消息统计数据
     *
     * <p>统计指定会话在指定时间段内的消息数据</p>
     * <p>用于会话详情页、消息统计展示</p>
     *
     * @param conversationId 会话ID，不能为空
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @param endTime 结束时间，格式：yyyy-MM-dd HH:mm:ss
     * @return 消息统计数据Map，包含消息总数、各类型消息数量、发送人数、接收人数等
     */
    Map<String, Object> getMessageStatistics(Long conversationId, String startTime, String endTime);

    /**
     * 获取文件统计数据
     *
     * <p>统计系统中文件的整体数据</p>
     * <p>用于文件管理页面、存储空间统计</p>
     *
     * @return 文件统计数据Map，包含文件总数、总大小、各类型文件数量、上传次数、下载次数等
     */
    Map<String, Object> getFileStatistics();

    /**
     * 获取活跃度排行
     *
     * <p>统计指定天数内的活跃度排行数据</p>
     * <p>用于排行榜展示、活跃度分析</p>
     *
     * @param days 统计天数，为null表示默认天数
     * @return 活跃度排行数据Map，包含最活跃用户列表、最活跃群组列表、最活跃会话列表等
     */
    Map<String, Object> getActiveRanking(Integer days);

    /**
     * 获取在线统计数据
     *
     * <p>统计系统当前的在线状态数据</p>
     * <p>用于在线监控、实时统计展示</p>
     *
     * @return 在线统计数据Map，包含在线用户数、离线用户数、在线率、今日在线峰值等
     */
    Map<String, Object> getOnlineStatistics();
}
