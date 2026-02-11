package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.constants.StatusConstants;
import com.ruoyi.im.dto.videocall.VideoCallInitiateRequest;
import com.ruoyi.im.dto.videocall.VideoCallInitiateGroupRequest;
import com.ruoyi.im.dto.videocall.VideoCallRejectRequest;
import com.ruoyi.im.dto.videocall.VideoCallSignalRequest;
import com.ruoyi.im.dto.videocall.VideoCallToggleRequest;
import com.ruoyi.im.service.ImVideoCallService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.websocket.ImWebSocketEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Map;

/**
 * 视频通话控制器
 *
 * @author ruoyi
 */
@Tag(name = "视频通话", description = "视频通话管理接口")
@RestController
@RequestMapping("/api/im/video-calls")
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
    public Result<Long> initiateCall(@Valid @RequestBody VideoCallInitiateRequest request) {
        Long callerId = SecurityUtils.getLoginUserId();

        Long callId = videoCallService.initiateCall(callerId, request.getCalleeId(),
                request.getConversationId(), request.getCallType());

        // 通过WebSocket通知接收者
        ImWebSocketEndpoint.sendCallNotification(request.getCalleeId(), callId, request.getCallType(), callerId);

        return Result.success("发起成功", callId);
    }

    /**
     * 接听通话
     */
    @Operation(summary = "接听通话", description = "接听来电")
    @PostMapping("/{callId}/accept")
    public Result<Void> acceptCall(
            @PathVariable @NotNull(message = "通话ID不能为空") @Positive(message = "通话ID必须为正数") Long callId) {
        Long userId = SecurityUtils.getLoginUserId();
        videoCallService.acceptCall(callId, userId);
        return Result.success("接听成功");
    }

    /**
     * 拒绝通话
     */
    @Operation(summary = "拒绝通话", description = "拒绝来电")
    @PostMapping("/{callId}/reject")
    public Result<Void> rejectCall(
            @PathVariable @NotNull(message = "通话ID不能为空") @Positive(message = "通话ID必须为正数") Long callId,
            @RequestBody(required = false) VideoCallRejectRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        String reason = request != null ? request.getReason() : null;
        videoCallService.rejectCall(callId, userId, reason);
        return Result.success("已拒绝");
    }

    /**
     * 结束通话
     */
    @Operation(summary = "结束通话", description = "结束当前通话")
    @PostMapping("/{callId}/end")
    public Result<Void> endCall(
            @PathVariable @NotNull(message = "通话ID不能为空") @Positive(message = "通话ID必须为正数") Long callId) {
        Long userId = SecurityUtils.getLoginUserId();
        videoCallService.endCall(callId, userId);
        return Result.success("通话已结束");
    }

    /**
     * 获取通话信息
     */
    @Operation(summary = "获取通话信息", description = "获取通话详情")
    @GetMapping("/{callId}")
    public Result<Object> getCallInfo(@PathVariable @NotNull(message = "通话ID不能为空") @Positive(message = "通话ID必须为正数") Long callId) {
        Object callInfo = videoCallService.getCallInfo(callId);
        if (callInfo == null) {
            return Result.fail("通话不存在或已过期");
        }
        return Result.success(callInfo);
    }

    /**
     * 获取用户当前通话状态
     */
    @Operation(summary = "获取用户通话状态", description = "获取用户当前正在进行的通话")
    @GetMapping("/active")
    public Result<Object> getUserActiveCall() {
        Long userId = SecurityUtils.getLoginUserId();
        Object callInfo = videoCallService.getUserActiveCall(userId);
        return Result.success(callInfo);
    }

    /**
     * 发送WebRTC信令
     * 客户端通过此接口发送offer/answer/ice-candidate
     */
    @Operation(summary = "发送WebRTC信令", description = "转发WebRTC信令消息给对端")
    @PostMapping("/signal")
    public Result<Void> sendSignal(@Valid @RequestBody VideoCallSignalRequest request) {
        Long fromUserId = SecurityUtils.getLoginUserId();

        // 通过WebSocket转发信令消息
        ImWebSocketEndpoint.sendWebRTCSignal(request.getCallId(), fromUserId, request.getSignalType(), request.getSignalData());
        return Result.success("发送成功");
    }

    /**
     * 获取通话历史
     */
    @Operation(summary = "获取通话历史", description = "获取用户通话历史记录")
    @GetMapping("/history")
    public Result<?> getCallHistory(
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "最少查询1条记录") Integer limit) {
        Long userId = SecurityUtils.getLoginUserId();
        return Result.success(videoCallService.getCallHistory(userId, limit));
    }

    // ==================== 群组多人通话接口 ====================

    /**
     * 发起群组多人通话
     */
    @Operation(summary = "发起群组通话", description = "发起群组多人视频/语音通话（最多9人）")
    @PostMapping("/group/initiate")
    public Result<Map<String, Object>> initiateGroupCall(@Valid @RequestBody VideoCallInitiateGroupRequest request) {
        Long callerId = SecurityUtils.getLoginUserId();

        Long callId = videoCallService.initiateGroupCall(
                callerId, request.getConversationId(), request.getCallType(),
                request.getMaxParticipants(), request.getInvitedUserIds());

        // 生成房间信息
        Map<String, Object> roomInfo = new java.util.HashMap<>();
        roomInfo.put("callId", callId);
        roomInfo.put("roomId", "call_" + callId);
        roomInfo.put("maxParticipants", request.getMaxParticipants());
        roomInfo.put("callType", request.getCallType());

        // 通过WebSocket通知所有被邀请用户
        for (Long userId : request.getInvitedUserIds()) {
            ImWebSocketEndpoint.sendCallNotification(userId, callId, request.getCallType(), callerId);
        }

        return Result.success("发起成功", roomInfo);
    }

    /**
     * 加入群组通话
     */
    @Operation(summary = "加入群组通话", description = "加入已发起的群组通话")
    @PostMapping("/group/{callId}/join")
    public Result<Void> joinGroupCall(
            @PathVariable @NotNull(message = "通话ID不能为空") @Positive(message = "通话ID必须为正数") Long callId) {
        Long userId = SecurityUtils.getLoginUserId();
        videoCallService.joinGroupCall(callId, userId);
        return Result.success("加入成功");
    }

    /**
     * 离开群组通话
     */
    @Operation(summary = "离开群组通话", description = "离开群组通话（不结束通话）")
    @PostMapping("/group/{callId}/leave")
    public Result<Void> leaveGroupCall(
            @PathVariable @NotNull(message = "通话ID不能为空") @Positive(message = "通话ID必须为正数") Long callId) {
        Long userId = SecurityUtils.getLoginUserId();
        videoCallService.leaveGroupCall(callId, userId);
        return Result.success("已离开");
    }

    /**
     * 获取通话参与者列表
     */
    @Operation(summary = "获取参与者列表", description = "获取群组通话的参与者列表")
    @GetMapping("/group/{callId}/participants")
    public Result<List<?>> getParticipants(
            @PathVariable @NotNull(message = "通话ID不能为空") @Positive(message = "通话ID必须为正数") Long callId) {
        List<?> participants = videoCallService.getCallParticipants(callId);
        return Result.success(participants);
    }

    /**
     * 切换麦克风状态
     */
    @Operation(summary = "切换麦克风", description = "开启/关闭麦克风")
    @PostMapping("/group/{callId}/mute")
    public Result<Void> toggleMute(
            @PathVariable @NotNull(message = "通话ID不能为空") @Positive(message = "通话ID必须为正数") Long callId,
            @Valid @RequestBody VideoCallToggleRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        videoCallService.toggleMute(callId, userId, request.getEnabled());
        return Result.success(request.getEnabled() ? "已静音" : "已取消静音");
    }

    /**
     * 切换摄像头状态
     */
    @Operation(summary = "切换摄像头", description = "开启/关闭摄像头")
    @PostMapping("/group/{callId}/camera")
    public Result<Void> toggleCamera(
            @PathVariable @NotNull(message = "通话ID不能为空") @Positive(message = "通话ID必须为正数") Long callId,
            @Valid @RequestBody VideoCallToggleRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        videoCallService.toggleCamera(callId, userId, request.getEnabled());
        return Result.success(request.getEnabled() ? "已关闭摄像头" : "已开启摄像头");
    }
}
