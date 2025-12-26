package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImSensitiveEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 敏感词事件Mapper接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Mapper
public interface ImSensitiveEventMapper extends BaseMapper<ImSensitiveEvent> {

    /**
     * 查询敏感词事件列表（包含用户详细信息）
     * 
     * @param userId 用户ID（可选）
     * @param level 敏感级别（可选）
     * @param status 处理状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param limit 限制数量
     * @return 敏感词事件列表
     */
    List<ImSensitiveEvent> selectEventsWithDetails(@Param("userId") Long userId,
                                                  @Param("level") String level,
                                                  @Param("status") String status,
                                                  @Param("startTime") Date startTime,
                                                  @Param("endTime") Date endTime,
                                                  @Param("limit") Integer limit);

    /**
     * 查询用户的敏感词事件列表
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 敏感词事件列表
     */
    List<ImSensitiveEvent> selectUserEvents(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 查询待处理的敏感词事件列表
     * 
     * @param limit 限制数量
     * @return 敏感词事件列表
     */
    List<ImSensitiveEvent> selectPendingEvents(@Param("limit") Integer limit);

    /**
     * 统计用户敏感词事件数量
     * 
     * @param userId 用户ID
     * @param level 敏感级别（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 事件数量
     */
    int countUserEvents(@Param("userId") Long userId,
                       @Param("level") String level,
                       @Param("startTime") Date startTime,
                       @Param("endTime") Date endTime);

    /**
     * 统计待处理事件数量
     * 
     * @return 待处理事件数量
     */
    int countPendingEvents();

    /**
     * 批量更新事件状态
     * 
     * @param eventIds 事件ID列表
     * @param status 新状态
     * @return 更新数量
     */
    int updateStatusBatch(@Param("eventIds") List<Long> eventIds, @Param("status") String status);

    /**
     * 查询高频敏感词用户列表
     * 
     * @param days 统计天数
     * @param minCount 最小触发次数
     * @param limit 限制数量
     * @return 用户统计列表
     */
    List<Object> selectFrequentUsers(@Param("days") int days, 
                                    @Param("minCount") int minCount, 
                                    @Param("limit") Integer limit);

    /**
     * 查询敏感词命中统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 敏感词统计列表
     */
    List<Object> selectWordHitStatistics(@Param("startTime") Date startTime,
                                        @Param("endTime") Date endTime,
                                        @Param("limit") Integer limit);

    /**
     * 查询敏感词事件统计（按日期分组）
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息列表
     */
    List<Object> selectEventStatistics(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 删除过期事件（超过指定天数的已处理事件）
     * 
     * @param days 天数
     * @return 删除数量
     */
    int deleteExpiredEvents(@Param("days") int days);

    /**
     * 根据消息ID查询敏感词事件
     * 
     * @param messageId 消息ID
     * @return 敏感词事件
     */
    ImSensitiveEvent selectByMessageId(@Param("messageId") Long messageId);

    /**
     * 删除用户的所有敏感词事件
     * 
     * @param userId 用户ID
     * @return 删除数量
     */
    int deleteByUserId(@Param("userId") Long userId);
}