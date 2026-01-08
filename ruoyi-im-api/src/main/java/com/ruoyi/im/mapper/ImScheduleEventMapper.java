package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.domain.ImScheduleEvent;
import com.ruoyi.im.dto.schedule.ScheduleEventQueryRequest;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日程事件Mapper
 */
public interface ImScheduleEventMapper extends BaseMapper<ImScheduleEvent> {

    /**
     * 分页查询日程事件列表
     */
    IPage<ImScheduleEvent> selectEventPage(Page<?> page, @Param("req") ScheduleEventQueryRequest request, @Param("userId") Long userId);

    /**
     * 查询用户在指定时间范围内的日程
     */
    @Select("SELECT * FROM im_schedule_event " +
            "WHERE user_id = #{userId} " +
            "AND status = 'SCHEDULED' " +
            "AND ((start_time >= #{startTime} AND start_time < #{endTime}) " +
            "     OR (end_time > #{startTime} AND end_time <= #{endTime}) " +
            "     OR (start_time <= #{startTime} AND end_time >= #{endTime})) " +
            "ORDER BY start_time ASC")
    List<ImScheduleEvent> selectByTimeRange(@Param("userId") Long userId,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 查询用户参与的日程
     */
    @Select("SELECT e.* FROM im_schedule_event e " +
            "INNER JOIN im_schedule_participant p ON e.id = p.event_id " +
            "WHERE p.user_id = #{userId} " +
            "AND e.status = 'SCHEDULED' " +
            "ORDER BY e.start_time ASC")
    List<ImScheduleEvent> selectParticipatedByUserId(@Param("userId") Long userId);
}
