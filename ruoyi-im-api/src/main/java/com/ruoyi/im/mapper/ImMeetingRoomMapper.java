package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.domain.ImMeetingRoom;
import com.ruoyi.im.dto.meeting.ImMeetingRoomQueryRequest;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会议室Mapper
 *
 * @author ruoyi
 */
public interface ImMeetingRoomMapper extends BaseMapper<ImMeetingRoom> {

    /**
     * 分页查询会议室列表
     *
     * @param page 分页参数
     * @param req 查询条件
     * @return 会议室分页列表
     */
    IPage<ImMeetingRoom> selectRoomPage(Page<?> page, @Param("req") ImMeetingRoomQueryRequest req);

    /**
     * 查询可用的会议室列表
     *
     * @return 可用会议室列表
     */
    List<ImMeetingRoom> selectAvailableRooms();

    /**
     * 查询指定部门下的会议室列表
     *
     * @param departmentId 部门ID
     * @return 会议室列表
     */
    List<ImMeetingRoom> selectByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 检查会议室在指定时间段是否可用
     *
     * @param roomId 会议室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 冲突的预订数量
     */
    int checkRoomAvailability(@Param("roomId") Long roomId,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);

    /**
     * 获取会议室预订统计
     *
     * @param roomId 会议室ID
     * @return 预订数量
     */
    int countBookingsByRoomId(@Param("roomId") Long roomId);
}
