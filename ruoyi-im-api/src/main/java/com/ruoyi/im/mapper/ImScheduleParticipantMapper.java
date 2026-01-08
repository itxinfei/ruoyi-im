package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImScheduleParticipant;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 日程参与人Mapper
 */
public interface ImScheduleParticipantMapper extends BaseMapper<ImScheduleParticipant> {

    /**
     * 查询日程的参与人列表（带用户信息）
     */
    @Select("SELECT p.*, u.nickname as userName, u.avatar as userAvatar " +
            "FROM im_schedule_participant p " +
            "LEFT JOIN im_user u ON p.user_id = u.id " +
            "WHERE p.event_id = #{eventId} " +
            "ORDER BY p.create_time ASC")
    List<ImScheduleParticipant> selectByEventId(@Param("eventId") Long eventId);

    /**
     * 查询用户在某个日程中的参与记录
     */
    @Select("SELECT * FROM im_schedule_participant WHERE event_id = #{eventId} AND user_id = #{userId}")
    ImScheduleParticipant selectByEventAndUser(@Param("eventId") Long eventId, @Param("userId") Long userId);

    /**
     * 统计日程的参与人数
     */
    @Select("SELECT COUNT(*) FROM im_schedule_participant WHERE event_id = #{eventId}")
    Integer countByEventId(@Param("eventId") Long eventId);
}
