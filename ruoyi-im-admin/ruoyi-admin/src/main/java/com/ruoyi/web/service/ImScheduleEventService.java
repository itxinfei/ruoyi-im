package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImScheduleEvent;
import java.util.List;
import java.util.Map;

/**
 * 日程事件Service接口
 *
 * @author ruoyi
 */
public interface ImScheduleEventService {

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
    List<ImScheduleEvent> selectEventsByTimeRange(Long userId, String startTime, String endTime);

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
     * 批量删除日程事件
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImScheduleEventByIds(Long[] ids);

    /**
     * 删除日程事件信息
     *
     * @param id 日程事件主键
     * @return 结果
     */
    int deleteImScheduleEventById(Long id);

    /**
     * 更新日程状态
     *
     * @param id 日程ID
     * @param status 状态
     * @return 结果
     */
    int updateEventStatus(Long id, String status);

    /**
     * 取消日程
     *
     * @param id 日程ID
     * @return 结果
     */
    int cancelEvent(Long id);

    /**
     * 完成日程
     *
     * @param id 日程ID
     * @return 结果
     */
    int completeEvent(Long id);

    /**
     * 获取日程统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getEventStatistics();

    /**
     * 查询即将开始的日程
     *
     * @param userId 用户ID
     * @param hours 小时数
     * @return 日程列表
     */
    List<ImScheduleEvent> selectUpcomingEvents(Long userId, int hours);

    /**
     * 查询今日日程
     *
     * @param userId 用户ID
     * @return 日程列表
     */
    List<ImScheduleEvent> selectTodayEvents(Long userId);
}
