package com.ruoyi.im.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.schedule.ScheduleEventCreateRequest;
import com.ruoyi.im.dto.schedule.ScheduleEventQueryRequest;
import com.ruoyi.im.service.ImScheduleEventService;
import com.ruoyi.im.vo.schedule.ScheduleEventDetailVO;
import com.ruoyi.im.vo.schedule.ScheduleParticipantVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 日程管理控制器
 */
@Tag(name = "日程管理", description = "日程管理接口")
@RestController
@RequestMapping("/api/im/schedule")
public class ImScheduleEventController {

    @Autowired
    private ImScheduleEventService scheduleEventService;

    /**
     * 创建日程
     */
    @Operation(summary = "创建日程")
    @PostMapping
    public Result<Long> createEvent(
            @Valid @RequestBody ScheduleEventCreateRequest request,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 2L;
        }
        Long eventId = scheduleEventService.createEvent(request, userId);
        return Result.success("创建成功", eventId);
    }

    /**
     * 更新日程
     */
    @Operation(summary = "更新日程")
    @PutMapping("/{eventId}")
    public Result<Void> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody ScheduleEventCreateRequest request,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 2L;
        }
        scheduleEventService.updateEvent(eventId, request, userId);
        return Result.success("更新成功");
    }

    /**
     * 删除日程
     */
    @Operation(summary = "删除日程")
    @DeleteMapping("/{eventId}")
    public Result<Void> deleteEvent(
            @PathVariable Long eventId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 2L;
        }
        scheduleEventService.deleteEvent(eventId, userId);
        return Result.success("删除成功");
    }

    /**
     * 获取日程详情
     */
    @Operation(summary = "获取日程详情")
    @GetMapping("/{eventId}")
    public Result<ScheduleEventDetailVO> getEventDetail(
            @PathVariable Long eventId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 2L;
        }
        ScheduleEventDetailVO detail = scheduleEventService.getEventDetail(eventId, userId);
        return Result.success(detail);
    }

    /**
     * 分页查询日程列表
     */
    @Operation(summary = "分页查询日程列表")
    @PostMapping("/page")
    public Result<IPage<ScheduleEventDetailVO>> getEventPage(
            @RequestBody ScheduleEventQueryRequest request,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 2L;
        }
        IPage<ScheduleEventDetailVO> page = scheduleEventService.getEventPage(request, userId);
        return Result.success(page);
    }

    /**
     * 获取指定时间范围内的日程
     */
    @Operation(summary = "获取指定时间范围内的日程")
    @GetMapping("/range")
    public Result<List<ScheduleEventDetailVO>> getEventsByTimeRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 2L;
        }
        List<ScheduleEventDetailVO> events = scheduleEventService.getEventsByTimeRange(startTime, endTime, userId);
        return Result.success(events);
    }

    /**
     * 回复参与邀请
     */
    @Operation(summary = "回复参与邀请")
    @PutMapping("/{eventId}/respond")
    public Result<Void> respondToInvite(
            @PathVariable Long eventId,
            @RequestParam Boolean accepted,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 2L;
        }
        scheduleEventService.respondToInvite(eventId, userId, accepted);
        return Result.success(accepted ? "已接受" : "已拒绝");
    }

    /**
     * 获取参与人列表
     */
    @Operation(summary = "获取参与人列表")
    @GetMapping("/{eventId}/participants")
    public Result<List<ScheduleParticipantVO>> getParticipants(@PathVariable Long eventId) {
        List<ScheduleParticipantVO> participants = scheduleEventService.getParticipants(eventId);
        return Result.success(participants);
    }

    /**
     * 取消日程
     */
    @Operation(summary = "取消日程")
    @PutMapping("/{eventId}/cancel")
    public Result<Void> cancelEvent(
            @PathVariable Long eventId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 2L;
        }
        scheduleEventService.cancelEvent(eventId, userId);
        return Result.success("已取消");
    }
}
