package com.ruoyi.im.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.im.dto.meeting.ImMeetingBookingRequest;
import com.ruoyi.im.dto.meeting.ImMeetingRoomCreateRequest;
import com.ruoyi.im.dto.meeting.ImMeetingRoomQueryRequest;
import com.ruoyi.im.dto.meeting.ImMeetingRoomUpdateRequest;
import com.ruoyi.im.vo.meeting.ImMeetingBookingVO;
import com.ruoyi.im.vo.meeting.ImMeetingRoomScheduleVO;
import com.ruoyi.im.vo.meeting.ImMeetingRoomVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 会议室管理服务接口
 *
 * @author ruoyi
 */
public interface ImMeetingRoomService {

    /**
     * 创建会议室
     *
     * @param request 创建请求
     * @param userId 当前用户ID
     * @return 会议室ID
     */
    Long createRoom(ImMeetingRoomCreateRequest request, Long userId);

    /**
     * 更新会议室
     *
     * @param request 更新请求
     * @param userId 当前用户ID
     */
    void updateRoom(ImMeetingRoomUpdateRequest request, Long userId);

    /**
     * 删除会议室
     *
     * @param roomId 会议室ID
     * @param userId 当前用户ID
     */
    void deleteRoom(Long roomId, Long userId);

    /**
     * 获取会议室详情
     *
     * @param roomId 会议室ID
     * @return 会议室详情
     */
    ImMeetingRoomVO getRoomDetail(Long roomId);

    /**
     * 分页查询会议室列表
     *
     * @param request 查询条件
     * @return 分页结果
     */
    IPage<ImMeetingRoomVO> getRoomPage(ImMeetingRoomQueryRequest request);

    /**
     * 获取可用会议室列表
     *
     * @return 可用会议室列表
     */
    List<ImMeetingRoomVO> getAvailableRooms();

    /**
     * 预订会议室
     *
     * @param request 预订请求
     * @param userId 当前用户ID
     * @return 预订ID
     */
    Long bookRoom(ImMeetingBookingRequest request, Long userId);

    /**
     * 取消预订
     *
     * @param bookingId 预订ID
     * @param userId 当前用户ID
     */
    void cancelBooking(Long bookingId, Long userId);

    /**
     * 确认预订
     *
     * @param bookingId 预订ID
     * @param userId 当前用户ID
     */
    void confirmBooking(Long bookingId, Long userId);

    /**
     * 签到
     *
     * @param bookingId 预订ID
     * @param userId 当前用户ID
     */
    void checkIn(Long bookingId, Long userId);

    /**
     * 签退
     *
     * @param bookingId 预订ID
     * @param userId 当前用户ID
     */
    void checkOut(Long bookingId, Long userId);

    /**
     * 获取预订详情
     *
     * @param bookingId 预订ID
     * @param userId 当前用户ID
     * @return 预订详情
     */
    ImMeetingBookingVO getBookingDetail(Long bookingId, Long userId);

    /**
     * 获取用户的预订列表
     *
     * @param userId 用户ID
     * @return 预订列表
     */
    List<ImMeetingBookingVO> getUserBookings(Long userId);

    /**
     * 获取会议室日程
     *
     * @param roomId 会议室ID
     * @param date 日期
     * @return 日程信息
     */
    ImMeetingRoomScheduleVO getRoomSchedule(Long roomId, String date);

    /**
     * 检查会议室可用性
     *
     * @param roomId 会议室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否可用
     */
    boolean checkAvailability(Long roomId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 提交会议反馈
     *
     * @param bookingId 预订ID
     * @param feedback 反馈内容
     * @param rating 评分
     * @param userId 当前用户ID
     */
    void submitFeedback(Long bookingId, String feedback, Integer rating, Long userId);

    /**
     * 获取会议室统计数据
     *
     * @param userId 当前用户ID
     * @return 统计数据
     */
    Map<String, Object> getStatistics(Long userId);
}
