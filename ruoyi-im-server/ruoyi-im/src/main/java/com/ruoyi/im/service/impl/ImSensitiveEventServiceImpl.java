package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.im.domain.ImSensitiveEvent;
import com.ruoyi.im.mapper.ImSensitiveEventMapper;
import com.ruoyi.im.service.IImSensitiveEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 敏感事件Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Service
public class ImSensitiveEventServiceImpl extends ServiceImpl<ImSensitiveEventMapper, ImSensitiveEvent> implements IImSensitiveEventService {

    @Autowired
    private ImSensitiveEventMapper imSensitiveEventMapper;

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
    @Override
    public boolean recordSensitiveEvent(Long userId, Long messageId, String originalContent, String hitWords, String level) {
        ImSensitiveEvent event = new ImSensitiveEvent();
        event.setUserId(userId);
        event.setMessageId(messageId);
        event.setOriginalContent(originalContent);
        event.setHitWords(hitWords);
        event.setLevel(level);
        event.setStatus("PENDING"); // 默认为待处理
        event.setCreateTime(new Date());
        return save(event);
    }

    /**
     * 批量记录敏感事件
     * 
     * @param events 敏感事件列表
     * @return 记录数量
     */
    @Override
    public int recordSensitiveEventBatch(List<ImSensitiveEvent> events) {
        int count = 0;
        for (ImSensitiveEvent event : events) {
            if (save(event)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 处理敏感事件
     * 
     * @param eventId 事件ID
     * @param status 处理状态
     * @param handlerId 处理人ID
     * @param handleComment 处理备注
     * @return 是否成功
     */
    @Override
    public boolean handleSensitiveEvent(Long eventId, String status, Long handlerId, String handleComment) {
        ImSensitiveEvent event = getById(eventId);
        if (event != null) {
            event.setStatus(status);
            // 如果有处理人ID，可以设置
            if (handlerId != null) {
                // 设置处理人相关信息
            }
            return updateById(event);
        }
        return false;
    }

    /**
     * 批量处理敏感事件
     * 
     * @param eventIds 事件ID列表
     * @param status 处理状态
     * @param handlerId 处理人ID
     * @return 处理数量
     */
    @Override
    public int handleSensitiveEventBatch(List<Long> eventIds, String status, Long handlerId) {
        int count = 0;
        for (Long eventId : eventIds) {
            if (handleSensitiveEvent(eventId, status, handlerId, null)) {
                count++;
            }
        }
        return count;
    }

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
    @Override
    public List<ImSensitiveEvent> selectSensitiveEvents(Long userId, String level, String status, Date startTime, Date endTime) {
        return imSensitiveEventMapper.selectSensitiveEvents(userId, level, status, startTime, endTime);
    }

    /**
     * 查询用户的敏感事件
     * 
     * @param userId 用户ID
     * @param level 敏感级别（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 敏感事件列表
     */
    @Override
    public List<ImSensitiveEvent> selectUserSensitiveEvents(Long userId, String level, Date startTime, Date endTime) {
        return imSensitiveEventMapper.selectUserSensitiveEvents(userId, level, startTime, endTime);
    }

    /**
     * 查询待处理的敏感事件
     * 
     * @param level 敏感级别（可选）
     * @param limit 限制数量
     * @return 敏感事件列表
     */
    @Override
    public List<ImSensitiveEvent> selectPendingEvents(String level, int limit) {
        return imSensitiveEventMapper.selectPendingEvents(level, limit);
    }

    /**
     * 统计用户敏感事件数量
     * 
     * @param userId 用户ID
     * @param level 敏感级别（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 事件数量
     */
    @Override
    public int countUserSensitiveEvents(Long userId, String level, Date startTime, Date endTime) {
        return imSensitiveEventMapper.countUserSensitiveEvents(userId, level, startTime, endTime);
    }

    /**
     * 统计各级别敏感事件数量
     * 
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 统计信息
     */
    @Override
    public Map<String, Integer> countEventsByLevel(Date startTime, Date endTime) {
        return imSensitiveEventMapper.countEventsByLevel(startTime, endTime);
    }

    /**
     * 统计待处理敏感事件数量
     * 
     * @param level 敏感级别（可选）
     * @return 待处理事件数量
     */
    @Override
    public int countPendingEvents(String level) {
        return imSensitiveEventMapper.countPendingEvents(level);
    }

    /**
     * 查询敏感词频繁触发的用户
     * 
     * @param level 敏感级别（可选）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 用户统计列表
     */
    @Override
    public List<Map<String, Object>> selectFrequentSensitiveUsers(String level, Date startTime, Date endTime, int limit) {
        return imSensitiveEventMapper.selectFrequentSensitiveUsers(level, startTime, endTime, limit);
    }

    /**
     * 查询敏感词命中统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 敏感词统计列表
     */
    @Override
    public List<Map<String, Object>> selectSensitiveWordHitStatistics(Date startTime, Date endTime, int limit) {
        return imSensitiveEventMapper.selectSensitiveWordHitStatistics(startTime, endTime, limit);
    }

    /**
     * 获取敏感事件详细信息
     * 
     * @param eventId 事件ID
     * @return 敏感事件信息
     */
    @Override
    public ImSensitiveEvent getSensitiveEventDetail(Long eventId) {
        return getById(eventId);
    }

    /**
     * 查询按日期统计的敏感事件
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日期统计列表
     */
    @Override
    public List<Map<String, Object>> selectEventStatisticsByDate(Date startTime, Date endTime) {
        return imSensitiveEventMapper.selectEventStatisticsByDate(startTime, endTime);
    }

    /**
     * 清理过期的敏感事件
     * 
     * @param days 过期天数
     * @return 清理数量
     */
    @Override
    public int cleanExpiredEvents(int days) {
        return imSensitiveEventMapper.cleanExpiredEvents(days);
    }

    /**
     * 根据消息ID查询敏感事件
     * 
     * @param messageId 消息ID
     * @return 敏感事件
     */
    @Override
    public ImSensitiveEvent getSensitiveEventByMessageId(Long messageId) {
        return imSensitiveEventMapper.getSensitiveEventByMessageId(messageId);
    }

    /**
     * 删除用户的所有敏感事件
     * 
     * @param userId 用户ID
     * @return 删除数量
     */
    @Override
    public int deleteUserAllSensitiveEvents(Long userId) {
        return imSensitiveEventMapper.deleteUserAllSensitiveEvents(userId);
    }

    /**
     * 获取敏感事件统计报告
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计报告
     */
    @Override
    public Map<String, Object> getSensitiveEventReport(Date startTime, Date endTime) {
        return imSensitiveEventMapper.getSensitiveEventReport(startTime, endTime);
    }

    /**
     * 检查用户是否为敏感用户（频繁触发敏感词）
     * 
     * @param userId 用户ID
     * @param days 统计天数
     * @param threshold 阈值
     * @return 是否为敏感用户
     */
    @Override
    public boolean isSensitiveUser(Long userId, int days, int threshold) {
        int count = countUserSensitiveEvents(userId, null, 
            new Date(System.currentTimeMillis() - days * 24L * 60 * 60 * 1000), 
            new Date());
        return count > threshold;
    }

    /**
     * 自动处理低级别敏感事件
     * 
     * @param level 敏感级别
     * @param days 处理多少天前的事件
     * @return 处理数量
     */
    @Override
    public int autoHandleLowLevelEvents(String level, int days) {
        return imSensitiveEventMapper.autoHandleLowLevelEvents(level, days);
    }
}