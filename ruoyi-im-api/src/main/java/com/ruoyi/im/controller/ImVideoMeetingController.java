package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.meeting.ImVideoMeetingCreateRequest;
import com.ruoyi.im.dto.meeting.ImVideoMeetingUpdateRequest;
import com.ruoyi.im.service.ImVideoMeetingService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.meeting.ImVideoMeetingDetailVO;
import com.ruoyi.im.vo.meeting.ImVideoMeetingVO;
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

@RestController
@RequestMapping("/api/im/meeting")
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
    
    @PostMapping("/create")
    public Result<Map<String, Object>> createMeeting(
             @Valid @RequestBody ImVideoMeetingCreateRequest request) {
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
    
    @PutMapping("/{meetingId}")
    public Result<Void> updateMeeting(
             @PathVariable Long meetingId,
             @Valid @RequestBody ImVideoMeetingUpdateRequest request) {
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
    
    @PostMapping("/{meetingId}/cancel")
    public Result<Void> cancelMeeting(
             @PathVariable Long meetingId) {
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
    
    @DeleteMapping("/{meetingId}")
    public Result<Void> deleteMeeting(
             @PathVariable Long meetingId) {
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
    
    @PostMapping("/{meetingId}/start")
    public Result<Map<String, Object>> startMeeting(
             @PathVariable Long meetingId) {
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
    
    @PostMapping("/{meetingId}/end")
    public Result<Void> endMeeting(
             @PathVariable Long meetingId) {
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
    
    @PostMapping("/{meetingId}/join")
    public Result<ImVideoMeetingDetailVO> joinMeeting(
             @PathVariable Long meetingId,
             @RequestParam(required = false) String password) {
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
    
    @PostMapping("/{meetingId}/leave")
    public Result<Void> leaveMeeting(
             @PathVariable Long meetingId) {
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
    
    @GetMapping("/{meetingId}")
    public Result<ImVideoMeetingDetailVO> getMeetingDetail(
             @PathVariable Long meetingId) {
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
    
    @GetMapping("/list")
    public Result<List<ImVideoMeetingVO>> getMeetingList(
             @RequestParam(required = false) String status) {
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
    
    @GetMapping("/{meetingId}/participants")
    public Result<List<?>> getParticipants(
             @PathVariable Long meetingId) {
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
    
    @PostMapping("/{meetingId}/invite")
    public Result<Void> inviteUsers(
             @PathVariable Long meetingId,
             @RequestBody List<Long> userIds) {
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
    
    @PostMapping("/{meetingId}/remove")
    public Result<Void> removeParticipant(
             @PathVariable Long meetingId,
             @RequestParam Long userId) {
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
    
    @PostMapping("/{meetingId}/mute")
    public Result<Void> muteParticipant(
             @PathVariable Long meetingId,
             @RequestParam Long userId,
             @RequestParam Boolean muted) {
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
    
    @PostMapping("/{meetingId}/screen-share/start")
    public Result<Void> startScreenShare(
             @PathVariable Long meetingId) {
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
    
    @PostMapping("/{meetingId}/screen-share/stop")
    public Result<Void> stopScreenShare(
             @PathVariable Long meetingId) {
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
    
    @PostMapping("/{meetingId}/change-host")
    public Result<Void> changeHost(
             @PathVariable Long meetingId,
             @RequestParam Long newHostId) {
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

