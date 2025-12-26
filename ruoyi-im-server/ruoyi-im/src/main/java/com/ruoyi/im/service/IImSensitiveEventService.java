package com.ruoyi.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.im.domain.ImSensitiveEvent;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 敏感事件Service接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public interface IImSensitiveEventService extends IService<ImSensitiveEvent> {

    /**
     * 记录敏感事件
     * 
     * @param userId 用户ID
     * @param messageId 消息ID
     * @param originalContent 原始内容
     * @param hitWords 命中的敏感词
     * @param level 敏感级别
     * @return 是否成功
     */
    boolean recordSensitiveEvent(Long userId, Long messageId, String originalContent, String hitWords, String level);

    /**
     * 批量记录敏感事件
     * 
     * @param events 敏感事件列表
     * @return 记录数量
     */
    int recordSensitiveEventBatch(List<ImSensitiveEvent> events);

    /**
     * 处理敏感事件
     * 
     * @param eventId 事件ID
     * @param status 处理状态
     * @param handlerId 处理人ID
     * @param handleComment 处理备注
     * @return 是否成功
     */
    boolean handleSensitiveEvent(Long eventId, String status, Long handlerId, String handleComment);

    /**
     * 批量处理敏感事件
     * 
     * @param eventIds 事件ID列表
     * @param status 处理状态
     * @param handlerId 处理人ID
     * @return 处理数量
     */
    int handleSensitiveEventBatch(List<Long> eventIds, String status, Long handlerId);

    /**
     * 查询敏感事件列表
     * 
     * @param userId 用户ID（可选）
     * @param level 敏感级别（可选）
     * @param status 处理状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 敏感事件列表
     */
    List<ImSensitiveEvent> selectSensitiveEvents(Long userId, String level, String status, Date startTime, Date endTime);

    /**
     * 查询用户的敏感事件
     * 
     * @param userId 用户ID
     * @param level 敏感级别（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 敏感事件列表
     */
    List<ImSensitiveEvent> selectUserSensitiveEvents(Long userId, String level, Date startTime, Date endTime);

    /**
     * 查询待处理的敏感事件
     * 
     * @param level 敏感级别（可选）
     * @param limit 限制数量
     * @return 敏感事件列表
     */
    List<ImSensitiveEvent> selectPendingEvents(String level, int limit);

    /**
     * 统计用户敏感事件数量
     * 
     * @param userId 用户ID
     * @param level 敏感级别（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 事件数量
     */
    int countUserSensitiveEvents(Long userId, String level, Date startTime, Date endTime);

    /**
     * 统计各级别敏感事件数量
     * 
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 统计信息
     */
    Map<String, Integer> countEventsByLevel(Date startTime, Date endTime);

    /**
     * 统计待处理敏感事件数量
     * 
     * @param level 敏感级别（可选）
     * @return 待处理事件数量
     */
    int countPendingEvents(String level);

    /**
     * 查询敏感词频繁触发的用户
     * 
     * @param level 敏感级别（可选）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 用户统计列表
     */
    List<Map<String, Object>> selectFrequentSensitiveUsers(String level, Date startTime, Date endTime, int limit);

    /**
     * 查询敏感词命中统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 敏感词统计列表
     */
    List<Map<String, Object>> selectSensitiveWordHitStatistics(Date startTime, Date endTime, int limit);

    /**
     * 获取敏感事件详细信息
     * 
     * @param eventId 事件ID
     * @return 敏感事件信息
     */
    ImSensitiveEvent getSensitiveEventDetail(Long eventId);

    /**
     * 查询按日期统计的敏感事件
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日期统计列表
     */
    List<Map<String, Object>> selectEventStatisticsByDate(Date startTime, Date endTime);

    /**
     * 清理过期的敏感事件
     * 
     * @param days 过期天数
     * @return 清理数量
     */
    int cleanExpiredEvents(int days);

    /**
     * 根据消息ID查询敏感事件
     * 
     * @param messageId 消息ID
     * @return 敏感事件
     */
    ImSensitiveEvent getSensitiveEventByMessageId(Long messageId);

    /**
     * 删除用户的所有敏感事件
     * 
     * @param userId 用户ID
     * @return 删除数量
     */
    int deleteUserAllSensitiveEvents(Long userId);

    /**
     * 获取敏感事件统计报告
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计报告
     */
    Map<String, Object> getSensitiveEventReport(Date startTime, Date endTime);

    /**
     * 检查用户是否为敏感用户（频繁触发敏感词）
     * 
     * @param userId 用户ID
     * @param days 统计天数
     * @param threshold 阈值
     * @return 是否为敏感用户
     */
    boolean isSensitiveUser(Long userId, int days, int threshold);

    /**
     * 自动处理低级别敏感事件
     * 
     * @param level 敏感级别
     * @param days 处理多少天前的事件
     * @return 处理数量
     */
    int autoHandleLowLevelEvents(String level, int days);
}