package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImVideoCallService;
import com.ruoyi.im.websocket.ImWebSocketEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private ImVideoCallService videoCallService;

    /**
     * 发起通话
     */
    @Operation(summary = "发起通话", description = "发起视频或语音通话")
    @PostMapping("/initiate")
    public Result<Long> initiateCall(
            @RequestParam Long calleeId,
            @RequestParam(required = false) Long conversationId,
            @RequestParam(defaultValue = "VIDEO") String callType,
            @RequestHeader(value = "userId", required = false) Long callerId) {
        if (callerId == null) {
            callerId = 1L;
        }

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
            @PathVariable Long callId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }

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
            @RequestParam(required = false) String reason,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }

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
            @PathVariable Long callId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }

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
    public Result<Object> getUserActiveCall(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }

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
            @RequestBody String signalData,
            @RequestHeader(value = "userId", required = false) Long fromUserId) {
        if (fromUserId == null) {
            fromUserId = 1L;
        }

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
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }

        try {
            // 这里可以调用Mapper获取历史记录
            return Result.success("获取成功");
        } catch (Exception e) {
            log.error("获取通话历史失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }
}
