package com.ruoyi.im.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.meeting.ImMeetingBookingRequest;
import com.ruoyi.im.dto.meeting.ImMeetingRoomCreateRequest;
import com.ruoyi.im.dto.meeting.ImMeetingRoomQueryRequest;
import com.ruoyi.im.dto.meeting.ImMeetingRoomUpdateRequest;
import com.ruoyi.im.service.ImMeetingRoomService;
import com.ruoyi.im.vo.meeting.ImMeetingBookingVO;
import com.ruoyi.im.vo.meeting.ImMeetingRoomScheduleVO;
import com.ruoyi.im.vo.meeting.ImMeetingRoomVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 会议室管理控制器
 * 提供会议室管理、预订、签到等功能
 *
 * @author ruoyi
 */
@Tag(name = "会议室管理", description = "会议室管理、预订、签到等接口")
@RestController
@RequestMapping("/api/im/meeting-room")
public class ImMeetingRoomController {

    @Autowired
    private ImMeetingRoomService meetingRoomService;

    /**
     * 创建会议室
     * 创建新的会议室
     *
     * @param request 创建请求
     * @param userId 当前登录用户ID
     * @return 会议室ID
     */
    @Operation(summary = "创建会议室", description = "创建新的会议室")
    @PostMapping("/create")
    public Result<Long> createRoom(@Valid @RequestBody ImMeetingRoomCreateRequest request,
                                    ) {
        }
        Long roomId = meetingRoomService.createRoom(request, userId);
        return Result.success("创建成功", roomId);
    }

    /**
     * 更新会议室
     * 更新会议室信息
     *
     * @param request 更新请求
     * @param userId 当前登录用户ID
     * @return 操作结果
     */
    @Operation(summary = "更新会议室", description = "更新会议室信息")
    @PutMapping
    public Result<Void> updateRoom(@Valid @RequestBody ImMeetingRoomUpdateRequest request,
                                    ) {
        }
        meetingRoomService.updateRoom(request, userId);
        return Result.success("更新成功");
    }

    /**
     * 删除会议室
     * 删除指定会议室
     *
     * @param roomId 会议室ID
     * @param userId 当前登录用户ID
     * @return 操作结果
     */
    @Operation(summary = "删除会议室", description = "删除指定会议室")
    @DeleteMapping("/{roomId}")
    public Result<Void> deleteRoom(@PathVariable Long roomId,
                                    ) {
        }
        meetingRoomService.deleteRoom(roomId, userId);
        return Result.success("删除成功");
    }

    /**
     * 获取会议室详情
     * 查询指定会议室的详细信息
     *
     * @param roomId 会议室ID
     * @return 会议室详情
     */
    @Operation(summary = "获取会议室详情", description = "查询指定会议室的详细信息")
    @GetMapping("/{roomId}")
    public Result<ImMeetingRoomVO> getRoomDetail(@PathVariable Long roomId) {
        ImMeetingRoomVO detail = meetingRoomService.getRoomDetail(roomId);
        return Result.success(detail);
    }

    /**
     * 分页查询会议室列表
     * 按条件分页查询会议室列表
     *
     * @param request 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询会议室列表", description = "按条件分页查询会议室列表")
    @PostMapping("/page")
    public Result<IPage<ImMeetingRoomVO>> getRoomPage(@RequestBody ImMeetingRoomQueryRequest request) {
        IPage<ImMeetingRoomVO> page = meetingRoomService.getRoomPage(request);
        return Result.success(page);
    }

    /**
     * 获取可用会议室列表
     * 查询所有可用的会议室
     *
     * @return 可用会议室列表
     */
    @Operation(summary = "获取可用会议室列表", description = "查询所有可用的会议室")
    @GetMapping("/available")
    public Result<List<ImMeetingRoomVO>> getAvailableRooms() {
        List<ImMeetingRoomVO> list = meetingRoomService.getAvailableRooms();
        return Result.success(list);
    }

    /**
     * 预订会议室
     * 预订指定的会议室
     *
     * @param request 预订请求
     * @param userId 当前登录用户ID
     * @return 预订ID
     */
    @Operation(summary = "预订会议室", description = "预订指定的会议室")
    @PostMapping("/book")
    public Result<Long> bookRoom(@Valid @RequestBody ImMeetingBookingRequest request,
                                  ) {
        }
        Long bookingId = meetingRoomService.bookRoom(request, userId);
        return Result.success("预订成功", bookingId);
    }

    /**
     * 取消预订
     * 取消指定的预订
     *
     * @param bookingId 预订ID
     * @param userId 当前登录用户ID
     * @return 操作结果
     */
    @Operation(summary = "取消预订", description = "取消指定的预订")
    @PostMapping("/booking/{bookingId}/cancel")
    public Result<Void> cancelBooking(@PathVariable Long bookingId,
                                       ) {
        }
        meetingRoomService.cancelBooking(bookingId, userId);
        return Result.success("取消成功");
    }

    /**
     * 确认预订
     * 确认待确认的预订
     *
     * @param bookingId 预订ID
     * @param userId 当前登录用户ID
     * @return 操作结果
     */
    @Operation(summary = "确认预订", description = "确认待确认的预订")
    @PostMapping("/booking/{bookingId}/confirm")
    public Result<Void> confirmBooking(@PathVariable Long bookingId,
                                        ) {
        }
        meetingRoomService.confirmBooking(bookingId, userId);
        return Result.success("确认成功");
    }

    /**
     * 签到
     * 会议开始时签到
     *
     * @param bookingId 预订ID
     * @param userId 当前登录用户ID
     * @return 操作结果
     */
    @Operation(summary = "签到", description = "会议开始时签到")
    @PostMapping("/booking/{bookingId}/check-in")
    public Result<Void> checkIn(@PathVariable Long bookingId,
                                 ) {
        }
        meetingRoomService.checkIn(bookingId, userId);
        return Result.success("签到成功");
    }

    /**
     * 签退
     * 会议结束时签退
     *
     * @param bookingId 预订ID
     * @param userId 当前登录用户ID
     * @return 操作结果
     */
    @Operation(summary = "签退", description = "会议结束时签退")
    @PostMapping("/booking/{bookingId}/check-out")
    public Result<Void> checkOut(@PathVariable Long bookingId,
                                  ) {
        }
        meetingRoomService.checkOut(bookingId, userId);
        return Result.success("签退成功");
    }

    /**
     * 获取预订详情
     * 查询指定预订的详细信息
     *
     * @param bookingId 预订ID
     * @param userId 当前登录用户ID
     * @return 预订详情
     */
    @Operation(summary = "获取预订详情", description = "查询指定预订的详细信息")
    @GetMapping("/booking/{bookingId}")
    public Result<ImMeetingBookingVO> getBookingDetail(@PathVariable Long bookingId,
                                                        ) {
        }
        ImMeetingBookingVO detail = meetingRoomService.getBookingDetail(bookingId, userId);
        return Result.success(detail);
    }

    /**
     * 获取用户的预订列表
     * 查询当前用户的所有预订
     *
     * @param userId 当前登录用户ID
     * @return 预订列表
     */
    @Operation(summary = "获取用户的预订列表", description = "查询当前用户的所有预订")
    @GetMapping("/booking/my")
    public Result<List<ImMeetingBookingVO>> getMyBookings(
            ) {
        }
        List<ImMeetingBookingVO> list = meetingRoomService.getUserBookings(userId);
        return Result.success(list);
    }

    /**
     * 获取会议室日程
     * 查询指定会议室在某一天的日程安排
     *
     * @param roomId 会议室ID
     * @param date 日期（yyyy-MM-dd格式）
     * @return 日程信息
     */
    @Operation(summary = "获取会议室日程", description = "查询指定会议室在某一天的日程安排")
    @GetMapping("/{roomId}/schedule")
    public Result<ImMeetingRoomScheduleVO> getRoomSchedule(@PathVariable Long roomId,
                                                             @RequestParam String date) {
        ImMeetingRoomScheduleVO schedule = meetingRoomService.getRoomSchedule(roomId, date);
        return Result.success(schedule);
    }

    /**
     * 检查会议室可用性
     * 检查指定时间段会议室是否可用
     *
     * @param roomId 会议室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否可用
     */
    @Operation(summary = "检查会议室可用性", description = "检查指定时间段会议室是否可用")
    @GetMapping("/{roomId}/availability")
    public Result<Boolean> checkAvailability(@PathVariable Long roomId,
                                              @RequestParam String startTime,
                                              @RequestParam String endTime) {
        boolean available = meetingRoomService.checkAvailability(
                roomId,
                java.time.LocalDateTime.parse(startTime, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                java.time.LocalDateTime.parse(endTime, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        return Result.success(available);
    }

    /**
     * 提交会议反馈
     * 提交会议后的反馈和评分
     *
     * @param bookingId 预订ID
     * @param feedback 反馈内容
     * @param rating 评分（1-5分）
     * @param userId 当前登录用户ID
     * @return 操作结果
     */
    @Operation(summary = "提交会议反馈", description = "提交会议后的反馈和评分")
    @PostMapping("/booking/{bookingId}/feedback")
    public Result<Void> submitFeedback(@PathVariable Long bookingId,
                                        @RequestParam String feedback,
                                        @RequestParam Integer rating,
                                        ) {
        }
        meetingRoomService.submitFeedback(bookingId, feedback, rating, userId);
        return Result.success("提交成功");
    }

    /**
     * 获取会议室统计数据
     * 获取当前用户的会议室预订统计
     *
     * @param userId 当前登录用户ID
     * @return 统计数据
     */
    @Operation(summary = "获取会议室统计数据", description = "获取当前用户的会议室预订统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            ) {
        }
        Map<String, Object> stats = meetingRoomService.getStatistics(userId);
        return Result.success(stats);
    }
}
