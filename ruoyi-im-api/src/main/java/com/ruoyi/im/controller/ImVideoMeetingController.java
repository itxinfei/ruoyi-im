package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.meeting.ImVideoMeetingCreateRequest;
import com.ruoyi.im.dto.meeting.ImVideoMeetingUpdateRequest;
import com.ruoyi.im.service.ImVideoMeetingService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.meeting.ImVideoMeetingDetailVO;
import com.ruoyi.im.vo.meeting.ImVideoMeetingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 视频会议控制器
 * 提供视频会议创建、管理、参与等功能
 *
 * @author ruoyi
 */
@Tag(name = "视频会议", description = "视频会议管理接口")
@RestController
@RequestMapping("/api/im/video-meetings")
public class ImVideoMeetingController {

    private static final Logger log = LoggerFactory.getLogger(ImVideoMeetingController.class);

    private final ImVideoMeetingService videoMeetingService;

    /**
     * 构造器注入依赖
     *
     * @param videoMeetingService 视频会议服务
     */
    public ImVideoMeetingController(ImVideoMeetingService videoMeetingService) {
        this.videoMeetingService = videoMeetingService;
    }

    /**
     * 创建会议
     */
    @Operation(summary = "创建会议", description = "创建新的视频会议")
    @PostMapping("/create")
    public Result<Map<String, Object>> createMeeting(
            @Parameter(description = "会议创建请求") @Valid @RequestBody ImVideoMeetingCreateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            Long meetingId = videoMeetingService.createMeeting(request, userId);

            // 获取会议详情
            ImVideoMeetingDetailVO meeting = videoMeetingService.getMeetingDetail(meetingId);

            Map<String, Object> result = new java.util.HashMap<>();
            result.put("meetingId", meetingId);
            result.put("meetingLink", meeting != null ? meeting.getMeetingLink() : null);
            result.put("roomId", meeting != null ? meeting.getRoomId() : null);

            return Result.success("创建成功", result);
        } catch (Exception e) {
            log.error("创建会议失败: userId={}", userId, e);
            return Result.fail("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新会议
     */
    @Operation(summary = "更新会议", description = "更新会议信息")
    @PutMapping("/{meetingId}")
    public Result<Void> updateMeeting(
            @Parameter(description = "会议ID") @PathVariable Long meetingId,
            @Parameter(description = "会议更新请求") @Valid @RequestBody ImVideoMeetingUpdateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            videoMeetingService.updateMeeting(meetingId, request, userId);
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("更新会议失败: meetingId={}, userId={}", meetingId, userId, e);
            return Result.fail("更新失败: " + e.getMessage());
        }
    }

    /**
     * 取消会议
     */
    @Operation(summary = "取消会议", description = "取消已预定的会议")
    @PostMapping("/{meetingId}/cancel")
    public Result<Void> cancelMeeting(
            @Parameter(description = "会议ID") @PathVariable Long meetingId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            videoMeetingService.cancelMeeting(meetingId, userId);
            return Result.success("会议已取消");
        } catch (Exception e) {
            log.error("取消会议失败: meetingId={}, userId={}", meetingId, userId, e);
            return Result.fail("取消失败: " + e.getMessage());
        }
    }

    /**
     * 删除会议
     */
    @Operation(summary = "删除会议", description = "删除已结束的会议")
    @DeleteMapping("/{meetingId}")
    public Result<Void> deleteMeeting(
            @Parameter(description = "会议ID") @PathVariable Long meetingId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            videoMeetingService.deleteMeeting(meetingId, userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除会议失败: meetingId={}, userId={}", meetingId, userId, e);
            return Result.fail("删除失败: " + e.getMessage());
        }
    }

    /**
     * 开始会议
     */
    @Operation(summary = "开始会议", description = "开始预定的会议")
    @PostMapping("/{meetingId}/start")
    public Result<Map<String, Object>> startMeeting(
            @Parameter(description = "会议ID") @PathVariable Long meetingId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            videoMeetingService.startMeeting(meetingId, userId);

            Map<String, Object> result = new java.util.HashMap<>();
            result.put("meetingId", meetingId);
            result.put("status", "IN_PROGRESS");
            result.put("roomId", "meeting_" + meetingId);

            return Result.success("会议已开始", result);
        } catch (Exception e) {
            log.error("开始会议失败: meetingId={}, userId={}", meetingId, userId, e);
            return Result.fail("开始失败: " + e.getMessage());
        }
    }

    /**
     * 结束会议
     */
    @Operation(summary = "结束会议", description = "结束当前进行的会议")
    @PostMapping("/{meetingId}/end")
    public Result<Void> endMeeting(
            @Parameter(description = "会议ID") @PathVariable Long meetingId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            videoMeetingService.endMeeting(meetingId, userId);
            return Result.success("会议已结束");
        } catch (Exception e) {
            log.error("结束会议失败: meetingId={}, userId={}", meetingId, userId, e);
            return Result.fail("结束失败: " + e.getMessage());
        }
    }

    /**
     * 加入会议
     */
    @Operation(summary = "加入会议", description = "加入指定的会议")
    @PostMapping("/{meetingId}/join")
    public Result<ImVideoMeetingDetailVO> joinMeeting(
            @Parameter(description = "会议ID") @PathVariable Long meetingId,
            @Parameter(description = "会议密码（如需要）") @RequestParam(required = false) String password) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            ImVideoMeetingDetailVO meeting = videoMeetingService.joinMeeting(meetingId, password, userId);
            return Result.success("加入成功", meeting);
        } catch (Exception e) {
            log.error("加入会议失败: meetingId={}, userId={}", meetingId, userId, e);
            return Result.fail("加入失败: " + e.getMessage());
        }
    }

    /**
     * 离开会议
     */
    @Operation(summary = "离开会议", description = "离开当前会议")
    @PostMapping("/{meetingId}/leave")
    public Result<Void> leaveMeeting(
            @Parameter(description = "会议ID") @PathVariable Long meetingId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            videoMeetingService.leaveMeeting(meetingId, userId);
            return Result.success("已离开会议");
        } catch (Exception e) {
            log.error("离开会议失败: meetingId={}, userId={}", meetingId, userId, e);
            return Result.fail("离开失败: " + e.getMessage());
        }
    }

    /**
     * 获取会议详情
     */
    @Operation(summary = "获取会议详情", description = "获取会议详细信息")
    @GetMapping("/{meetingId}")
    public Result<ImVideoMeetingDetailVO> getMeetingDetail(
            @Parameter(description = "会议ID") @PathVariable Long meetingId) {
        try {
            ImVideoMeetingDetailVO meeting = videoMeetingService.getMeetingDetail(meetingId);
            if (meeting == null) {
                return Result.fail("会议不存在");
            }
            return Result.success(meeting);
        } catch (Exception e) {
            log.error("获取会议详情失败: meetingId={}", meetingId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的会议列表
     */
    @Operation(summary = "获取会议列表", description = "获取当前用户的会议列表")
    @GetMapping("/list")
    public Result<List<ImVideoMeetingVO>> getMeetingList(
            @Parameter(description = "会议状态：SCHEDULED预定中, IN_PROGRESS进行中, COMPLETED已结束, CANCELLED已取消") @RequestParam(required = false) String status) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            List<ImVideoMeetingVO> meetings = videoMeetingService.getUserMeetings(userId, status);
            return Result.success(meetings);
        } catch (Exception e) {
            log.error("获取会议列表失败: userId={}", userId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取会议参与者列表
     */
    @Operation(summary = "获取参与者列表", description = "获取会议的参与者列表")
    @GetMapping("/{meetingId}/participants")
    public Result<List<?>> getParticipants(
            @Parameter(description = "会议ID") @PathVariable Long meetingId) {
        try {
            List<?> participants = videoMeetingService.getMeetingParticipants(meetingId);
            return Result.success(participants);
        } catch (Exception e) {
            log.error("获取参与者列表失败: meetingId={}", meetingId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 邀请用户加入会议
     */
    @Operation(summary = "邀请用户", description = "邀请用户加入会议")
    @PostMapping("/{meetingId}/invite")
    public Result<Void> inviteUsers(
            @Parameter(description = "会议ID") @PathVariable Long meetingId,
            @Parameter(description = "被邀请用户ID列表") @RequestBody List<Long> userIds) {
        Long inviterId = SecurityUtils.getLoginUserId();

        try {
            videoMeetingService.inviteUsers(meetingId, userIds, inviterId);
            return Result.success("邀请成功");
        } catch (Exception e) {
            log.error("邀请用户失败: meetingId={}", meetingId, e);
            return Result.fail("邀请失败: " + e.getMessage());
        }
    }

    /**
     * 移除参与者
     */
    @Operation(summary = "移除参与者", description = "将用户移出会议")
    @PostMapping("/{meetingId}/remove")
    public Result<Void> removeParticipant(
            @Parameter(description = "会议ID") @PathVariable Long meetingId,
            @Parameter(description = "被移除的用户ID") @RequestParam Long userId) {
        Long operatorId = SecurityUtils.getLoginUserId();

        try {
            videoMeetingService.removeParticipant(meetingId, userId, operatorId);
            return Result.success("已移除");
        } catch (Exception e) {
            log.error("移除参与者失败: meetingId={}, userId={}", meetingId, userId, e);
            return Result.fail("移除失败: " + e.getMessage());
        }
    }

    /**
     * 静音/取消静音
     */
    @Operation(summary = "静音/取消静音", description = "设置参与者静音状态")
    @PostMapping("/{meetingId}/mute")
    public Result<Void> muteParticipant(
            @Parameter(description = "会议ID") @PathVariable Long meetingId,
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "是否静音：true静音，false取消静音") @RequestParam Boolean muted) {
        try {
            videoMeetingService.muteParticipant(meetingId, userId, muted);
            return Result.success(muted ? "已静音" : "已取消静音");
        } catch (Exception e) {
            log.error("设置静音状态失败: meetingId={}, userId={}", meetingId, userId, e);
            return Result.fail("操作失败: " + e.getMessage());
        }
    }

    /**
     * 开始屏幕共享
     */
    @Operation(summary = "开始屏幕共享", description = "开始共享屏幕")
    @PostMapping("/{meetingId}/screen-share/start")
    public Result<Void> startScreenShare(
            @Parameter(description = "会议ID") @PathVariable Long meetingId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            videoMeetingService.startScreenShare(meetingId, userId);
            return Result.success("已开始屏幕共享");
        } catch (Exception e) {
            log.error("开始屏幕共享失败: meetingId={}, userId={}", meetingId, userId, e);
            return Result.fail("操作失败: " + e.getMessage());
        }
    }

    /**
     * 停止屏幕共享
     */
    @Operation(summary = "停止屏幕共享", description = "停止共享屏幕")
    @PostMapping("/{meetingId}/screen-share/stop")
    public Result<Void> stopScreenShare(
            @Parameter(description = "会议ID") @PathVariable Long meetingId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            videoMeetingService.stopScreenShare(meetingId, userId);
            return Result.success("已停止屏幕共享");
        } catch (Exception e) {
            log.error("停止屏幕共享失败: meetingId={}, userId={}", meetingId, userId, e);
            return Result.fail("操作失败: " + e.getMessage());
        }
    }

    /**
     * 转移主持人
     */
    @Operation(summary = "转移主持人", description = "将主持人权限转移给其他用户")
    @PostMapping("/{meetingId}/change-host")
    public Result<Void> changeHost(
            @Parameter(description = "会议ID") @PathVariable Long meetingId,
            @Parameter(description = "新主持人用户ID") @RequestParam Long newHostId) {
        Long operatorId = SecurityUtils.getLoginUserId();

        try {
            videoMeetingService.changeHost(meetingId, newHostId, operatorId);
            return Result.success("主持人已转移");
        } catch (Exception e) {
            log.error("转移主持人失败: meetingId={}, newHostId={}", meetingId, newHostId, e);
            return Result.fail("转移失败: " + e.getMessage());
        }
    }
}
