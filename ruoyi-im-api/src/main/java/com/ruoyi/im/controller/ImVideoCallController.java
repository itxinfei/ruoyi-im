package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImVideoCallService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.websocket.ImWebSocketEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 视频通话控制器
 *
 * @author ruoyi
 */
@Tag(name = "视频通话", description = "视频通话管理接口")
@RestController
@RequestMapping("/api/im/video-call")
public class ImVideoCallController {

    private static final Logger log = LoggerFactory.getLogger(ImVideoCallController.class);

    private final ImVideoCallService videoCallService;

    /**
     * 构造器注入依赖
     *
     * @param videoCallService 视频通话服务
     */
    public ImVideoCallController(ImVideoCallService videoCallService) {
        this.videoCallService = videoCallService;
    }

    /**
     * 发起通话
     */
    @Operation(summary = "发起通话", description = "发起视频或语音通话")
    @PostMapping("/initiate")
    public Result<Long> initiateCall(
            @RequestParam Long calleeId,
            @RequestParam(required = false) Long conversationId,
            @RequestParam(defaultValue = "VIDEO") String callType) {
        Long callerId = SecurityUtils.getLoginUserId();

        try {
            Long callId = videoCallService.initiateCall(callerId, calleeId, conversationId, callType);

            // 通过WebSocket通知接收者
            ImWebSocketEndpoint.sendCallNotification(calleeId, callId, callType, callerId);

            return Result.success("发起成功", callId);
        } catch (Exception e) {
            log.error("发起通话失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 接听通话
     */
    @Operation(summary = "接听通话", description = "接听来电")
    @PostMapping("/{callId}/accept")
    public Result<Void> acceptCall(
            @PathVariable Long callId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            videoCallService.acceptCall(callId, userId);
            return Result.success("接听成功");
        } catch (Exception e) {
            log.error("接听通话失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 拒绝通话
     */
    @Operation(summary = "拒绝通话", description = "拒绝来电")
    @PostMapping("/{callId}/reject")
    public Result<Void> rejectCall(
            @PathVariable Long callId,
            @RequestParam(required = false) String reason) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            videoCallService.rejectCall(callId, userId, reason);
            return Result.success("已拒绝");
        } catch (Exception e) {
            log.error("拒绝通话失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 结束通话
     */
    @Operation(summary = "结束通话", description = "结束当前通话")
    @PostMapping("/{callId}/end")
    public Result<Void> endCall(
            @PathVariable Long callId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            videoCallService.endCall(callId, userId);
            return Result.success("通话已结束");
        } catch (Exception e) {
            log.error("结束通话失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取通话信息
     */
    @Operation(summary = "获取通话信息", description = "获取通话详情")
    @GetMapping("/{callId}")
    public Result<Object> getCallInfo(@PathVariable Long callId) {
        try {
            Object callInfo = videoCallService.getCallInfo(callId);
            if (callInfo == null) {
                return Result.fail("通话不存在或已过期");
            }
            return Result.success(callInfo);
        } catch (Exception e) {
            log.error("获取通话信息失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取用户当前通话状态
     */
    @Operation(summary = "获取用户通话状态", description = "获取用户当前正在进行的通话")
    @GetMapping("/active")
    public Result<Object> getUserActiveCall() {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            Object callInfo = videoCallService.getUserActiveCall(userId);
            return Result.success(callInfo);
        } catch (Exception e) {
            log.error("获取用户通话状态失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 发送WebRTC信令
     * 客户端通过此接口发送offer/answer/ice-candidate
     */
    @Operation(summary = "发送WebRTC信令", description = "转发WebRTC信令消息给对端")
    @PostMapping("/signal")
    public Result<Void> sendSignal(
            @RequestParam Long callId,
            @RequestParam String signalType,
            @RequestBody String signalData) {
        Long fromUserId = SecurityUtils.getLoginUserId();

        try {
            // 通过WebSocket转发信令消息
            ImWebSocketEndpoint.sendWebRTCSignal(callId, fromUserId, signalType, signalData);
            return Result.success("发送成功");
        } catch (Exception e) {
            log.error("发送信令失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取通话历史
     */
    @Operation(summary = "获取通话历史", description = "获取用户通话历史记录")
    @GetMapping("/history")
    public Result<?> getCallHistory(
            @RequestParam(defaultValue = "20") Integer limit) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            return Result.success(videoCallService.getCallHistory(userId, limit));
        } catch (Exception e) {
            log.error("获取通话历史失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    // ==================== 群组多人通话接口 ====================

    /**
     * 发起群组多人通话
     */
    @Operation(summary = "发起群组通话", description = "发起群组多人视频/语音通话（最多9人）")
    @PostMapping("/group/initiate")
    public Result<Map<String, Object>> initiateGroupCall(
            @RequestParam Long conversationId,
            @RequestParam(defaultValue = "VIDEO") String callType,
            @RequestParam(defaultValue = "9") Integer maxParticipants,
            @RequestBody List<Long> invitedUserIds) {
        Long callerId = SecurityUtils.getLoginUserId();

        try {
            // 验证最大参与者数
            if (maxParticipants > 9) {
                return Result.fail("最多支持9人同时通话");
            }
            if (invitedUserIds == null || invitedUserIds.isEmpty()) {
                return Result.fail("请邀请至少一人参与通话");
            }
            if (invitedUserIds.size() + 1 > maxParticipants) {
                return Result.fail("邀请人数超过最大参与者数限制");
            }

            Long callId = videoCallService.initiateGroupCall(
                callerId, conversationId, callType, maxParticipants, invitedUserIds);

            // 生成房间信息
            Map<String, Object> roomInfo = new java.util.HashMap<>();
            roomInfo.put("callId", callId);
            roomInfo.put("roomId", "call_" + callId);
            roomInfo.put("maxParticipants", maxParticipants);
            roomInfo.put("callType", callType);

            // 通过WebSocket通知所有被邀请用户
            for (Long userId : invitedUserIds) {
                ImWebSocketEndpoint.sendCallNotification(userId, callId, callType, callerId);
            }

            return Result.success("发起成功", roomInfo);
        } catch (Exception e) {
            log.error("发起群组通话失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 加入群组通话
     */
    @Operation(summary = "加入群组通话", description = "加入已发起的群组通话")
    @PostMapping("/group/{callId}/join")
    public Result<Void> joinGroupCall(
            @PathVariable Long callId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            videoCallService.joinGroupCall(callId, userId);
            return Result.success("加入成功");
        } catch (Exception e) {
            log.error("加入群组通话失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 离开群组通话
     */
    @Operation(summary = "离开群组通话", description = "离开群组通话（不结束通话）")
    @PostMapping("/group/{callId}/leave")
    public Result<Void> leaveGroupCall(
            @PathVariable Long callId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            videoCallService.leaveGroupCall(callId, userId);
            return Result.success("已离开");
        } catch (Exception e) {
            log.error("离开群组通话失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取通话参与者列表
     */
    @Operation(summary = "获取参与者列表", description = "获取群组通话的参与者列表")
    @GetMapping("/group/{callId}/participants")
    public Result<List<?>> getParticipants(
            @PathVariable Long callId) {
        try {
            List<?> participants = videoCallService.getCallParticipants(callId);
            return Result.success(participants);
        } catch (Exception e) {
            log.error("获取参与者列表失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 切换麦克风状态
     */
    @Operation(summary = "切换麦克风", description = "开启/关闭麦克风")
    @PostMapping("/group/{callId}/mute")
    public Result<Void> toggleMute(
            @PathVariable Long callId,
            @RequestParam Boolean muted) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            videoCallService.toggleMute(callId, userId, muted);
            return Result.success(muted ? "已静音" : "已取消静音");
        } catch (Exception e) {
            log.error("切换麦克风状态失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 切换摄像头状态
     */
    @Operation(summary = "切换摄像头", description = "开启/关闭摄像头")
    @PostMapping("/group/{callId}/camera")
    public Result<Void> toggleCamera(
            @PathVariable Long callId,
            @RequestParam Boolean cameraOff) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            videoCallService.toggleCamera(callId, userId, cameraOff);
            return Result.success(cameraOff ? "已关闭摄像头" : "已开启摄像头");
        } catch (Exception e) {
            log.error("切换摄像头状态失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }
}
