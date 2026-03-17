package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.constant.ImErrorCode;
import com.ruoyi.im.constant.SystemConstants;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.domain.ImVideoCall;
import com.ruoyi.im.domain.ImVideoCallParticipant;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.mapper.ImVideoCallMapper;
import com.ruoyi.im.mapper.ImVideoCallParticipantMapper;
import com.ruoyi.im.service.ImVideoCallService;
import com.ruoyi.im.util.BusinessExceptionHelper;
import com.ruoyi.im.util.ImRedisUtil;
import com.ruoyi.im.websocket.ImWebSocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 视频通话服务实现
 *
 * @author ruoyi
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ImVideoCallServiceImpl implements ImVideoCallService {

    private static final Logger log = LoggerFactory.getLogger(ImVideoCallServiceImpl.class);

    private static final String CALL_CACHE_KEY = "im:video_call:";
    private static final String USER_CALL_KEY = "im:user_call:";
    private static final int CALL_TIMEOUT_SECONDS = 60; // 60秒超时

    @Autowired
    private ImVideoCallMapper videoCallMapper;

    @Autowired
    private ImVideoCallParticipantMapper participantMapper;

    @Autowired
    private ImUserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Long initiateCall(Long callerId, Long calleeId, Long conversationId, String callType) {
        // 检查接收者是否在线
        String onlineKey = "im:online:" + calleeId;
        Boolean isOnline = redisTemplate.hasKey(onlineKey);

        if (Boolean.FALSE.equals(isOnline)) {
            throw new BusinessException(ImErrorCode.CALL_USER_OFFLINE, "对方不在线");
        }

        // 检查接收者是否正在通话中
        String userCallKey = USER_CALL_KEY + calleeId;
        Boolean isInCall = redisTemplate.hasKey(userCallKey);
        if (Boolean.TRUE.equals(isInCall)) {
            throw new BusinessException(ImErrorCode.CALL_USER_BUSY, "对方正在通话中");
        }

        // 创建通话记录
        ImVideoCall call = new ImVideoCall();
        call.setCallerId(callerId);
        call.setCalleeId(calleeId);
        call.setConversationId(conversationId);
        call.setCallType(callType);
        call.setStatus("CALLING");
        call.setStartTime(LocalDateTime.now());

        videoCallMapper.insertImVideoCall(call);

        // 缓存通话信息
        String callKey = CALL_CACHE_KEY + call.getId();
        redisTemplate.opsForValue().set(callKey, call, CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // 设置用户通话状态
        redisTemplate.opsForValue().set(USER_CALL_KEY + callerId, call.getId(), CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        log.info("发起通话: callId={}, caller={}, callee={}, type={}", call.getId(), callerId, calleeId, callType);

        // 发送通知给接收者
        ImWebSocketEndpoint.sendCallNotification(calleeId, call.getId(), callType, callerId);

        return call.getId();
    }

    @Override
    public void acceptCall(Long callId, Long userId) {
        ImVideoCall call = getCallEntity(callId);
        if (call == null) {
            BusinessExceptionHelper.throwVideoCallNotFound();
        }

        // 只有接收者可以接听
        if (!call.getCalleeId().equals(userId)) {
            BusinessExceptionHelper.throwNoPermissionToAnswer();
        }

        if (!"CALLING".equals(call.getStatus())) {
            BusinessExceptionHelper.throwVideoCallStatusError();
        }

        // 更新状态
        call.setStatus("CONNECTED");
        call.setStartTime(LocalDateTime.now());
        videoCallMapper.updateImVideoCall(call);

        // 缓存通话信息（延长缓存时间到1小时）
        String callKey = CALL_CACHE_KEY + callId;
        redisTemplate.opsForValue().set(callKey, call, 3600, TimeUnit.SECONDS);

        // 设置双方用户通话状态
        redisTemplate.opsForValue().set(USER_CALL_KEY + call.getCallerId(), callId, 3600, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(USER_CALL_KEY + call.getCalleeId(), callId, 3600, TimeUnit.SECONDS);

        log.info("接听通话: callId={}, user={}", callId, userId);

        // 通知对方已接听
        ImWebSocketEndpoint.sendCallStatusUpdate(callId, call.getCallerId(), call.getCalleeId(), "CONNECTED");
    }

    @Override
    public void rejectCall(Long callId, Long userId, String reason) {
        ImVideoCall call = getCallEntity(callId);
        if (call == null) {
            BusinessExceptionHelper.throwVideoCallNotFound();
        }

        // 只有接收者可以拒绝
        if (!call.getCalleeId().equals(userId)) {
            BusinessExceptionHelper.throwNoPermissionToReject();
        }

        if (!"CALLING".equals(call.getStatus())) {
            BusinessExceptionHelper.throwVideoCallStatusError();
        }

        // 更新状态
        call.setStatus("REJECTED");
        call.setRejectReason(reason);
        call.setEndTime(LocalDateTime.now());
        videoCallMapper.updateImVideoCall(call);

        // 通知对方已拒绝
        ImWebSocketEndpoint.sendCallStatusUpdate(callId, call.getCallerId(), call.getCalleeId(), "REJECTED");

        // 清除通话状态
        clearCallState(callId);

        log.info("拒绝通话: callId={}, user={}, reason={}", callId, userId, reason);
    }

    @Override
    public void endCall(Long callId, Long userId) {
        ImVideoCall call = getCallEntity(callId);
        if (call == null) {
            BusinessExceptionHelper.throwVideoCallNotFound();
        }

        // 只有通话参与者可以结束
        if (!call.getCallerId().equals(userId) && !call.getCalleeId().equals(userId)) {
            BusinessExceptionHelper.throwNoPermissionToEnd();
        }

        if ("ENDED".equals(call.getStatus()) || "REJECTED".equals(call.getStatus()) || "TIMEOUT".equals(call.getStatus())) {
            return; // 已经结束
        }

        // 计算通话时长
        LocalDateTime endTime = LocalDateTime.now();
        Integer duration = null;
        if (call.getStartTime() != null) {
            duration = (int) Duration.between(call.getStartTime(), endTime).getSeconds();
        }

        // 更新状态
        call.setStatus("ENDED");
        call.setEndTime(endTime);
        call.setDuration(duration);
        videoCallMapper.updateImVideoCall(call);

        // 通知对方已结束
        ImWebSocketEndpoint.sendCallStatusUpdate(callId, call.getCallerId(), call.getCalleeId(), "ENDED");

        // 清除通话状态
        clearCallState(callId);

        log.info("结束通话: callId={}, user={}, duration={}s", callId, userId, duration);
    }

    @Override
    public Object getCallInfo(Long callId) {
        String callKey = CALL_CACHE_KEY + callId;
        ImVideoCall call = (ImVideoCall) redisTemplate.opsForValue().get(callKey);
        if (call == null) {
            call = videoCallMapper.selectImVideoCallById(callId);
        }
        return formatCallInfo(call);
    }

    @Override
    public Object getUserActiveCall(Long userId) {
        String userCallKey = USER_CALL_KEY + userId;
        Object callIdObj = redisTemplate.opsForValue().get(userCallKey);
        if (callIdObj == null) {
            return null;
        }

        Long callId = Long.valueOf(callIdObj.toString());
        return getCallInfo(callId);
    }

    @Override
    public void timeoutCall(Long callId) {
        ImVideoCall call = getCallEntity(callId);
        if (call == null) {
            return;
        }

        if (!"CALLING".equals(call.getStatus())) {
            return;
        }

        // 更新状态
        call.setStatus("TIMEOUT");
        call.setEndTime(LocalDateTime.now());
        videoCallMapper.updateImVideoCall(call);

        // 清除通话状态
        clearCallState(callId);

        log.info("通话超时: callId={}", callId);
    }

    /**
     * 获取通话实体
     */
    private ImVideoCall getCallEntity(Long callId) {
        String callKey = CALL_CACHE_KEY + callId;
        ImVideoCall call = (ImVideoCall) redisTemplate.opsForValue().get(callKey);
        if (call == null) {
            call = videoCallMapper.selectImVideoCallById(callId);
            if (call != null && "CALLING".equals(call.getStatus())) {
                // 如果数据库中还在呼叫中，说明可能缓存丢失了，恢复缓存
                redisTemplate.opsForValue().set(callKey, call, CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }
        }
        return call;
    }

    /**
     * 清除通话状态
     */
    private void clearCallState(Long callId) {
        ImVideoCall call = getCallEntity(callId);
        if (call == null) {
            return;
        }

        String callKey = CALL_CACHE_KEY + callId;
        redisTemplate.delete(callKey);

        if (call.getCallerId() != null) {
            redisTemplate.delete(USER_CALL_KEY + call.getCallerId());
        }
        if (call.getCalleeId() != null) {
            redisTemplate.delete(USER_CALL_KEY + call.getCalleeId());
        }
    }

    /**
     * 格式化通话信息
     */
    private Map<String, Object> formatCallInfo(ImVideoCall call) {
        if (call == null) {
            return null;
        }

        Map<String, Object> info = new HashMap<>();
        info.put("callId", call.getId());
        info.put("callerId", call.getCallerId());
        info.put("calleeId", call.getCalleeId());
        info.put("conversationId", call.getConversationId());
        info.put("callType", call.getCallType());
        info.put("status", call.getStatus());
        info.put("startTime", call.getStartTime());
        info.put("endTime", call.getEndTime());
        info.put("duration", call.getDuration());
        info.put("rejectReason", call.getRejectReason());

        return info;
    }

    @Override
    public List<?> getCallHistory(Long userId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = SystemConstants.DEFAULT_PAGE_SIZE;
        }
        if (limit > SystemConstants.MAX_PAGE_SIZE) {
            limit = SystemConstants.MAX_PAGE_SIZE; // 最多返回100条
        }

        List<ImVideoCall> calls = videoCallMapper.selectCallsByUserId(userId, limit);
        return calls.stream()
                .map(this::formatCallInfo)
                .collect(Collectors.toList());
    }

    // ==================== 群组多人通话方法 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long initiateGroupCall(Long callerId, Long conversationId, String callType,
                                   Integer maxParticipants, List<Long> invitedUserIds) {
        // 验证最大参与者数
        if (maxParticipants == null || maxParticipants <= 0) {
            maxParticipants = SystemConstants.VIDEO_CALL_MAX_PARTICIPANTS;
        }
        if (maxParticipants > SystemConstants.VIDEO_CALL_MAX_PARTICIPANTS) {
            BusinessExceptionHelper.throwMaxParticipantsExceeded(SystemConstants.VIDEO_CALL_MAX_PARTICIPANTS);
        }

        if (invitedUserIds == null || invitedUserIds.isEmpty()) {
            BusinessExceptionHelper.throwInviteAtLeastOne();
        }

        // 检查邀请人数
        if (invitedUserIds.size() + 1 > maxParticipants) {
            BusinessExceptionHelper.throwInviteExceedsLimit();
        }

        // 检查发起者是否正在通话
        String callerCallKey = USER_CALL_KEY + callerId;
        Boolean callerInCall = redisTemplate.hasKey(callerCallKey);
        if (Boolean.TRUE.equals(callerInCall)) {
            BusinessExceptionHelper.throwAlreadyInCall();
        }

        // 创建通话记录
        ImVideoCall call = new ImVideoCall();
        call.setCallerId(callerId);
        call.setConversationId(conversationId);
        call.setCallType(callType);
        call.setCallMode("GROUP");
        call.setStatus("CALLING");
        call.setMaxParticipants(maxParticipants);
        call.setCurrentParticipants(1); // 发起者自动加入
        call.setStartTime(LocalDateTime.now());
        call.setRoomId("call_" + System.currentTimeMillis());

        videoCallMapper.insertImVideoCall(call);

        Long callId = call.getId();

        // 添加发起者作为参与者
        addParticipant(callId, callerId, "JOINED");

        // 添加被邀请用户（状态为已邀请）
        for (Long userId : invitedUserIds) {
            addParticipant(callId, userId, "INVITED");
        }

        // 缓存通话信息
        String callKey = CALL_CACHE_KEY + callId;
        redisTemplate.opsForValue().set(callKey, call, CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // 设置发起者通话状态
        redisTemplate.opsForValue().set(callerCallKey, callId, CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        log.info("发起群组通话: callId={}, caller={}, conversationId={}, maxParticipants={}",
            callId, callerId, conversationId, maxParticipants);

        return callId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinGroupCall(Long callId, Long userId) {
        ImVideoCall call = getCallEntity(callId);
        if (call == null) {
            BusinessExceptionHelper.throwVideoCallNotFound();
        }

        if (!"GROUP".equals(call.getCallMode())) {
            BusinessExceptionHelper.throwNotGroupCall();
        }

        // 检查是否已参与
        ImVideoCallParticipant existing = participantMapper.selectByCallIdAndUserId(callId, userId);
        if (existing != null) {
            if ("JOINED".equals(existing.getStatus())) {
                BusinessExceptionHelper.throwAlreadyJoinedCall();
            }
            if ("LEFT".equals(existing.getStatus())) {
                BusinessExceptionHelper.throwAlreadyLeftCall();
            }
        }

        // 检查人数限制
        Integer currentCount = participantMapper.countJoinedByCallId(callId);
        if (currentCount >= call.getMaxParticipants()) {
            BusinessExceptionHelper.throwCallFull();
        }

        // 更新或创建参与者记录
        if (existing != null) {
            existing.setStatus("JOINED");
            existing.setJoinTime(LocalDateTime.now());
            participantMapper.updateById(existing);
        } else {
            addParticipant(callId, userId, "JOINED");
        }

        // 更新当前参与者数
        call.setCurrentParticipants(currentCount + 1);
        videoCallMapper.updateImVideoCall(call);

        // 设置用户通话状态
        redisTemplate.opsForValue().set(USER_CALL_KEY + userId, callId, 3600, TimeUnit.SECONDS);

        log.info("加入群组通话: callId={}, userId={}", callId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveGroupCall(Long callId, Long userId) {
        ImVideoCall call = getCallEntity(callId);
        if (call == null) {
            BusinessExceptionHelper.throwVideoCallNotFound();
        }

        if (!"GROUP".equals(call.getCallMode())) {
            BusinessExceptionHelper.throwNotGroupCall();
        }

        // 更新参与者状态
        ImVideoCallParticipant participant = participantMapper.selectByCallIdAndUserId(callId, userId);
        if (participant != null && "JOINED".equals(participant.getStatus())) {
            participant.setStatus("LEFT");
            participant.setLeaveTime(LocalDateTime.now());
            participantMapper.updateById(participant);

            // 更新当前参与者数
            Integer currentCount = call.getCurrentParticipants();
            if (currentCount != null && currentCount > 0) {
                call.setCurrentParticipants(currentCount - 1);
                videoCallMapper.updateImVideoCall(call);
            }
        }

        // 清除用户通话状态
        redisTemplate.delete(USER_CALL_KEY + userId);

        log.info("离开群组通话: callId={}, userId={}", callId, userId);
    }

    @Override
    public void toggleMute(Long callId, Long userId, Boolean muted) {
        ImVideoCallParticipant participant = participantMapper.selectByCallIdAndUserId(callId, userId);
        if (participant == null) {
            BusinessExceptionHelper.throwNotInCall();
        }

        participant.setIsMuted(muted);
        participantMapper.updateById(participant);

        log.info("切换麦克风状态: callId={}, userId={}, muted={}", callId, userId, muted);
    }

    @Override
    public void toggleCamera(Long callId, Long userId, Boolean cameraOff) {
        ImVideoCallParticipant participant = participantMapper.selectByCallIdAndUserId(callId, userId);
        if (participant == null) {
            BusinessExceptionHelper.throwNotInCall();
        }

        participant.setIsCameraOff(cameraOff);
        participantMapper.updateById(participant);

        log.info("切换摄像头状态: callId={}, userId={}, cameraOff={}", callId, userId, cameraOff);
    }

    @Override
    public List<?> getCallParticipants(Long callId) {
        List<ImVideoCallParticipant> participants = participantMapper.selectByCallId(callId);

        return participants.stream()
            .map(p -> {
                Map<String, Object> info = new HashMap<>();
                info.put("userId", p.getUserId());
                info.put("userName", p.getUserName());
                info.put("userAvatar", p.getUserAvatar());
                info.put("status", p.getStatus());
                info.put("isMuted", p.getIsMuted());
                info.put("isCameraOff", p.getIsCameraOff());
                info.put("joinTime", p.getJoinTime());
                return info;
            })
            .collect(Collectors.toList());
    }

    /**
     * 添加通话参与者
     */
    private void addParticipant(Long callId, Long userId, String status) {
        ImUser user = userMapper.selectImUserById(userId);
        if (user == null) {
            return;
        }

        ImVideoCallParticipant participant = new ImVideoCallParticipant();
        participant.setCallId(callId);
        participant.setUserId(userId);
        participant.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        participant.setUserAvatar(user.getAvatar());
        participant.setStatus(status);
        participant.setIsMuted(false);
        participant.setIsCameraOff(false);
        participant.setCreateTime(LocalDateTime.now());

        if ("JOINED".equals(status)) {
            participant.setJoinTime(LocalDateTime.now());
        }

        participantMapper.insert(participant);
    }
}
