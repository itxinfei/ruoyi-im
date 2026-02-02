package com.ruoyi.im.service;

import java.time.LocalDate;
import java.util.Map;

/**
 * 统计服务接口
 * 提供系统数据统计、活跃度分析等功能
 *
 * @author ruoyi
 */
public interface ImStatsService {

    /**
     * 获取系统概览数据
     * 包含用户数、群组数、消息数等核心指标
     *
     * @return 概览统计数据Map
     */
    Map<String, Object> getOverview();

    /**
     * 获取用户活跃度统计
     *
     * @param days 统计天数
     * @return 活跃度数据Map，包含activeUsers、newUsers
     */
    Map<String, Object> getUserActiveStats(Integer days);

    /**
     * 获取群组活跃度统计
     *
     * @param days 统计天数
     * @return 活跃度数据Map，包含activeGroups、newGroups
     */
    Map<String, Object> getGroupActiveStats(Integer days);

    /**
     * 获取消息统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 消息统计数据Map
     */
    Map<String, Object> getMessageStats(LocalDate startDate, LocalDate endDate);

    /**
     * 获取消息类型统计（用于管理后台）
     * 统计不同类型消息的数量
     *
     * @param params 查询参数
     * @return 消息类型统计数据Map，包含totalMessages、textMessages、imageMessages、fileMessages
     */
    Map<String, Object> getMessageAdminStats(Map<String, Object> params);

    /**
     * 获取用户角色统计（用于管理后台）
     * 统计不同角色的用户数量
     *
     * @return 用户角色统计数据Map，包含total、superAdminCount、adminCount、userCount
     */
    Map<String, Object> getUserStats();
}
