package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImMeetingBooking;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会议室预订Mapper
 *
 * @author ruoyi
 */
public interface ImMeetingBookingMapper extends BaseMapper<ImMeetingBooking> {

    /**
     * 查询用户的有效预订列表
     *
     * @param userId 用户ID
     * @return 预订列表
     */
    List<ImMeetingBooking> selectUserBookings(@Param("userId") Long userId);

    /**
     * 查询会议室的预订列表
     *
     * @param roomId 会议室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 预订列表
     */
    List<ImMeetingBooking> selectRoomBookings(@Param("roomId") Long roomId,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定时间范围内的所有预订
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 预订列表
     */
    List<ImMeetingBooking> selectBookingsInTimeRange(@Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 查询需要提醒的预订
     *
     * @param reminderTime 提醒时间
     * @return 预订列表
     */
    List<ImMeetingBooking> selectPendingReminderBookings(@Param("reminderTime") LocalDateTime reminderTime);

    /**
     * 统计用户预订次数
     *
     * @param userId 用户ID
     * @return 预订次数
     */
    int countByUserId(@Param("userId") Long userId);
}
