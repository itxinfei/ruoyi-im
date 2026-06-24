package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImVideoCallService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.websocket.ImWebSocketEndpoint;
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

@RestController
@RequestMapping("/api/im/video-call")
public class ImVideoCallController {

    private static final Logger log = LoggerFactory.getLogger(ImVideoCallController.class);

    private static final String DEFAULT_CALL_TYPE = "VIDEO";
    private static final String CALL_ID_PREFIX = "call-";
    private static final int MAX_PARTICIPANTS = 9;

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
    
    @PostMapping("/initiate")
    public Result<Long> initiateCall(
            @RequestParam Long calleeId,
            @RequestParam(required = false) Long conversationId,
            @RequestParam(defaultValue = DEFAULT_CALL_TYPE) String callType) {
        Long callerId = SecurityUtils.getLoginUserId();

        try {
            Long callId = videoCallService.initiateCall(callerId, calleeId, conversationId, callType);

            // 通过WebSocket通知接收者
            ImWebSocketEndpoint.sendCallNotification(calleeId, callId, callType, callerId);

            return Result.success("发起成功", callId);
        } catch (Exception e) {
            log.error("发起通话失败", e);
            return Result.fail("发起通话失败");
        }
    }

    /**
     * 接听通话
     */
    
    @PostMapping("/{callId}/accept")
    public Result<Void> acceptCall(
            @PathVariable String callId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            Long parsedCallId = parseCallId(callId);
            videoCallService.acceptCall(parsedCallId, userId);
            return Result.success("接听成功");
        } catch (Exception e) {
            log.error("接听通话失败", e);
            return Result.fail("接听通话失败");
        }
    }

    /**
     * 拒绝通话
     */
    
    @PostMapping("/{callId}/reject")
    public Result<Void> rejectCall(
            @PathVariable String callId,
            @RequestParam(required = false) String reason) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            Long parsedCallId = parseCallId(callId);
            videoCallService.rejectCall(parsedCallId, userId, reason);
            return Result.success("已拒绝");
        } catch (Exception e) {
            log.error("拒绝通话失败", e);
            return Result.fail("拒绝通话失败");
        }
    }

    /**
     * 结束通话
     */
    
    @PostMapping("/{callId}/end")
    public Result<Void> endCall(
            @PathVariable String callId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            // 处理前端临时生成的callId格式 (call-1776910478925)
            Long parsedCallId = parseCallId(callId);
            videoCallService.endCall(parsedCallId, userId);
            return Result.success("通话已结束");
        } catch (Exception e) {
            log.error("结束通话失败", e);
            return Result.fail("结束通话失败");
        }
    }
    
    /**
     * 解析通话ID，处理前端临时生成的格式
     */
    private Long parseCallId(String callId) {
        try {
            // 如果是数字字符串，直接转换
            if (callId.matches("\\d+")) {
                return Long.parseLong(callId);
            }
            // 如果是前端临时生成的格式 (call-1776910478925)
            else if (callId.startsWith(CALL_ID_PREFIX)) {
                String numericPart = callId.substring(CALL_ID_PREFIX.length());
                return Long.parseLong(numericPart);
            }
            throw new NumberFormatException("Invalid callId format");
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid callId: " + callId, e);
        }
    }

    /**
     * 获取通话信息
     */
    
    @GetMapping("/{callId}")
    public Result<Object> getCallInfo(@PathVariable String callId) {
        try {
            Long parsedCallId = parseCallId(callId);
            Object callInfo = videoCallService.getCallInfo(parsedCallId);
            if (callInfo == null) {
                return Result.fail("通话不存在或已过期");
            }
            return Result.success(callInfo);
        } catch (Exception e) {
            log.error("获取通话信息失败", e);
            return Result.fail("获取通话信息失败");
        }
    }

    /**
     * 获取用户当前通话状态
     */
    
    @GetMapping("/active")
    public Result<Object> getUserActiveCall() {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            Object callInfo = videoCallService.getUserActiveCall(userId);
            return Result.success(callInfo);
        } catch (Exception e) {
            log.error("获取用户通话状态失败", e);
            return Result.fail("获取用户通话状态失败");
        }
    }

    /**
     * 发送WebRTC信令
     * 客户端通过此接口发送offer/answer/ice-candidate
     */
    
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
            log.error("发送信令失败", e);
            return Result.fail("发送信令失败");
        }
    }

    /**
     * 获取通话历史
     */
    
    @GetMapping("/history")
    public Result<?> getCallHistory(
            @RequestParam(defaultValue = "20") Integer limit) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            return Result.success(videoCallService.getCallHistory(userId, limit));
        } catch (Exception e) {
            log.error("获取通话历史失败", e);
            return Result.fail("获取通话历史失败");
        }
    }

    // ==================== 群组多人通话接口 ====================

    /**
     * 发起群组多人通话
     */
    
    @PostMapping("/group/initiate")
    public Result<Map<String, Object>> initiateGroupCall(
            @RequestParam Long conversationId,
            @RequestParam(defaultValue = DEFAULT_CALL_TYPE) String callType,
            @RequestParam(defaultValue = MAX_PARTICIPANTS + "") Integer maxParticipants,
            @RequestBody List<Long> invitedUserIds) {
        Long callerId = SecurityUtils.getLoginUserId();

        try {
            // 验证最大参与者数
            if (maxParticipants > MAX_PARTICIPANTS) {
                return Result.fail("最多支持" + MAX_PARTICIPANTS + "人同时通话");
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
            log.error("发起群组通话失败", e);
            return Result.fail("发起群组通话失败");
        }
    }

    /**
     * 加入群组通话
     */
    
    @PostMapping("/group/{callId}/join")
    public Result<Void> joinGroupCall(
            @PathVariable String callId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            Long parsedCallId = parseCallId(callId);
            videoCallService.joinGroupCall(parsedCallId, userId);
            return Result.success("加入成功");
        } catch (Exception e) {
            log.error("加入群组通话失败", e);
            return Result.fail("加入群组通话失败");
        }
    }

    /**
     * 离开群组通话
     */
    
    @PostMapping("/group/{callId}/leave")
    public Result<Void> leaveGroupCall(
            @PathVariable String callId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            Long parsedCallId = parseCallId(callId);
            videoCallService.leaveGroupCall(parsedCallId, userId);
            return Result.success("已离开");
        } catch (Exception e) {
            log.error("离开群组通话失败", e);
            return Result.fail("离开群组通话失败");
        }
    }

    /**
     * 获取通话参与者列表
     */
    
    @GetMapping("/group/{callId}/participants")
    public Result<List<?>> getParticipants(
            @PathVariable String callId) {
        try {
            Long parsedCallId = parseCallId(callId);
            List<?> participants = videoCallService.getCallParticipants(parsedCallId);
            return Result.success(participants);
        } catch (Exception e) {
            log.error("获取参与者列表失败", e);
            return Result.fail("获取参与者列表失败");
        }
    }

    /**
     * 切换麦克风状态
     */
    
    @PostMapping("/group/{callId}/mute")
    public Result<Void> toggleMute(
            @PathVariable String callId,
            @RequestParam Boolean muted) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            Long parsedCallId = parseCallId(callId);
            videoCallService.toggleMute(parsedCallId, userId, muted);
            return Result.success(muted ? "已静音" : "已取消静音");
        } catch (Exception e) {
            log.error("切换麦克风状态失败", e);
            return Result.fail("切换麦克风状态失败");
        }
    }

    /**
     * 切换摄像头状态
     */
    
    @PostMapping("/group/{callId}/camera")
    public Result<Void> toggleCamera(
            @PathVariable String callId,
            @RequestParam Boolean cameraOff) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            Long parsedCallId = parseCallId(callId);
            videoCallService.toggleCamera(parsedCallId, userId, cameraOff);
            return Result.success(cameraOff ? "已关闭摄像头" : "已开启摄像头");
        } catch (Exception e) {
            log.error("切换摄像头状态失败", e);
            return Result.fail("切换摄像头状态失败");
        }
    }
}

