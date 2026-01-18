package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImScheduleEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * IM日程事件数据访问层接口（Admin模块专用）
 *
 * <p>负责处理IM日程事件管理相关的数据库操作</p>
 * <p>主要功能包括：日程事件的增删改查、按时间范围查询、日程状态管理、日程统计等</p>
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Mapper
public interface ImScheduleEventMapper {


    /**
     * 查询日程事件
     *
     * @param id 日程事件主键
     * @return 日程事件
     */
    ImScheduleEvent selectImScheduleEventById(Long id);

    /**
     * 查询日程事件列表
     *
     * @param imScheduleEvent 日程事件
     * @return 日程事件集合
     */
    List<ImScheduleEvent> selectImScheduleEventList(ImScheduleEvent imScheduleEvent);

    /**
     * 查询用户的日程事件
     *
     * @param userId 用户ID
     * @return 日程列表
     */
    List<ImScheduleEvent> selectEventsByUserId(Long userId);

    /**
     * 查询指定时间范围内的日程事件
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日程列表
     */
    List<ImScheduleEvent> selectEventsByTimeRange(@Param("userId") Long userId,
                                                   @Param("startTime") String startTime,
                                                   @Param("endTime") String endTime);

    /**
     * 新增日程事件
     *
     * @param imScheduleEvent 日程事件
     * @return 结果
     */
    int insertImScheduleEvent(ImScheduleEvent imScheduleEvent);

    /**
     * 修改日程事件
     *
     * @param imScheduleEvent 日程事件
     * @return 结果
     */
    int updateImScheduleEvent(ImScheduleEvent imScheduleEvent);

    /**
     * 删除日程事件
     *
     * @param id 日程事件主键
     * @return 结果
     */
    int deleteImScheduleEventById(Long id);

    /**
     * 批量删除日程事件
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImScheduleEventByIds(Long[] ids);

    /**
     * 更新日程状态
     *
     * @param id 日程ID
     * @param status 状态
     * @return 结果
     */
    int updateEventStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 获取日程统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getEventStatistics();

    /**
     * 查询即将开始的日程（指定时间内）
     *
     * @param userId 用户ID
     * @param hours 小时数
     * @return 日程列表
     */
    List<ImScheduleEvent> selectUpcomingEvents(@Param("userId") Long userId, @Param("hours") int hours);

    /**
     * 查询今日日程
     *
     * @param userId 用户ID
     * @return 日程列表
     */
    List<ImScheduleEvent> selectTodayEvents(Long userId);
}
